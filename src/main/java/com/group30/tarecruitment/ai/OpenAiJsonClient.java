package com.group30.tarecruitment.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenAiJsonClient {

    private final AiSettings settings;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiJsonClient(AiSettings settings) {
        this(settings, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(settings.connectTimeoutSeconds()))
                .build(), new ObjectMapper());
    }

    OpenAiJsonClient(AiSettings settings, HttpClient httpClient, ObjectMapper objectMapper) {
        this.settings = settings;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode requestJsonObject(String systemPrompt, String userPrompt) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", settings.model());
        payload.put("temperature", 0.2);
        payload.set("response_format", responseFormatNode());
        payload.set("messages", messagesNode(systemPrompt, userPrompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl(settings.baseUrl()) + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(settings.requestTimeoutSeconds()))
                .header("Authorization", "Bearer " + settings.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI request failed with status " + response.statusCode() + ": " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                throw new IllegalStateException("AI response did not contain content.");
            }
            return objectMapper.readTree(stripMarkdownFence(content));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse AI response.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI request was interrupted.", e);
        }
    }

    private JsonNode responseFormatNode() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "json_object");
        return node;
    }

    private ArrayNode messagesNode(String systemPrompt, String userPrompt) {
        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(messageNode("system", systemPrompt));
        messages.add(messageNode("user", userPrompt));
        return messages;
    }

    private JsonNode messageNode(String role, String content) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String stripMarkdownFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineBreak = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineBreak < 0 || lastFence <= firstLineBreak) {
            return trimmed;
        }
        return trimmed.substring(firstLineBreak + 1, lastFence).trim();
    }
}
