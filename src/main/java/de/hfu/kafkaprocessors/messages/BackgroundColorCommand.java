package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record BackgroundColorCommand(int r, int g, int b) implements JSONSerdeCompatible {
}
