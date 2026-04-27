package hu.backend.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TaskRequest(String title) {
}