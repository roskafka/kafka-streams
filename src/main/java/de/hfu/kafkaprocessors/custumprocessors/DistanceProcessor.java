package de.hfu.kafkaprocessors.custumprocessors;

import de.hfu.kafkaprocessors.PositionProcessor;
import de.hfu.kafkaprocessors.messages.Message;
import de.hfu.kafkaprocessors.messages.PayloadPosition;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceProcessor implements Processor<String, Message, String, Float> {

    private ProcessorContext context;
    private KeyValueStore<String, Message> positionStore;

    private static final Logger logger = LoggerFactory.getLogger(PositionProcessor.class);

    @Override
    public void init(ProcessorContext<String, Float> context) {

        Processor.super.init(context);
        this.context = context;

        positionStore = context.getStateStore("turtleBotPositions");
    }


    @Override
    public void process(Record<String, Message> record) {

        System.out.println(record.value());
        System.out.println("Headers: " + record.headers());

        positionStore.put(record.key(), record.value());
        logger.info("Received position of robot {} at ({}, {})", record.key(), record.value().payload().x(), record.value().payload().y());
        PayloadPosition otherRobot = getOtherRobot(record.key());

        float distance = Float.MAX_VALUE;
        if (otherRobot != null) {
            distance = getDistance(record.value().payload().x(), record.value().payload().y(), otherRobot.x(), otherRobot.y());
        }
        logger.info("distance: {}", distance);

        Record<String, Float> distanceRecord = new Record<>("", distance,  System.currentTimeMillis());
        context.forward(distanceRecord);
    }

    private float getDistance(float x1, float y1, float x2, float y2) {

        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private PayloadPosition getOtherRobot(String thisRobot) {

        KeyValueIterator<String, Message> it = positionStore.all();

        while (it.hasNext()) {


            positionStore.delete("robot-2");
            KeyValue<String, Message> robotRecord = it.next();
            logger.info(robotRecord.toString());


            String robot = robotRecord.key;

            if (!robot.equals(thisRobot)) {
                return positionStore.get(robot).payload();
            }
        }

        return null;
    }


    @Override
    public void close() {
        // Processor.super.close();
    }
}
