// Shared helpers: PayPal REST calls + licence signing. Secrets come only from environment variables.
export const PLANS = {
  pro: { price: "4.99", name: "Forma Studio Pro - 31 kun" },
  max: { price: "14.99", name: "Forma Studio Max - 31 kun" }
};
const env = k => Netlify.env.get(k);
export const API = () => (env("PAYPAL_ENV") === "sandbox" ? "https://api-m.sandbox.paypal.com" : "https://api-m.paypal.com");

export async function paypalToken() {
  const id = env("PAYPAL_CLIENT_ID"), secret = env("PAYPAL_CLIENT_SECRET");
  if (!id || !secret) throw new Error("PayPal kalitlari sozlanmagan");
  const r = await fetch(API() + "/v1/oauth2/token", {
    method: "POST",
    headers: { Authorization: "Basic " + btoa(id + ":" + secret), "Content-Type": "application/x-www-form-urlencoded" },
    body: "grant_type=client_credentials"
  });
  if (!r.ok) throw new Error("PayPal token xatosi " + r.status);
  return (await r.json()).access_token;
}
export async function pp(path, opts = {}) {
  const t = await paypalToken();
  const r = await fetch(API() + path, { ...opts, headers: { Authorization: "Bearer " + t, "Content-Type": "application/json", ...(opts.headers || {}) } });
  const body = await r.json().catch(() => ({}));
  return { ok: r.ok, status: r.status, body };
}
const b64u = buf => Buffer.from(buf).toString("base64url");
export async function signLicence(plan, expiry, nonce) {
  const jwk = JSON.parse(env("LICENSE_PRIVATE_KEY") || "null");
  if (!jwk) throw new Error("Litsenziya kaliti sozlanmagan");
  const key = await crypto.subtle.importKey("jwk", { kty: jwk.kty, crv: jwk.crv, x: jwk.x, y: jwk.y, d: jwk.d }, { name: "ECDSA", namedCurve: "P-256" }, false, ["sign"]);
  const payload = b64u(JSON.stringify({ p: plan, e: expiry, n: nonce }));
  const sig = await crypto.subtle.sign({ name: "ECDSA", hash: "SHA-256" }, key, new TextEncoder().encode(payload));
  return `FS1-${payload}.${b64u(sig)}`;
}
export const json = (o, status = 200) => Response.json(o, { status });
