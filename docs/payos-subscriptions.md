# PayOS salon subscription payments

The owner chooses a plan and creates a subscription request. `POST /api/v1/owner/subscription/requests/{id}/prepare-payment` creates a PayOS hosted checkout link. The owner pays on that page. PayOS sends a signed webhook to `POST /api/v1/webhooks/payos`. The backend checks the signature, order code, payment link ID and exact quoted amount before recording payment. An admin can approve and activate the plan after that record exists.

## Configuration

Set `PAYOS_CLIENT_ID`, `PAYOS_API_KEY` and `PAYOS_CHECKSUM_KEY` in the backend `.env` from the same PayOS payment channel. Set `PAYOS_RETURN_URL` and `PAYOS_CANCEL_URL` to the public frontend owner subscription page. The frontend should have a public HTTPS URL in production.

Register `https://YOUR_PUBLIC_API_HOST/api/v1/webhooks/payos` as the PayOS payment channel webhook. PayOS documents `POST /confirm-webhook` for registering and checking this URL. The endpoint must be reachable from PayOS over HTTPS. The PayOS registration sample webhook is acknowledged even when its order code has no matching local subscription.

For local Docker development, set `NGROK_DOMAIN` and `NGROK_AUTHTOKEN` in `.env`, then run `docker compose --profile payos-local up -d ngrok`. The tunnel forwards the domain to `backend:8080`. Set the PayOS webhook to `https://NGROK_DOMAIN/api/v1/webhooks/payos` using `POST /confirm-webhook`. Keep both containers running while testing payments.

Owner and admin can also use the `sync-payment` API to check PayOS directly. The backend compares the payment status, order code, payment link ID and exact paid amount before recording a payment. This provides a recovery path when webhook delivery is delayed.

Never put the PayOS keys or ngrok auth token in frontend environment variables.

The backend has no fallback to client-reported payment. Existing manual transfer records remain readable, but new subscription approvals require a verified PayOS payment.
