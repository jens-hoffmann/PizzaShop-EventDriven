# Kafka topics are created by the kafka-init service in compose.yaml.
# This script is kept for manual topic creation against a running broker.
set -euo pipefail

BOOTSTRAP="${BOOTSTRAP:-localhost:9092}"

/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic catalog.events.v1 --partitions 1 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic order.events.v1 --partitions 3 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic payment.events.v1 --partitions 3 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic kitchen.events.v1 --partitions 3 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic order.events.v1.DLT --partitions 1 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic payment.events.v1.DLT --partitions 1 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic kitchen.events.v1.DLT --partitions 1 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists --topic catalog.events.v1.DLT --partitions 1 --replication-factor 1

/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --list
