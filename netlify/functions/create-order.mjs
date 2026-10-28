import { PLANS, pp, json } from "../lib/paypal.mjs";
// Creates a PayPal order. The price is set here on the server, so a buyer cannot change it in the browser.
export default async (req) => {
  try {
    const { plan } = await req.json().catch(() => ({}));
    const P = PLANS[plan]; if (!P) return json({ error: "Noma'lum tarif" }, 400);
    const r = await pp("/v2/checkout/orders", { method: "POST", body: JSON.stringify({
      intent: "CAPTURE",
      purchase_units: [{ custom_id: plan, description: P.name, amount: { currency_code: "USD", value: P.price } }],
      application_context: { brand_name: "Forma Studio", shipping_preference: "NO_SHIPPING", user_action: "PAY_NOW" }
    }) });
    if (!r.ok) return json({ error: "Buyurtma yaratilmadi", detail: r.body?.name }, 502);
    return json({ id: r.body.id });
  } catch (e) { return json({ error: e.message }, 500); }
};
export const config = { path: "/api/create-order", method: "POST" };
