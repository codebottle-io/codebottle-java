package io.codebottle.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebottle.api.model.Category;
import io.codebottle.api.model.Language;
import io.codebottle.api.model.Snippet;
import io.codebottle.api.rest.CodeBottleRequest;
import io.codebottle.api.rest.Endpoint;
import org.jetbrains.annotations.Nullable;

public final class CodeBottle {
    private final Map<Integer, Language> languageCache = new ConcurrentHashMap<>();
    private final Map<Integer, Category> categoryCache = new ConcurrentHashMap<>();
    private final Map<Integer, Snippet> snippetCache = new ConcurrentHashMap<>();

    private final String token;

    public CodeBottle() {
        this(null);
    }

    public CodeBottle(@Nullable String token) {
        this.token = token;
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public Optional<Language> getLanguageByID(int id) {
        if (id == -1) return Optional.empty();

        synchronized (languageCache) {
            return Optional.ofNullable(languageCache.get(id));
        }
    }

    public CompletableFuture<Language> requestLanguageByID(int id) {
        return new CodeBottleRequest<Language>(this)
                .to(Endpoint.LANGUAGE_SPECIFIC, id)
                .makeGET()
                .then(data -> {
                    synchronized (languageCache) {
                        return getLanguageByID(id)
                                .map(entity -> entity.update(data))
                                .orElseGet(() -> languageCache.compute(id,
                                        // existing values don't matter here, as the cache access failed before
                                        (k, v) -> new Language(this, data)));
                    }
                });
    }

    public Optional<Category> getCategoryByID(int id) {
        if (id == -1) return Optional.empty();

        synchronized (categoryCache) {
            return Optional.ofNullable(categoryCache.get(id));
        }
    }

    public CompletableFuture<Category> requestCategoryByID(int id) {
        return new CodeBottleRequest<Category>(this)
                .to(Endpoint.CATEGORY_SPECIFIC, id)
                .makeGET()
                .then(data -> {
                    synchronized (categoryCache) {
                        return getCategoryByID(id)
                                .map(entity -> entity.update(data))
                                .orElseGet(() -> categoryCache.compute(id,
                                        // existing values don't matter here, as the cache access failed before
                                        (k, v) -> new Category(this, data)));
                    }
                });
    }

    public Optional<Snippet> getSnippetByID(int id) {
        if (id == -1) return Optional.empty();

        synchronized (snippetCache) {
            return Optional.ofNullable(snippetCache.get(id));
        }
    }

    public CompletableFuture<Snippet> requestSnippetByID(int id) {
        return new CodeBottleRequest<Snippet>(this)
                .to(Endpoint.SNIPPET_SPECIFIC, id)
                .makeGET()
                .then(data -> {
                    synchronized (snippetCache) {
                        return getSnippetByID(id)
                                .map(entity -> entity.update(data))
                                .orElseGet(() -> snippetCache.compute(id,
                                        // existing values don't matter here, as the cache access failed before
                                        (k, v) -> new Snippet(this, data)));
                    }
                });
    }

    public Optional<Snippet.Revision> getSnippetRevisionByID(int snippetId, int id) throws IndexOutOfBoundsException {
        if (id == -1) return Optional.empty();

        synchronized (snippetCache) {
            return Optional.ofNullable(snippetCache.get(id))
                    .flatMap(snippet -> snippet.getRevisionByID(id));
        }
    }

    public CompletableFuture<Snippet.Revision> requestSnippetRevision(int snippetId, int id) {
        return getSnippetByID(snippetId)
                .orElseGet(() -> requestSnippetByID(snippetId).join())
                .requestRevision(id);
    }

    public CompletableFuture<List<Snippet.Revision>> requestSnippetRevisions(int snippetId, int id) {
        return getSnippetByID(snippetId)
                .orElseGet(() -> requestSnippetByID(snippetId).join())
                .requestRevisions();
    }
}
