// Public PayPal client id for the checkout buttons (the client id is public by design; the secret never leaves the server).
export default async () => Response.json({ clientId: Netlify.env.get("PAYPAL_CLIENT_ID") || null, env: Netlify.env.get("PAYPAL_ENV") || "live" });
export const config = { path: "/api/config" };
