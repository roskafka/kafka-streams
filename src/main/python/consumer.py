import json

from kafka import KafkaConsumer

consumer = KafkaConsumer('actions', bootstrap_servers='localhost:9092', value_deserializer=lambda m: json.loads(m.decode('utf-8')))
print("Waiting for messages...")
for message in consumer:
    print(message)
