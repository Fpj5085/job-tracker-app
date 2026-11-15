// A3 screen recording: drive a visible Chrome through the demo flow while ffmpeg captures the desktop
const { spawn } = require("child_process");
const http = require("http");
const path = require("path");
const WS = WebSocket;
const JWT = process.env.JWT_TOKEN;
const port = 9555;
const APP_ID = process.env.APP_ID;
const ffmpeg = spawn(process.env.FFMPEG_PATH || "ffmpeg", ["-y", "-f", "gdigrab", "-framerate", "15", "-i", "desktop", "-t", "60", "-c:v", "libx264", "-pix_fmt", "yuv420p", process.env.OUT_MP4], { stdio: "ignore" });

const sleep = (ms) => new Promise(r => setTimeout(r, ms));
function getJson(p, method) { return new Promise((res, rej) => { const req = http.request({ host: "127.0.0.1", port, path: p, method: method || "GET" }, r => { let b = ""; r.on("data", d => b += d); r.on("end", () => { try { res(JSON.parse(b)); } catch (e) { res(null); } }); }); req.on("error", rej); req.end(); }); }

let ws; let id = 0; const pending = new Map();
function send(method, params) { return new Promise((res, rej) => { const mid = ++id; pending.set(mid, { res, rej }); ws.send(JSON.stringify({ id: mid, method, params: params || {} })); }); }

async function main() {
  const chrome = spawn(process.env.CHROME_PATH, ["--remote-debugging-port=" + port, "--user-data-dir=" + process.env.CHROME_PROFILE, "--window-size=1360,850", "--new-window", "about:blank"]);
  await sleep(5000);
  let target = null;
  for (let i = 0; i < 20; i++) { try { target = await getJson("/json/list"); if (target && target.length) break; } catch (e) {} await sleep(500); }
  const page = target.find(t => t.type === "page") || target[0];
  ws = new WS(page.webSocketDebuggerUrl);
  await new Promise(r => ws.addEventListener("open", r, { once: true }));
  ws.addEventListener("message", m => { const msg = JSON.parse(m.data); if (msg.id && pending.has(msg.id)) { const p = pending.get(msg.id); pending.delete(msg.id); msg.error ? p.rej(new Error(JSON.stringify(msg.error))) : p.res(msg.result); } });
  await send("Page.enable"); await send("Network.enable");
  await send("Emulation.setDeviceMetricsOverride", { width: 1360, height: 850, deviceScaleFactor: 1, mobile: false });
  await send("Network.setCookie", { name: "jwt", value: JWT, domain: "localhost", url: "http://localhost:3101/", path: "/", httpOnly: true, sameSite: "Lax" });
  const go = async (u, dwell) => { await send("Page.navigate", { url: u }); await sleep(dwell || 5000); };
  const click = async (text) => { await send("Runtime.evaluate", { expression: `(() => { const el = Array.from(document.querySelectorAll("button, a")).find(e => e.textContent.trim().startsWith(${JSON.stringify(text)}) && e.offsetParent !== null); if (el) el.click(); return el ? "ok" : "nf"; })()`, returnByValue: true }); };
  await sleep(8000); // give ffmpeg a lead-in showing the login page
  await go("http://localhost:3101/login", 6000);
  await send("Runtime.evaluate", { expression: "localStorage.setItem('username', 'demo')" });
  await go("http://localhost:3101/dashboard", 8000);
  await go("http://localhost:3101/applications", 6000);
  await go("http://localhost:3101/applications/" + APP_ID, 7000);
  await click("沟通记录"); await sleep(7000);
  await click("Offer ("); await sleep(8000);
  await go("http://localhost:3101/offers", 12000);
  await sleep(5000);
  ffmpeg.kill(); chrome.kill(); process.exit(0);
}

main().catch(e => { console.error("FAILED", e.stack || e.message); try { ffmpeg.kill(); } catch (_) {} process.exit(1); });
