package io.codebottle.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.codebottle.api.CodeBottleAPI;

public class Category extends AbstractEntity {
    private @JsonProperty(required = true) int id;
    private @JsonProperty(required = true) String name;

    public Category(CodeBottleAPI context, JsonNode data) {
        super(context, data);
    }

    public String getName() {
        return name;
    }

    @Override
    public Category update(JsonNode data) {
        this.name = data.path("name").asText(name);

        return this;
    }
}
