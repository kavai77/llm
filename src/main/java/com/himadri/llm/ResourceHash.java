package com.himadri.llm;

import com.google.common.hash.Hashing;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ResourceHash {
    public enum Resource {
        INDEX_CSS("/static/index.css");

        private final String path;

        Resource(String path) {
            this.path = path;
        }
    }
    private final Map<Resource, String> resourceHashMap = new EnumMap<>(Resource.class);

    @PostConstruct
    public void init() throws IOException {
        for (Resource resource: Resource.values()) {
            String hash = Hashing.sha256()
                    .hashBytes(IOUtils.resourceToByteArray(resource.path))
                    .toString();
            resourceHashMap.put(resource, hash);
        }
    }

    public String getResourceHash(Resource resource) {
        return resourceHashMap.get(resource);
    }
}
