package com.example.authapi.process;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessService {

    private final DataApiClient dataApiClient;
    private final ProcessingLogRepository logs;

    public ProcessService(DataApiClient dataApiClient, ProcessingLogRepository logs) {
        this.dataApiClient = dataApiClient;
        this.logs = logs;
    }

    public ProcessResponse process(UUID userId, String text) {
        String result = dataApiClient.transform(text);
        logs.save(new ProcessingLog(UUID.randomUUID(), userId, text, result, Instant.now()));
        return new ProcessResponse(result);
    }
}
