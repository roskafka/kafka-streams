package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record MessageOut(MetaData metadata, Payload payload) implements JSONSerdeCompatible {
}
