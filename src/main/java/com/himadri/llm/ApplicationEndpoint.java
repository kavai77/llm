package com.himadri.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ApplicationEndpoint {
    private final ResourceHash resourceHash;

    @GetMapping(value = "/")
    public String index(Model model) {
        model.addAttribute("indexcsshash", resourceHash.getResourceHash(ResourceHash.Resource.INDEX_CSS));
        return "index";
    }

    @GetMapping(value = "/historic.html")
    public String historic(Model model) {
        model.addAttribute("indexcsshash", resourceHash.getResourceHash(ResourceHash.Resource.INDEX_CSS));
        return "historic-question-and-answer";
    }
}
