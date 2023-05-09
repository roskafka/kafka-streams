package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record MetaData(String robot, String topic, String type) implements JSONSerdeCompatible {
}
