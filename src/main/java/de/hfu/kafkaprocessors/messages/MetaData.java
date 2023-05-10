package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record MetaData(String mapping, String source, String type) implements JSONSerdeCompatible {
}
