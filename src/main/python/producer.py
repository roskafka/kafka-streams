import io
import time
from datetime import datetime

import fastavro.schema
from confluent_kafka.schema_registry.avro import AvroDeserializer, AvroSerializer
from confluent_kafka import DeserializingConsumer, SerializingProducer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.serialization import StringDeserializer, StringSerializer
from confluent_kafka.avro import AvroProducer
from confluent_kafka import avro

schema_registry_client = SchemaRegistryClient({"url": "http://localhost:8081"})

schema_path = "Greetings.avsc"
schema = avro.load(schema_path)
# schema = schema_registry_client.get_latest_version("avroTestTopic-value")


# producer with schema-registry and avro
producer = AvroProducer({
    'bootstrap.servers': 'localhost:9092',
    'schema.registry.url': 'http://localhost:8081',
}, default_value_schema=schema, default_key_schema=schema)


def send_position(x, y, robot):
    # serialize avro Greetings
    data = {
        "name": "Klaus",
        "info": "Hello world",
        "age": 42
    }
    producer.produce(topic="avroTestTopic", key=data, value=data)
    print(f"Sent position {robot=} {x=} {y=} now={datetime.now()}")


def send_positions(x1, y1, x2, y2):
    send_position(x1, y1, 1)
    send_position(x2, y2, 2)


time.sleep(1)
print("Start sending")
for i in range(3):
    print(f"Round: {i}")
    send_positions(10, 10, 20, 20)
    time.sleep(5)
    send_positions(10, 10, 10, 11.5)
    time.sleep(5)
    send_positions(10, 10, 10, 10.5)
    time.sleep(5)

producer.flush()
