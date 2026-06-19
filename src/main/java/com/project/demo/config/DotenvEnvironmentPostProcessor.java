package com.project.demo.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 프로젝트 루트의 .env 파일을 읽어 Spring Environment 에 주입한다.
 * (spring-dotenv 가 Spring Boot 4 에서 동작하지 않아 직접 구현)
 *
 * - key=value 형식, # 주석 및 빈 줄 무시, 양끝 따옴표 제거
 * - 최저 우선순위(addLast)로 등록 → 실제 OS 환경변수/CLI 인자가 항상 우선
 * - 값은 로그에 출력하지 않는다(비밀번호 보호)
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = Path.of(".env").toAbsolutePath();
        if (!Files.isRegularFile(envFile)) {
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#")) {
                    continue;
                }
                int eq = s.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = s.substring(0, eq).trim();
                String val = s.substring(eq + 1).trim();
                if (val.length() >= 2
                        && ((val.startsWith("\"") && val.endsWith("\""))
                        || (val.startsWith("'") && val.endsWith("'")))) {
                    val = val.substring(1, val.length() - 1);
                }
                values.put(key, val);
            }
        } catch (IOException e) {
            System.out.println("[dotenv] .env 읽기 실패: " + e.getMessage());
            return;
        }

        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(SOURCE_NAME, values));
            System.out.println("[dotenv] " + envFile + " 에서 " + values.size() + "개 키 로드: " + values.keySet());
        }
    }
}
