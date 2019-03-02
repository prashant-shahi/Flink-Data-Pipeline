package com.kredaro.app;


import com.kredaro.app.model.Backup;
import com.kredaro.app.model.InputMessage;
import com.kredaro.app.operator.BackupAggregator;
import com.kredaro.app.operator.InputMessageTimestampAssigner;
import com.kredaro.app.operator.WordsCapitalizer;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

import static com.kredaro.app.connector.Consumers.*;
import static com.kredaro.app.connector.Producers.*;

public class FlinkDataPipeline {

    public static void capitalize() throws Exception {
        String inputTopic = "flink_input";
        String outputTopic = "flink_output";
        String consumerGroup = "";
        String address = "35.200.176.64:9092";

        StreamExecutionEnvironment environment =
          StreamExecutionEnvironment.getExecutionEnvironment();

        FlinkKafkaConsumer011<String> flinkKafkaConsumer =
          createStringConsumerForTopic(inputTopic, address, consumerGroup);
        flinkKafkaConsumer.setStartFromEarliest();

        DataStream<String> stringInputStream =
          environment.addSource(flinkKafkaConsumer);

        FlinkKafkaProducer011<String> flinkKafkaProducer =
        createStringProducer(outputTopic, address);

        stringInputStream
          .map(new WordsCapitalizer())
          .addSink(flinkKafkaProducer);

        environment.execute();
    }

public static void createBackup () throws Exception {
    String inputTopic = "flink_input";
    String outputTopic = "flink_output";
    String consumerGroup = "";
    String kafkaAddress = "35.200.176.64:9092";

    StreamExecutionEnvironment environment =
      StreamExecutionEnvironment.getExecutionEnvironment();

    environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    FlinkKafkaConsumer011<InputMessage> flinkKafkaConsumer =
      createInputMessageConsumer(inputTopic, kafkaAddress, consumerGroup);
    flinkKafkaConsumer.setStartFromEarliest();

    flinkKafkaConsumer
      .assignTimestampsAndWatermarks(new InputMessageTimestampAssigner());
    FlinkKafkaProducer011<Backup> flinkKafkaProducer =
      createBackupProducer(outputTopic, kafkaAddress);

    DataStream<InputMessage> inputMessagesStream =
      environment.addSource(flinkKafkaConsumer);

    inputMessagesStream
      .timeWindowAll(Time.hours(24))
      .aggregate(new BackupAggregator())
      .addSink(flinkKafkaProducer);

    environment.execute();
}

    public static void main(String[] args) throws Exception {
        createBackup();
    }

}
