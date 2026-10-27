package com.example.authapi.process;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_log")
public class ProcessingLog {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "input_text", nullable = false, length = 10000)
    private String inputText;

    @Column(name = "output_text", nullable = false, length = 10000)
    private String outputText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ProcessingLog() {
    }

    public ProcessingLog(UUID id, UUID userId, String inputText, String outputText, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.inputText = inputText;
        this.outputText = outputText;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getInputText() {
        return inputText;
    }

    public String getOutputText() {
        return outputText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
