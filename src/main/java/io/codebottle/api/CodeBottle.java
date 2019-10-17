package io.codebottle.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.codebottle.api.model.Category;
import io.codebottle.api.model.Language;
import io.codebottle.api.model.Snippet;
import io.codebottle.api.rest.CodeBottleRequest;
import io.codebottle.api.rest.Endpoint;
import lombok.Builder;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;

@Builder
public final class CodeBottle {
    public final CompletionStage<Void> lazyLoading = CompletableFuture.allOf(requestLanguages(), requestCategories());

    private final Map<String, Language> languageCache = new ConcurrentHashMap<>();
    private final Map<String, Category> categoryCache = new ConcurrentHashMap<>();
    private final Map<String, Snippet> snippetCache = new ConcurrentHashMap<>();
    
    @Builder.Default
    private final @Nullable String token = null;
    @Builder.Default
    private @Getter final OkHttpClient httpClient = new OkHttpClient.Builder().build();

    @SuppressWarnings("ConstantConditions")
    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public Optional<Language> getLanguageByID(String id) {
        return Optional.ofNullable(languageCache.get(id));
    }

    public Collection<Language> getLanguages() {
        return languageCache.values();
    }

    public CompletableFuture<Language> requestLanguageByID(String id) {
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

    public CompletableFuture<Collection<Language>> requestLanguages() {
        return new CodeBottleRequest<Collection<Language>>(this)
                .to(Endpoint.LANGUAGES)
                .makeGET()
                .then(data -> {
                    synchronized (languageCache) {
                        return StreamSupport.stream(data.spliterator(), false)
                                .map(node -> getLanguageByID(node.path("id").asText())
                                        .map(entity -> entity.update(node))
                                        .orElseGet(() -> {
                                            final Language language = new Language(this, node);

                                            languageCache.compute(language.getID(), (k, v) -> language);

                                            return language;
                                        }))
                                .collect(Collectors.toList());
                    }
                });
    }

    public Optional<Category> getCategoryByID(String id) {
        return Optional.ofNullable(categoryCache.get(id));
    }

    public Collection<Category> getCategories() {
        return categoryCache.values();
    }

    public CompletableFuture<Category> requestCategoryByID(String id) {
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

    public CompletableFuture<Collection<Category>> requestCategories() {
        return new CodeBottleRequest<Collection<Category>>(this)
                .to(Endpoint.CATEGORIES)
                .makeGET()
                .then(data -> {
                    synchronized (categoryCache) {
                        return StreamSupport.stream(data.spliterator(), false)
                                .map(node -> getCategoryByID(node.path("id").asText())
                                        .map(entity -> entity.update(node))
                                        .orElseGet(() -> {
                                            final Category category = new Category(this, node);

                                            categoryCache.compute(category.getID(), (k, v) -> category);

                                            return category;
                                        }))
                                .collect(Collectors.toList());
                    }
                });
    }

    public Optional<Snippet> getSnippetByID(String id) {
        return Optional.ofNullable(snippetCache.get(id));
    }

    public Collection<Snippet> getSnippets() {
        return snippetCache.values();
    }

    public CompletableFuture<Snippet> requestSnippetByID(String id) {
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

    public CompletableFuture<Collection<Snippet>> requestSnippets() {
        return new CodeBottleRequest<Collection<Snippet>>(this)
                .to(Endpoint.SNIPPETS)
                .makeGET()
                .then(data -> {
                    synchronized (snippetCache) {
                        return StreamSupport.stream(data.spliterator(), false)
                                .map(node -> getSnippetByID(node.path("id").asText())
                                        .map(entity -> entity.update(node))
                                        .orElseGet(() -> {
                                            final Snippet snippet = new Snippet(this, node);

                                            snippetCache.compute(snippet.getID(), (k, v) -> snippet);

                                            return snippet;
                                        }))
                                .collect(Collectors.toList());
                    }
                });
    }

    public Optional<Snippet.Revision> getSnippetRevisionByID(String snippetId, int id) throws IndexOutOfBoundsException {
        return Optional.ofNullable(snippetCache.get(snippetId))
                .flatMap(snippet -> snippet.getRevisionByID(id));
    }

    public Collection<Snippet.Revision> getSnippetRevisions() {
        return snippetCache.values()
                .stream()
                .flatMap(snippet -> snippet.getRevisions().stream())
                .collect(Collectors.toList());
    }

    public CompletableFuture<Snippet.Revision> requestSnippetRevision(String snippetId, int id) {
        return getSnippetByID(snippetId)
                .orElseGet(() -> requestSnippetByID(snippetId).join())
                .requestRevision(id);
    }

    public CompletableFuture<List<Snippet.Revision>> requestSnippetRevisions(String snippetId, int id) {
        return getSnippetByID(snippetId)
                .orElseGet(() -> requestSnippetByID(snippetId).join())
                .requestRevisions();
    }
    
    public CompletableFuture<Collection<Snippet.Revision>> requestAllRevisions() {
        return requestSnippets()
                .thenApply(snippets -> {
                    Collection<Snippet.Revision> yields = new ArrayList<>();
                    
                    (snippets.size() > 200 ? snippets.parallelStream() : snippets.stream())
                            .map(Snippet::requestRevisions)
                            .map(CompletableFuture::join)
                            .forEach(yields::addAll);
                    
                    return yields;
                });
    }
}
