package io.codebottle.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.codebottle.api.CodeBottleAPI;

public abstract class AbstractEntity {
    protected final CodeBottleAPI context;

    protected final @JsonProperty(required = true) String id;

    protected AbstractEntity(CodeBottleAPI context, JsonNode data) {
        this.context = context;
        this.id = data.get("id").asText();

        update(data);
    }

    public abstract AbstractEntity update(JsonNode data);

    public AbstractEntity(CodeBottleAPI context, int id) {
        this.context = context;

        this.id = String.valueOf(id);
    }

    public String getID() {
        return id;
    }

    public CodeBottleAPI getContext() {
        return context;
    }
}
