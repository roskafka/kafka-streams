import json

from kafka import KafkaProducer

producer = KafkaProducer(bootstrap_servers='localhost:29092', value_serializer=lambda v: json.dumps(v).encode('utf-8'))

for i in range(10):
    data = {
        "_t": "position",
        "x": i,
        "y": 200
    }
    producer.send('positions', key=f"robot-{i % 2}".encode("utf-8"), value=data)

producer.flush()
