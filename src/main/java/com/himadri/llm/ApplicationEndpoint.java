package com.himadri.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ApplicationEndpoint {
    private final ResourceHash resourceHash;

    @GetMapping(value = "/")
    public String index(Model model) {
        return page(model, "index");
    }

    @GetMapping(value = "/{page}.html")
    public String page(Model model, @PathVariable String page) {
        resourceHash.getResources().forEach(resource -> model.addAttribute(resource.name(), resource.hash()));
        return page;
    }
}
