package com.himadri.llm.controller;

import com.himadri.llm.db.DatabaseService;
import com.himadri.llm.security.AuthenticationService;
import com.himadri.llm.security.SecurityConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
@PropertySource("file:" + SecurityConfig.SECRETS + "secrets.properties")
public class InferenceService {
    public static final String DATA_PREFIX = "data: ";
    public static final int MAX_TOKENS = 4092;

    @Value("${replicate.bearer.auth}")
    private final String replicateBearerAuth;

    @Value("${system.prompt}")
    private final String systemPrompt;

    private final DatabaseService databaseService;
    private final AuthenticationService authenticationService;

    @SneakyThrows
    public SseEmitter predict(String input) {
        checkArgument(isNotBlank(input));
        checkArgument(input.length() <= 300);
        SseEmitter emitter = new SseEmitter();
        var userId = authenticationService.getUid();
        var numberOfInferences = databaseService.getNumberOfInferences(userId);
        LlmModel model;
        if (numberOfInferences < 5) {
            model = LlmModel.BETTER;
        } else if (numberOfInferences < 20) {
            model = LlmModel.CHEAPER;
        } else {
            closeEmitter(emitter, new AtomicReference<>(),
                    new AtomicReference<>(new IllegalStateException("You have reached the maximum number of questions today. Please come back tomorrow.")));
            return emitter;
        }


        Thread.startVirtualThread(() -> {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(replicateBearerAuth);
            PredictApiRequest.builder().build();

            HttpEntity<PredictApiRequest> entity = new HttpEntity<>(PredictApiRequest.builder()
                    .input(PredictApiInputRequest.builder()
                            .system_prompt(systemPrompt)
                            .prompt(input)
                            .max_tokens(MAX_TOKENS)
                            .build())
                    .build(), headers);
            var predictApiResponse = restTemplate.exchange(model.getUrl(), HttpMethod.POST, entity, PredictApiResponse.class);
            StringBuilder sb = new StringBuilder();
            AtomicReference<String> idReference = new AtomicReference<>();
            AtomicReference<Exception> completedWithError = new AtomicReference<>();
            try (var httpResponse = restTemplate.execute(predictApiResponse.getBody().urls().stream(), HttpMethod.GET,
                    request -> request.getHeaders().add("Accept", "text/event-stream"),
                    response -> {
                        String line;
                        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody()))) {
                            while (!(line = bufferedReader.readLine()).equals("event: done")) {
                                if (line.startsWith(DATA_PREFIX)) {
                                    String data = line.substring(DATA_PREFIX.length());
                                    if (data.isEmpty()) {
                                        if (!sb.isEmpty()) {
                                            sb.append("\n");
                                        }
                                    } else {
                                        sb.append(data);
                                    }
                                    emitter.send(sb.toString());
                                }
                            }
                        } catch (IOException e) {
                            completedWithError.set(e);
                        }
                        var id = databaseService.addInference(userId, model.name(), input, sb.toString());
                        idReference.set(id);
                        return response;
                    })) {
            } finally {
                closeEmitter(emitter, idReference, completedWithError);
            }
        });
        return emitter;
    }

    private static void closeEmitter(SseEmitter emitter, AtomicReference<String> id, AtomicReference<Exception> completedWithError) {
        try {
            if (completedWithError.get() != null) {
                emitter.send(SseEmitter.event().name("error").data(completedWithError.get().getMessage()));
            }
            emitter.send(SseEmitter.event().name("done").data(defaultString(id.get())));
        } catch (IOException e) {
            completedWithError.set(e);
        } finally {
            if (completedWithError.get() != null) {
                emitter.completeWithError(completedWithError.get());
            } else {
                emitter.complete();
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum LlmModel {
        BETTER("https://api.replicate.com/v1/models/meta/meta-llama-3.1-405b-instruct/predictions"),
        CHEAPER("https://api.replicate.com/v1/models/meta/meta-llama-3-70b-instruct/predictions");

        private final String url;
    }

    @Builder
    public record PredictApiRequest(PredictApiInputRequest input) {}
    @Builder(toBuilder = true)
    public record PredictApiInputRequest(String prompt, String system_prompt, int max_tokens) {}
    public record PredictApiResponse(PredictApiUrlsResponse urls) {}
    public record PredictApiUrlsResponse(String cancel, String get, String stream) {}
}
