import json

from kafka import KafkaConsumer

consumer = KafkaConsumer('actions', bootstrap_servers='localhost:29092')
print("Waiting for messages...")
for message in consumer:
    print(message)
