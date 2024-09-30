package com.himadri.llm;

import com.himadri.llm.controller.InferenceService;
import com.himadri.llm.db.DatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LlmEndpoint {
    private final DatabaseService databaseService;
    private final InferenceService inferenceService;

    @GetMapping(value = "/login")
    public void login() {
        databaseService.addLogin();
    }

    @GetMapping(value = "/predict", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter predict(@RequestParam("input") String input) {
        return inferenceService.predict(input);
    }

    @GetMapping(value = "/inference/{id}")
    public DatabaseService.InferenceResponse getInference(@PathVariable("id") String id) {
        return databaseService.getInference(id);
    }
}
