package de.hfu.kafkaprocessors;

import de.hfu.kafkaprocessors.messages.Message;
import de.hfu.kafkaprocessors.serialization.JSONSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PositionProcessor {

        private static final String INPUT_TOPIC = "positions";
        private static final String OUTPUT_TOPIC = "actions";

        private static final Logger logger = LoggerFactory.getLogger(PositionProcessor.class);

        @Autowired
        public void createPositionsStream(final StreamsBuilder builder) {
            KStream<String, Message> positions = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(),new JSONSerde<>()));
            positions
                    .foreach((key, value) -> logger.info("Received message: " + value + " with key: " + key));
        }
}
