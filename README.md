# Stripe integration for ShopSafe

This branch adds Stripe PaymentSheet integration for one-time payments and a Firebase Cloud Functions backend to create PaymentIntents and handle Stripe webhooks.

## What I added

- app/src/main/java/com/aistudio/shopsafe/payment/PaymentScreen.kt
- app/src/main/java/com/aistudio/shopsafe/payment/PaymentViewModel.kt
- app/src/main/java/com/aistudio/shopsafe/network/StripeApiService.kt
- app/src/main/java/com/aistudio/shopsafe/repository/PaymentRepository.kt
- functions/package.json
- functions/tsconfig.json
- functions/src/index.ts
- .env.example (added STRIPE_* placeholders)
- docs/stripe-setup.md

## How to test locally

1. Add STRIPE_PUBLISHABLE_KEY and STRIPE_SECRET_KEY and STRIPE_WEBHOOK_SECRET to `.env` in the repo root (do not commit). Example:

   STRIPE_PUBLISHABLE_KEY=pk_test_...
   STRIPE_SECRET_KEY=sk_test_...
   STRIPE_WEBHOOK_SECRET=whsec_...

2. Install Firebase tools and dependencies for functions:
   cd functions && npm install

3. Start the functions emulator or deploy:
   firebase emulators:start --only functions

4. Use the Stripe CLI to forward webhooks to your local endpoint (or register webhook URL in Stripe dashboard):
   stripe listen --forward-to localhost:5001/<PROJECT>/us-central1/api/webhook

5. Run the Android app, open PaymentScreen, and test with card number `4242 4242 4242 4242`.


## Important notes

- Do not commit secret keys.
- The server verifies webhook signatures using STRIPE_WEBHOOK_SECRET.

