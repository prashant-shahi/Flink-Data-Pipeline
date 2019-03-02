package com.kredaro.app.operator;

import com.kredaro.app.model.Backup;
import com.kredaro.app.model.InputMessage;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

    public class BackupAggregator implements AggregateFunction<InputMessage, List<InputMessage>, Backup> {
        @Override
        public List<InputMessage> createAccumulator() {
            return new ArrayList<>();
        }

        @Override
        public List<InputMessage> add(InputMessage inputMessage, List<InputMessage> inputMessages) {
            inputMessages.add(inputMessage);
            return inputMessages;
        }

        @Override
        public Backup getResult(List<InputMessage> inputMessages) {
            Backup backup = new Backup(inputMessages, LocalDateTime.now());
                return backup;
        }

        @Override
        public List<InputMessage> merge(List<InputMessage> inputMessages, List<InputMessage> acc1) {
            inputMessages.addAll(acc1);
            return inputMessages;
        }
    }
