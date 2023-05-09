package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record Message(MetaData meta, PayloadPosition payload) implements JSONSerdeCompatible {
}
