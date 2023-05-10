package de.hfu.kafkaprocessors.messages;


public record BackgroundColorCommand(int r, int g, int b) implements Payload {
}
