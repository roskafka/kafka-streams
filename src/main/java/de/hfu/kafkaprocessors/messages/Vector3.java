package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record Vector3(float x, float y, float z) implements JSONSerdeCompatible {
}
