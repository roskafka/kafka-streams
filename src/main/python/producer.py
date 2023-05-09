import json
import time
from datetime import datetime

from kafka import KafkaProducer

from models import Message, PayloadPosition, MetaData

producer = KafkaProducer(bootstrap_servers='localhost:9092', value_serializer=lambda v: v.json().encode('utf-8'))

data = Message(
    payload=PayloadPosition(x=10, y=0, theta=0, linear_velocity=1, angular_velocity=1),
    meta=MetaData(robot=f"robot-1", topic="positions", type="message")
)
producer.send('positions', key=f"robot-1".encode("utf-8"), value=data)

data = Message(
    payload=PayloadPosition(x=0, y=0, theta=0, linear_velocity=1, angular_velocity=1),
    meta=MetaData(robot=f"robot-2", topic="positions", type="message")
)
producer.send('positions', key=f"robot-2".encode("utf-8"), value=data)

time.sleep(2)

data = Message(
    payload=PayloadPosition(x=4.555, y=0, theta=0, linear_velocity=1, angular_velocity=1),
    meta=MetaData(robot=f"robot-1", topic="positions", type="message")
)
producer.send('positions', key=f"robot-1".encode("utf-8"), value=data)

data = Message(
    payload=PayloadPosition(x=4.5, y=0, theta=0, linear_velocity=1, angular_velocity=1),
    meta=MetaData(robot=f"robot-2", topic="positions", type="message")
)
print("Sending message ts: ", datetime.now())
producer.send('positions', key=f"robot-2".encode("utf-8"), value=data)


producer.flush()
