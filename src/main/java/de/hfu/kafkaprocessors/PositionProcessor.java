package de.hfu.kafkaprocessors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PositionProcessor {

        private static final String INPUT_TOPIC = "positions";
        private static final String OUTPUT_TOPIC = "actions";

        @Autowired
        public void createPositionsStream(final StreamsBuilder builder) {
            KStream<String, String> positions = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
            positions
                    .map((key, value) -> new KeyValue<>(key, value + "_processed"))
                    .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
        }
}
