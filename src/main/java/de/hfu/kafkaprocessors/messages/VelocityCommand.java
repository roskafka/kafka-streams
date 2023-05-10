package de.hfu.kafkaprocessors.messages;

public record VelocityCommand(Vector3 linear, Vector3 angular) implements Payload {
}
