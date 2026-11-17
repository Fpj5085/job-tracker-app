// A3 demo single-step capture: node capture-step.cjs <outname> <url> [clickText]
const { spawn } = require("child_process");
const http = require("http");
const fs = require("fs");
const path = require("path");
const WS = WebSocket;
const JWT = process.env.JWT_TOKEN;
const OUT = process.env.OUT_DIR;
const outName = process.argv[2];
const targetUrl = process.argv[3];
const clickText = process.argv[4];
const port = 9444;
const chrome = spawn(process.env.CHROME_PATH, [
  "--headless=new", "--remote-debugging-port=" + port, "--user-data-dir=" + process.env.CHROME_PROFILE,
  "--window-size=1360,850", "--disable-gpu", "about:blank"
], { stdio: "ignore" });

const sleep = (ms) => new Promise(r => setTimeout(r, ms));

function getJson(p, method) {
  return new Promise((res, rej) => {
    const req = http.request({ host: "127.0.0.1", port, path: p, method: method || "GET" }, r => {
      let b = ""; r.on("data", d => b += d); r.on("end", () => { try { res(JSON.parse(b)); } catch (e) { res(null); } });
    });
    req.on("error", rej); req.end();
  });
}

let ws; let id = 0; const pending = new Map();
const consoleEvents = [];
function send(method, params) {
  return new Promise((res, rej) => { const mid = ++id; pending.set(mid, { res, rej }); ws.send(JSON.stringify({ id: mid, method, params: params || {} })); });
}

async function main() {
  let target = null;
  for (let i = 0; i < 30; i++) {
    try { target = await getJson("/json/list"); if (target && target.length) break; } catch (e) {}
    await sleep(400);
  }
  if (!target || !target.length) { const created = await getJson("/json/new?url=about:blank", "PUT"); target = [created]; }
  const page = target.find(t => t.type === "page") || target[0];
  ws = new WS(page.webSocketDebuggerUrl);
  await new Promise(r => ws.addEventListener("open", r, { once: true }));
  ws.addEventListener("message", m => { const msg = JSON.parse(m.data); if (msg.id && pending.has(msg.id)) { const p = pending.get(msg.id); pending.delete(msg.id); msg.error ? p.rej(new Error(JSON.stringify(msg.error))) : p.res(msg.result); } });
  ws.addEventListener("message", m => { const msg = JSON.parse(m.data); if (msg.method === "Runtime.consoleAPICalled") consoleEvents.push(msg.params.type + ": " + msg.params.args.map(a => a.value || a.description || "").join(" ")); if (msg.method === "Runtime.exceptionThrown") consoleEvents.push("EXCEPTION: " + ((msg.params.exceptionDetails.exception && msg.params.exceptionDetails.exception.description) || msg.params.exceptionDetails.text)); if (msg.id && pending.has(msg.id)) { const p = pending.get(msg.id); pending.delete(msg.id); msg.error ? p.rej(new Error(JSON.stringify(msg.error))) : p.res(msg.result); } });
  await send("Page.enable"); await send("Network.enable"); await send("Runtime.enable");
  await send("Page.navigate", { url: targetUrl }); await sleep(4000);
  await send("Runtime.evaluate", { expression: "localStorage.setItem('username', 'demo')" });
  await send("Page.navigate", { url: targetUrl }); await sleep(4000);
  if (clickText) {
    const r = await send("Runtime.evaluate", { expression: `(() => { const el = Array.from(document.querySelectorAll("button, a")).find(e => e.textContent.trim().startsWith(${JSON.stringify(clickText)}) && e.offsetParent !== null); if (el) { el.click(); return "clicked"; } return "notfound"; })()`, returnByValue: true });
    console.log("CLICK", JSON.stringify(r.result.value)); await sleep(3000);
  }
  const shot = await send("Page.captureScreenshot", { format: "png" });
  fs.writeFileSync(path.join(OUT, outName), Buffer.from(shot.data, "base64"));
  const text = await send("Runtime.evaluate", { expression: "document.body.innerText", returnByValue: true });
  fs.writeFileSync(path.join(OUT, outName.replace(".png", ".txt")), String(text.result.value), "utf8"); // innerText dump
  fs.writeFileSync(path.join(OUT, outName.replace(".png", ".console.txt")), consoleEvents.join("\n"), "utf8");
  console.log("saved", outName);
  chrome.kill(); process.exit(0);
}

main().catch(e => { console.error("FAILED", e.stack || e.message); chrome.kill(); process.exit(1); });
