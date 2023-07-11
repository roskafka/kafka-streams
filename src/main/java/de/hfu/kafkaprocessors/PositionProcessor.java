package de.hfu.kafkaprocessors;

import de.hfu.Color;
import de.hfu.Pose;
import de.hfu.Twist;
import de.hfu.Vector3;
import de.hfu.kafkaprocessors.customprocessors.DistanceProcessor;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class PositionProcessor {

    private static final String INPUT_TOPIC = "roskafka-positions";

    private static final String COLOR_OUTPUT_TOPIC = "kafkaros-backgroundcolor";

    private static final String MOVEMENT_OUTPUT_TOPIC = "kafkaros_klaus_movementCommands";      // TODO: klaus is hardcoded here

    private static final Logger logger = LoggerFactory.getLogger(PositionProcessor.class);

    private static final double THRESHOLD_DISTANCE_TOO_CLOSE = 3;
    private static final double THRESHOLD_DISTANCE_CLOSE = 4;


    private static final Color backgroundColorTooClose = new Color(255, 0, 0);
    private static final Color backgroundColorClose = new Color(250, 250, 0);
    private static final Color backgroundColorFar = new Color(0, 255, 0);
    private static final Twist movementCommandCircle = new Twist(new Vector3(1., 0., 0.), new Vector3(0., 0., 1.));
    private static final Twist movementCommandStop = new Twist(new Vector3(0., 0., 0.), new Vector3(0., 0., 0.));

    private Color currentBackgroundColor = null;
    private boolean robot1Stopped = false;

    @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
    String schemaRegistryUrl;

    @Produces
    public Topology createPositionsStream() {

        StreamsBuilder builder = new StreamsBuilder();

        Serde<Color> colorSerde = new SpecificAvroSerde<>();
        Serde<Twist> twistSerde = new SpecificAvroSerde<>();
        Serde<Pose> poseSerde = new SpecificAvroSerde<>();

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
        colorSerde.configure(serdeConfig, false);
        twistSerde.configure(serdeConfig, false);
        poseSerde.configure(serdeConfig, false);

        // create store
        StoreBuilder<KeyValueStore<String, Pose>> storeBuilder = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore("turtleBotPositions"),
                Serdes.String(),
                poseSerde);

        // register store
        builder.addStateStore(storeBuilder);

        KStream<String, Pose> positions = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), poseSerde));

        KStream<String, Float> distances = positions
                .process(DistanceProcessor::new, "turtleBotPositions");


        distances
                .filter((key, distance) -> distance < THRESHOLD_DISTANCE_TOO_CLOSE && !robot1Stopped)
                .peek((key, distance) -> robot1Stopped = true)
                .mapValues(distance -> movementCommandStop)
                .peek((key, distance) -> logger.info("Robot 1 stopped"))
                .to(MOVEMENT_OUTPUT_TOPIC, Produced.with(Serdes.String(), twistSerde));

        // send drive command, when distance is far enough
        distances
                .filter((key, distance) -> distance > THRESHOLD_DISTANCE_TOO_CLOSE && robot1Stopped)
                .peek((key, distance) -> robot1Stopped = false)
                .mapValues(distance ->  movementCommandCircle)
                .peek((key, distance) -> logger.info("Robot 1 started"))
                .to(MOVEMENT_OUTPUT_TOPIC, Produced.with(Serdes.String(), twistSerde));

        // output color based on distance
        distances
                .mapValues(distance -> {
                    Color backgroundColorCommand;
                    if (distance < THRESHOLD_DISTANCE_TOO_CLOSE) {
                        backgroundColorCommand = backgroundColorTooClose;
                    } else if (distance < THRESHOLD_DISTANCE_CLOSE) {
                        backgroundColorCommand = backgroundColorClose;
                    } else {
                        backgroundColorCommand = backgroundColorFar;
                    }
                    return backgroundColorCommand;
                })
                .filter((key, backgroundColorCommand) -> !backgroundColorCommand.equals(currentBackgroundColor))
                .mapValues(backgroundColorCommand -> {
                    currentBackgroundColor = backgroundColorCommand;
                    return backgroundColorCommand;
                })
                .peek((key, backgroundColorCommand) -> logger.info("Sending background color command: {}", backgroundColorCommand))

                .to(COLOR_OUTPUT_TOPIC, Produced.with(Serdes.String(), colorSerde));

        return builder.build();
    }
}
