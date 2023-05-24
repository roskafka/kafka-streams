package de.hfu.kafkaprocessors.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hfu.kafkaprocessors.messages.Message;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class JSONSerde<T extends JSONSerdeCompatible> implements Serializer<T>, Deserializer<T>, Serde<T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {}

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        byte[] type = headers.lastHeader("type").value();
        String typeString = new String(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(data);
            String type = node.get("metadata").get("type").asText();
            switch (type) {
                case "turtlesim/msg/Pose":
                    return (T) OBJECT_MAPPER.convertValue(node, Message.class);
                default:
                    throw new SerializationException("Unknown type: " + type);
            }

        } catch (final IOException e) {
            e.printStackTrace();
            System.err.println("Error deserializing JSON message ");
            throw new SerializationException(e);
        }
    }

    @Override
    public byte[] serialize(final String topic, final T data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (final Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    @Override
    public void close() {}

    @Override
    public Serializer<T> serializer() {
        return this;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this;
    }
}
