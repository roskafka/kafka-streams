package de.hfu.kafkaprocessors.serialization;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.hfu.kafkaprocessors.messages.Message;
import de.hfu.kafkaprocessors.messages.VelocityCommand;


@SuppressWarnings("DefaultAnnotationParam") // being explicit for the example
// type property in meta.type

public interface JSONSerdeCompatible {

}
