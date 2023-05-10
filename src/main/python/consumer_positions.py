import json
from datetime import datetime

from kafka import KafkaConsumer

#consumer = KafkaConsumer('actions', bootstrap_servers='localhost:9092', value_deserializer=lambda m: json.loads(m.decode('utf-8')))
consumer = KafkaConsumer('_robot1_movementCommand', bootstrap_servers='localhost:9092', group_id='positions_logger')
print("Waiting for messages...")
for message in consumer:
    print("Received message ts: ", datetime.now())
    print(message)
