package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record VelocityCommand(Vector3 linear, Vector3 angular) implements JSONSerdeCompatible {
}
