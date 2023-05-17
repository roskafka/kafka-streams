package de.hfu.kafkaprocessors;

import de.hfu.kafkaprocessors.custumprocessors.DistanceProcessor;
import de.hfu.kafkaprocessors.messages.*;
import de.hfu.kafkaprocessors.serialization.JSONSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PositionProcessor {

    private static final String INPUT_TOPIC = "positions";

    private static final String COLOR_OUTPUT_TOPIC = "_backgroundcolor";

    private static final String MOVEMENT_OUTPUT_TOPIC = "_klaus_movementCommands";      // TODO: klaus is hardcoded here

    private static final Logger logger = LoggerFactory.getLogger(PositionProcessor.class);

    private static final double THRESHOLD_DISTANCE_TOO_CLOSE = 3;
    private static final double THRESHOLD_DISTANCE_CLOSE = 4;


    private static final BackgroundColorCommand backgroundColorTooClose = new BackgroundColorCommand(255, 0, 0);
    private static final BackgroundColorCommand backgroundColorClose = new BackgroundColorCommand(250, 250, 0);
    private static final BackgroundColorCommand backgroundColorFar = new BackgroundColorCommand(0, 255, 0);
    private static final VelocityCommand movementCommandCircle = new VelocityCommand(new Vector3(1, 0, 0), new Vector3(0, 0, 1));
    private static final VelocityCommand movementCommandStop = new VelocityCommand(Vector3.zero(), Vector3.zero());

    private BackgroundColorCommand currentBackgroundColor = null;
    private boolean robot1Stopped = false;

    @Autowired
    public void createPositionsStream(final StreamsBuilder builder) {

        // create store
        StoreBuilder storeBuilder = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore("turtleBotPositions"),
                Serdes.String(),
                new JSONSerde());
        // register store
        builder.addStateStore(storeBuilder);

        KStream<String, Message> positions = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), new JSONSerde<>()));

        KStream<String, Float> distances = positions
                .process(DistanceProcessor::new, "turtleBotPositions");


        distances
                .filter((key, distance) -> distance < THRESHOLD_DISTANCE_TOO_CLOSE && !robot1Stopped)
                .peek((key, distance) -> robot1Stopped = true)
                .mapValues(distance -> new MessageOut(new MetaData("klaus", "klaus", "geometry/msg/Twist"), movementCommandStop))
                .peek((key, distance) -> logger.info("Robot 1 stopped"))
                .to(MOVEMENT_OUTPUT_TOPIC, Produced.with(Serdes.String(), new JSONSerde<>()));

        // send drive command, when distance is far enough
        distances
                .filter((key, distance) -> distance > THRESHOLD_DISTANCE_TOO_CLOSE && robot1Stopped)
                .peek((key, distance) -> robot1Stopped = false)
                .mapValues(distance -> new MessageOut(new MetaData("klaus", "klaus", "geometry/msg/Twist"), movementCommandCircle))
                .peek((key, distance) -> logger.info("Robot 1 started"))
                .to(MOVEMENT_OUTPUT_TOPIC, Produced.with(Serdes.String(), new JSONSerde<>()));

        // output color based on distance
        distances
                .mapValues(distance -> {
                    BackgroundColorCommand backgroundColorCommand;
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
                    return new MessageOut(new MetaData("klaus", "klaus", "color"), backgroundColorCommand);
                })
                .peek((key, backgroundColorCommand) -> logger.info("Sending background color command: {}", backgroundColorCommand))

                .to(COLOR_OUTPUT_TOPIC, Produced.with(Serdes.String(), new JSONSerde<>()));
    }
}
