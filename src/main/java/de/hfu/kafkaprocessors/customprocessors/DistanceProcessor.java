package de.hfu.kafkaprocessors.customprocessors;

import de.hfu.Pose;
import de.hfu.kafkaprocessors.PositionProcessor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceProcessor implements Processor<String, Pose, String, Float> {

    private ProcessorContext<String, Float> context;
    private KeyValueStore<String, Pose> positionStore;

    private static final Logger logger = LoggerFactory.getLogger(PositionProcessor.class);

    @Override
    public void init(ProcessorContext<String, Float> context) {

        Processor.super.init(context);
        this.context = context;

        positionStore = context.getStateStore("turtleBotPositions");
    }


    @Override
    public void process(Record<String, Pose> record) {

        positionStore.put(record.key(), record.value());
        // logger.info("Received position of robot {} at ({}, {})", record.key(), record.value().getX(), record.value().getY());
        Pose otherRobot = getOtherRobot(record.key());

        float distance = Float.MAX_VALUE;
        if (otherRobot != null) {
            distance = getDistance(record.value().getX(), record.value().getY(), otherRobot.getX(), otherRobot.getY());
        }
        logger.debug("distance={}", distance);

        Record<String, Float> distanceRecord = new Record<>(record.key(), distance,  System.currentTimeMillis());
        context.forward(distanceRecord);
    }

    private float getDistance(float x1, float y1, float x2, float y2) {

        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private Pose getOtherRobot(String thisRobot) {

        KeyValueIterator<String, Pose> it = positionStore.all();

        while (it.hasNext()) {
            KeyValue<String, Pose> robotRecord = it.next();
            String robot = robotRecord.key;

            if (!robot.equals(thisRobot)) {
                return positionStore.get(robot);
            }
        }

        return null;
    }


    @Override
    public void close() {
        // Processor.super.close();
    }
}
