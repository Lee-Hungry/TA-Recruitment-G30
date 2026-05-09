package com.group30.tarecruitment.ai;

public record AiSettings(
        String apiKey,
        String baseUrl,
        String model,
        int connectTimeoutSeconds,
        int requestTimeoutSeconds
) {
}
