package io.codebottle.api.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.codebottle.api.CodeBottleAPI;
import io.codebottle.api.rest.CodeBottleRequest;
import io.codebottle.api.rest.Endpoint;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Snippet extends AbstractEntity {
    private final List<Revision> revisions;

    private @JsonProperty(required = true) String title;
    private @JsonProperty @Nullable String description;
    private @JsonProperty(required = true) String code;
    private @JsonProperty(required = true) int views;
    private @JsonProperty(required = true) Language language;
    private @JsonProperty(required = true) Category category;
    private @JsonProperty(required = true) int votes;
    private @JsonProperty(required = true) String username;
    private @JsonProperty(required = true) Instant createdAt;
    private @JsonProperty(required = true) Instant updatedAt;

    public Snippet(CodeBottleAPI context, JsonNode data) {
        super(context, data);

        revisions = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public String getCode() {
        return code;
    }

    public int getViews() {
        return views;
    }

    public Language getLanguage() {
        return language;
    }

    public Category getCategory() {
        return category;
    }

    public int getVotes() {
        return votes;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public Snippet update(JsonNode data) {
        this.title = data.path("title").asText(title);
        this.description = data.path("description").asText(description);
        this.code = data.path("code").asText(code);
        this.views = data.path("views").asInt(views);
        this.language = context.getLanguageByID(data.path("language").path("id").asText(null))
                .orElseGet(() -> new Language(context, data.path("language")))
                .update(data.path("language"));
        this.category = context.getCategoryByID(data.path("category").path("id").asText(null))
                .orElseGet(() -> new Category(context, data.path("category")))
                .update(data.path("category"));
        this.votes = data.path("votes").asInt(votes);
        this.username = data.path("username").asText(username);
        this.createdAt = Optional.ofNullable(data.path("createdAt").asText(null))
                .map(Instant::parse)
                .orElse(createdAt);
        this.updatedAt = Optional.ofNullable(data.path("updatedAt").asText(null))
                .map(Instant::parse)
                .orElse(updatedAt);

        return this;
    }

    public Optional<Revision> getRevisionByID(int id) throws IndexOutOfBoundsException {
        return Optional.ofNullable(revisions.get(id));
    }

    public Collection<Revision> getRevisions() {
        return Collections.unmodifiableCollection(revisions);
    }

    public CompletableFuture<Revision> requestRevision(int id) {
        return new CodeBottleRequest<Revision>(context)
                .to(Endpoint.SNIPPET_REVISION_SPECIFIC, this.id, id)
                .makeGET()
                .then(data -> {
                    synchronized (revisions) {
                        if (revisions.size() >= id) {
                            // existing revision
                            final Revision revision = revisions.get(id);

                            return revision.update(data);
                        } else {
                            // nonexisting revision
                            final Revision revision = new Revision(context, data, id);

                            revisions.set(id, revision);

                            return revision;
                        }
                    }
                });
    }

    public CompletableFuture<List<Revision>> requestRevisions() {
        return new CodeBottleRequest<List<Revision>>(context)
                .to(Endpoint.SNIPPET_REVISIONS, id)
                .makeGET()
                .then(data -> {
                    synchronized (revisions) {
                        int i = -1;

                        try {
                            for (i = 0; i < revisions.size(); i++) {
                                final Revision revision = revisions.get(i);
                                final JsonNode revNode = data.path(i);

                                if (revNode.isMissingNode())
                                    continue;

                                revision.update(revNode);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            // when this occurrs, an index that didn't exist in cache before was reached

                            // this will never happen
                            if (i == -1) throw new AssertionError("something fucked up happened to your jvm");
                        }

                        while (i < data.size()) {
                            revisions.add(new Revision(context, data.get(i), i));

                            i++;
                        }

                        return Collections.unmodifiableList(revisions);
                    }
                });
    }

    public static class Revision extends AbstractEntity implements Comparable<Revision> {
        public static final Comparator<Revision> REVISION_COMPARATOR = Comparator.comparingInt(entity -> Integer.parseInt(entity.getID()));

        private @JsonProperty(required = true) String title;
        private @JsonProperty @Nullable String description;
        private @JsonProperty(required = true) String code;
        private @JsonProperty(required = true) Language language;
        private @JsonProperty(required = true) Category category;
        private @JsonProperty(required = true) String author;
        private @JsonProperty(required = true) String explanation;
        private @JsonProperty(required = true) Instant createdAt;

        protected Revision(CodeBottleAPI context, JsonNode data, int index) {
            super(context, index);
        }

        public String getTitle() {
            return title;
        }

        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        public String getCode() {
            return code;
        }

        public Language getLanguage() {
            return language;
        }

        public Category getCategory() {
            return category;
        }

        public String getAuthor() {
            return author;
        }

        public String getExplanation() {
            return explanation;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        @Override
        public Revision update(JsonNode data) {
            this.title = data.path("title").asText(title);
            this.description = data.path("description").asText(description);
            this.code = data.path("code").asText(code);
            this.language = context.getLanguageByID(data.path("language").path("id").asText(null))
                    .orElseGet(() -> new Language(context, data.path("language")))
                    .update(data.path("language"));
            this.category = context.getCategoryByID(data.path("category").path("id").asText(null))
                    .orElseGet(() -> new Category(context, data.path("category")))
                    .update(data.path("category"));
            this.author = data.path("author").asText(author);
            this.explanation = data.path("explanation").asText(explanation);
            this.createdAt = Optional.ofNullable(data.path("createdAt").asText(null))
                    .map(Instant::parse)
                    .orElse(createdAt);

            return this;
        }

        @Override
        @Contract(pure = true)
        public int compareTo(@NotNull Snippet.Revision other) {
            return REVISION_COMPARATOR.compare(this, other);
        }
    }
}
