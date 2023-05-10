package de.hfu.kafkaprocessors;

import de.hfu.kafkaprocessors.messages.Message;
import de.hfu.kafkaprocessors.messages.PayloadPosition;
import de.hfu.kafkaprocessors.messages.Vector3;
import de.hfu.kafkaprocessors.messages.VelocityCommand;
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

import java.util.HashMap;
import java.util.Map;

@Component
public class PositionProcessor {

    private static final String INPUT_TOPIC = "positions";
    
    private static final Logger logger = LoggerFactory.getLogger(PositionProcessor.class);

    private static final double THRESHOLD_DISTANCE = 0.1;

    Map<String, PayloadPosition> latestPositions = new HashMap<>();

    @Autowired
    public void createPositionsStream(final StreamsBuilder builder) {
        // KStream<String, Message> positions = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(),new JSONSerde<>()));
        KStream<String, Message> positions = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), new JSONSerde<>()));
        // on every new position check if the 2 robots are close to each other
        // if so, stop one robot
        // one position contains the position of one robot
        // when the robots are far away from each other, start the stopped robot
        positions
                .map((key, message) -> new KeyValue<>(message.meta().robot(), message.payload()))
                .map((robot, payload) -> {
                    latestPositions.put(robot, payload);
                    logger.info("Received position of robot {} at ({}, {})", robot, payload.x(), payload.y());
                    PayloadPosition otherRobot = getOtherRobot(robot);
                    boolean stop = false;
                    if (otherRobot != null) {
                        stop = stopIfClose(payload.x(), payload.y(), otherRobot.x(), otherRobot.y());
                    }
                    logger.info("Stop: {}", stop);
                    String robotToStop = stop ? "robot1" : null;    // TODO: robot1 is hardcoded
                    return new KeyValue<>(robotToStop, new VelocityCommand(Vector3.zero(), Vector3.zero()));
                }).filter((robot, velocityCommand) -> robot != null)
                .to("_robot1_movementCommand", Produced.with(Serdes.String(), new JSONSerde<>()));  // TODO: robot1 is hardcoded
    }

    private float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private boolean stopIfClose(float x1, float y1, float x2, float y2) {
        return getDistance(x1, y1, x2, y2) < THRESHOLD_DISTANCE;
    }

    private PayloadPosition getOtherRobot(String thisRobot) {
        for (String robot : latestPositions.keySet()) {
            if (!robot.equals(thisRobot)) {
                return latestPositions.get(robot);
            }
        }
        return null;
    }
}
