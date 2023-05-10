package de.hfu.kafkaprocessors.messages;

import de.hfu.kafkaprocessors.serialization.JSONSerdeCompatible;

public record Vector3(float x, float y, float z) implements JSONSerdeCompatible {

    public static Vector3 zero() {
        return new Vector3(0, 0, 0);
    }
}
