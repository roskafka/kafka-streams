package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record PayloadPosition(float x, float y, float theta, float linear_velocity, float angular_velocity) implements JSONSerdeCompatible {
}
