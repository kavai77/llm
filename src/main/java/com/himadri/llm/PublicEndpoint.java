package com.himadri.llm;

import com.himadri.llm.db.DatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicEndpoint {
    private final DatabaseService databaseService;

    @GetMapping(value = "/inference/{id}")
    public DatabaseService.InferenceResponse getInference(@PathVariable("id") String id) {
        return databaseService.getInference(id);
    }
}
