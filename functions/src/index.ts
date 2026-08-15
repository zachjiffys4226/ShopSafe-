import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import express from 'express';
import Stripe from 'stripe';
import bodyParser from 'body-parser';
import cors from 'cors';

admin.initializeApp();
const app = express();
app.use(cors({ origin: true }));
app.use(bodyParser.raw({ type: 'application/json' }));

const stripeSecret = process.env.STRIPE_SECRET_KEY;
const stripe = new Stripe(stripeSecret || '', { apiVersion: '2024-08-05' });

app.post('/create-payment-intent', async (req, res) => {
  try {
    // Expecting JSON body with { amount, currency }
    const raw = req.body && req.body.toString ? req.body.toString() : '{}';
    const parsed = JSON.parse(raw);
    const amount = parsed.amount || 500; // default $5.00
    const currency = parsed.currency || 'usd';

    const paymentIntent = await stripe.paymentIntents.create({
      amount,
      currency,
      automatic_payment_methods: { enabled: true },
    });

    res.json({ clientSecret: paymentIntent.client_secret });
  } catch (err: any) {
    console.error('create-payment-intent error', err);
    res.status(500).json({ error: err.message });
  }
});

app.post('/webhook', async (req, res) => {
  const sig = req.headers['stripe-signature'] as string | undefined;
  const webhookSecret = process.env.STRIPE_WEBHOOK_SECRET;

  if (!webhookSecret) {
    console.warn('No STRIPE_WEBHOOK_SECRET set in env; skipping signature verification');
  }

  let event: Stripe.Event;

  try {
    if (webhookSecret && sig) {
      event = stripe.webhooks.constructEvent(req.body as Buffer, sig, webhookSecret);
    } else {
      // If no webhook secret available (dev mode), parse JSON directly
      event = JSON.parse(req.body.toString());
    }
  } catch (err: any) {
    console.error('Webhook signature verification failed.', err.message);
    return res.status(400).send(`Webhook Error: ${err.message}`);
  }

  // Handle the event
  switch (event.type) {
    case 'payment_intent.succeeded':
      const paymentIntent = event.data.object as Stripe.PaymentIntent;
      console.log(`PaymentIntent for ${paymentIntent.amount} succeeded.`);
      // TODO: update Firestore order document or perform fulfillment
      break;
    case 'payment_intent.payment_failed':
      const failedIntent = event.data.object as Stripe.PaymentIntent;
      console.log(`Payment failed: ${failedIntent.last_payment_error?.message}`);
      break;
    default:
      console.log(`Unhandled event type ${event.type}`);
  }

  res.json({ received: true });
});

exports.api = functions.https.onRequest(app);
