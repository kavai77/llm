package com.himadri.llm;

import com.google.common.hash.Hashing;
import lombok.Getter;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Getter
public class ResourceHash {
    private final List<Resource> resources = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Arrays.stream(resolver.getResources("classpath:/static/**")).forEach(resource -> {
            try {
                String hash = Hashing.sha256()
                        .hashBytes(resource.getContentAsByteArray())
                        .toString();
                resources.add(new Resource(resource.getFilename().toLowerCase().replace(".", "_"), hash));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    record Resource(String name, String hash) {}
}
