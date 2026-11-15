const { spawn } = require("child_process");
const http = require("http");
const fs = require("fs");
const path = require("path");
const WS = WebSocket;
const JWT = process.env.JWT_TOKEN;
const BASE = "http://localhost:3101";
const OUT = process.env.OUT_DIR;
const port = 9444;
const chrome = spawn(process.env.CHROME_PATH, [
  "--headless=new", "--remote-debugging-port=" + port, "--user-data-dir=" + process.env.CHROME_PROFILE,
  "--window-size=1360,850", "--disable-gpu", "about:blank"
], { stdio: "ignore" });

function sleep(ms){ return new Promise(r => setTimeout(r, ms)); }

async function getWsUrl() {
  for (let i = 0; i < 30; i++) {
    try {
      const data = await new Promise((res, rej) => {
        http.get("http://127.0.0.1:" + port + "/json/list", r => { let s=""; r.on("data", d => s += d); r.on("end", () => { const list = JSON.parse(s); const page = list.find(t => t.type === "page"); res(page ? page.webSocketDebuggerUrl : list[0].webSocketDebuggerUrl); }); }).on("error", rej);
      });
      if (typeof data === "string") { return data; } if (!data || data.length === 0) { const created = await new Promise((res, rej) => { const req = http.request({ host: "127.0.0.1", port, path: "/json/new?url=about:blank", method: "PUT" }, r => { let b=""; r.on("data", d => b += d); r.on("end", () => res(JSON.parse(b))); }); req.on("error", rej); req.end(); }); return created.webSocketDebuggerUrl; } return data[0].webSocketDebuggerUrl;
    } catch (e) { await sleep(500); }
  }
  throw new Error("chrome devtools not reachable");
}

let ws, id = 0; const pending = new Map();
function send(method, params) {
  return new Promise((res, rej) => { const mid = ++id; pending.set(mid, { res, rej }); ws.send(JSON.stringify({ id: mid, method, params: params || {} })); });
}

async function main() {
  const url = await getWsUrl();
  ws = new WS(url);
  await new Promise(r => ws.addEventListener("open", r, { once: true }));
  ws.addEventListener("message", m => { const msg = JSON.parse(m.data); if (msg.id && pending.has(msg.id)) { const p = pending.get(msg.id); pending.delete(msg.id); msg.error ? p.rej(new Error(JSON.stringify(msg.error))) : p.res(msg.result); } });
  await send("Page.enable");
  await send("Network.enable");
  // seed auth cookie on localhost
  await send("Network.setCookie", { name: "jwt", value: JWT, domain: "localhost", url: BASE + "/", path: "/", httpOnly: true, sameSite: "Lax" });
  async function shot(name) {
    await sleep(2500);
    const r = await send("Page.captureScreenshot", { format: "png" });
    fs.writeFileSync(path.join(OUT, name), Buffer.from(r.data, "base64"));
    console.log("saved", name);
  }
  async function go(u) { await send("Page.navigate", { url: u }); await sleep(3500); const loc = await send("Runtime.evaluate", { expression: "location.href", returnByValue: true }); console.log("AT", loc.result.value); }
  async function clickByText(text) {
    const r = await send("Runtime.evaluate", { expression: `(() => { const els = Array.from(document.querySelectorAll("button, a")); const el = els.find(e => e.textContent.trim().startsWith(${JSON.stringify(text)}) && e.offsetParent !== null); if (el) { el.click(); return "clicked:" + el.textContent.trim(); } return "notfound:" + text; })()`, returnByValue: true });
    console.log("CLICK", JSON.stringify(r.result.value));
    await sleep(3000);
  }

  // 01 login page (unauthenticated render)
  await go(BASE + "/login"); await shot("step1-login.png");
  // authenticated pages via seeded JWT cookie + localStorage session marker
  await send("Runtime.evaluate", { expression: "localStorage.setItem('username', 'demo')" });
  await go(BASE + "/dashboard"); await shot("step2-dashboard.png");
  await go(BASE + "/applications"); await sleep(2500);
  await send("Runtime.evaluate", { expression: `Array.from(document.querySelectorAll("a")).find(a => a.getAttribute("href") && a.getAttribute("href").startsWith("/applications/"))?.click()` });
  await sleep(3500);
  await shot("step3-interview-timeline.png");
  await clickByText("沟通记录"); await shot("step4-communications.png");
  await clickByText("Offer"); await shot("step5-offer-entry.png");
  await go(BASE + "/offers"); await shot("step6-offers-compare.png");
  console.log("DONE");
  chrome.kill();
  process.exit(0);
}

main().catch(e => { console.error("FAILED", e.stack || e.message); chrome.kill(); process.exit(1); });
