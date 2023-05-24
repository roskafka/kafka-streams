package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record Message(MetaData metadata, PayloadPosition payload) implements JSONSerdeCompatible {
public record Message(MetaData metadata, PayloadTemperature payload) implements JSONSerdeCompatible {
}


