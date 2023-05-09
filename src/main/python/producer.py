import json

from kafka import KafkaProducer

from models import Message, PayloadPosition, MetaData

producer = KafkaProducer(bootstrap_servers='localhost:9092', value_serializer=lambda v: v.json().encode('utf-8'))

for i in range(10):
    data = Message(
        payload=PayloadPosition(x=i, y=i, theta=i, linear_velocity=i, angular_velocity=i),
        meta=MetaData(robot=f"robot-{i % 2}", topic="positions", type="message")
    )
    producer.send('positions', key=f"robot-{i % 2}".encode("utf-8"), value=data)

producer.flush()
