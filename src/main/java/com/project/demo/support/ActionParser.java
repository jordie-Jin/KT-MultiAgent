package com.project.demo.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;

/**
 * log_analysis.action(TEXT) → responsePlan(string[]) 변환 (명세 §11.3).
 * 하위호환 파싱: 값이 '['로 시작하면 JSON 배열, 아니면 줄바꿈(\n) split.
 */
public final class ActionParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ActionParser() {
    }

    public static List<String> parse(String action) {
        if (action == null || action.isBlank()) {
            return List.of();
        }
        String t = action.trim();
        if (t.startsWith("[")) {
            try {
                List<String> parsed = MAPPER.readValue(t, new TypeReference<List<String>>() {
                });
                return parsed.stream()
                        .filter(s -> s != null && !s.isBlank())
                        .map(String::trim)
                        .toList();
            } catch (Exception ignored) {
                // JSON 파싱 실패 시 줄바꿈 분리로 폴백
            }
        }
        return Arrays.stream(t.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
