package io.codebottle.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.codebottle.api.CodeBottle;

public abstract class AbstractEntity {
    protected final CodeBottle context;

    protected final @JsonProperty(required = true) String id;

    protected AbstractEntity(CodeBottle context, JsonNode data) {
        this.context = context;
        this.id = data.get("id").asText();

        update(data);
    }

    public AbstractEntity(CodeBottle context, int id) {
        this.context = context;

        this.id = String.valueOf(id);
    }

    public String getID() {
        return id;
    }

    public CodeBottle getContext() {
        return context;
    }

    public abstract AbstractEntity update(JsonNode data);
}
