import json
from datetime import datetime

from kafka import KafkaConsumer


consumer = KafkaConsumer('_backgroundcolor', bootstrap_servers='localhost:9092')
print("Waiting for messages...")
for message in consumer:
    print("Received message ts: ", datetime.now())
    print(message)
