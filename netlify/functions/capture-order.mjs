import { PLANS, pp, signLicence, json } from "../lib/paypal.mjs";
// Captures the payment, checks with PayPal that the right amount was really paid, then returns a signed licence code.
// Calling it again for an already-paid order returns the same code (so a buyer can restore access).
export default async (req) => {
  try {
    const { orderID } = await req.json().catch(() => ({}));
    if (!/^[A-Z0-9]{10,30}$/.test(orderID || "")) return json({ error: "Buyurtma raqami noto'g'ri" }, 400);
    let r = await pp(`/v2/checkout/orders/${orderID}/capture`, { method: "POST", body: "{}" });
    if (!r.ok) {
      const issue = r.body?.details?.[0]?.issue;
      if (issue === "ORDER_ALREADY_CAPTURED") r = await pp(`/v2/checkout/orders/${orderID}`);
      else return json({ error: "To'lov o'tmadi", detail: issue || r.body?.name }, 402);
    }
    const o = r.body, pu = o.purchase_units?.[0], cap = pu?.payments?.captures?.[0];
    const plan = pu?.custom_id, P = PLANS[plan];
    if (o.status !== "COMPLETED" || !cap || cap.status !== "COMPLETED" || !P) return json({ error: "To'lov tasdiqlanmadi" }, 402);
    if (cap.amount?.currency_code !== "USD" || cap.amount?.value !== P.price) return json({ error: "To'lov summasi mos emas" }, 402);
    const paidAt = new Date(cap.create_time || Date.now());
    const exp = new Date(paidAt.getTime() + 31 * 864e5).toISOString().slice(0, 10);
    const code = await signLicence(plan, exp, orderID.slice(-8));
    return json({ plan, exp, code, email: o.payer?.email_address || null });
  } catch (e) { return json({ error: e.message }, 500); }
};
export const config = { path: "/api/capture-order", method: "POST" };
