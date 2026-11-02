#!/usr/bin/env bash
set -euo pipefail

CATALOG_URL="${CATALOG_URL:-http://localhost:8081}"
ORDER_URL="${ORDER_URL:-http://localhost:8082}"
KITCHEN_URL="${KITCHEN_URL:-http://localhost:8083}"
PAYMENT_URL="${PAYMENT_URL:-http://localhost:8084}"
NOTIFICATION_URL="${NOTIFICATION_URL:-http://localhost:8085}"

MARGHERITA_ID="11111111-1111-1111-1111-111111111111"
CUSTOMER_ID="smoke-$(date +%s)"
IDEMPOTENCY_KEY="smoke-key-$(date +%s)-$RANDOM"

json() {
  python3 -c "import sys,json; data=json.load(sys.stdin); print($1)"
}

echo "Checking service health..."
for url in "$CATALOG_URL" "$ORDER_URL" "$KITCHEN_URL" "$PAYMENT_URL" "$NOTIFICATION_URL"; do
  curl -fsS "$url/actuator/health" >/dev/null
  echo "  ok $url"
done

echo "Creating order for customer $CUSTOMER_ID..."
ORDER_RESPONSE=$(curl -fsS -X POST "$ORDER_URL/api/v1/orders" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -d "{
    \"customerId\": \"$CUSTOMER_ID\",
    \"items\": [{\"pizzaId\": \"$MARGHERITA_ID\", \"size\": \"LARGE\", \"quantity\": 2}],
    \"deliveryAddress\": {\"street\": \"Main Street 1\", \"city\": \"Pizza Town\", \"postalCode\": \"12345\"}
  }")

ORDER_ID=$(echo "$ORDER_RESPONSE" | json 'data["id"]')
echo "  order $ORDER_ID"

echo "Replaying the same idempotency key..."
REPLAY_RESPONSE=$(curl -fsS -X POST "$ORDER_URL/api/v1/orders" \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -d "{
    \"customerId\": \"$CUSTOMER_ID\",
    \"items\": [{\"pizzaId\": \"$MARGHERITA_ID\", \"size\": \"LARGE\", \"quantity\": 2}],
    \"deliveryAddress\": {\"street\": \"Main Street 1\", \"city\": \"Pizza Town\", \"postalCode\": \"12345\"}
  }")
REPLAY_ID=$(echo "$REPLAY_RESPONSE" | json 'data["id"]')
if [ "$REPLAY_ID" != "$ORDER_ID" ]; then
  echo "Idempotency replay returned a different order: $REPLAY_ID" >&2
  exit 1
fi
echo "  replay returned the original order"

echo "Waiting for the kitchen..."
STATUS=""
for _ in $(seq 1 40); do
  STATUS=$(curl -fsS "$ORDER_URL/api/v1/orders/$ORDER_ID" | json 'data["status"]')
  echo "  status: $STATUS"
  if [ "$STATUS" = "READY" ]; then
    break
  fi
  if [ "$STATUS" = "PAYMENT_FAILED" ] || [ "$STATUS" = "CANCELLED" ]; then
    echo "Order did not reach READY (status $STATUS)" >&2
    exit 1
  fi
  sleep 1
done

if [ "$STATUS" != "READY" ]; then
  echo "Timed out waiting for READY (last status $STATUS)" >&2
  exit 1
fi

HISTORY_STATUSES=$(curl -fsS "$ORDER_URL/api/v1/orders/$ORDER_ID/history" | json '",".join(item["status"] for item in data)')
echo "  history: $HISTORY_STATUSES"
case "$HISTORY_STATUSES" in
  *PLACED*PAID*CONFIRMED*PREPARING*READY*) ;;
  *)
    echo "Unexpected history: $HISTORY_STATUSES" >&2
    exit 1
    ;;
esac

NOTIFICATION_COUNT=$(curl -fsS "$NOTIFICATION_URL/api/v1/notifications?customerId=$CUSTOMER_ID" | json 'data["totalElements"]')
echo "  notifications: $NOTIFICATION_COUNT"
if [ "$NOTIFICATION_COUNT" -lt 5 ]; then
  echo "Expected at least 5 notifications" >&2
  exit 1
fi

echo "Completing the order..."
FINAL_STATUS=$(curl -fsS -X POST "$ORDER_URL/api/v1/admin/orders/$ORDER_ID/complete" | json 'data["status"]')
if [ "$FINAL_STATUS" != "COMPLETED" ]; then
  echo "Expected COMPLETED, got $FINAL_STATUS" >&2
  exit 1
fi
echo "  status: $FINAL_STATUS"

echo "Smoke test passed."
