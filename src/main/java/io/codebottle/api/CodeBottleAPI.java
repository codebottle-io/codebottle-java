package io.codebottle.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.codebottle.api.model.Category;
import io.codebottle.api.model.Language;
import io.codebottle.api.model.Snippet;
import io.codebottle.api.rest.CodeBottleRequest;
import io.codebottle.api.rest.Endpoint;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;

/**
 * API Class. Create an instance of this using {@code #builder()} to use the API.
 */
public final class CodeBottleAPI {
    private final Map<String, Language> languageCache = new ConcurrentHashMap<>();
    private final Map<String, Category> categoryCache = new ConcurrentHashMap<>();
    private final Map<String, Snippet> snippetCache = new ConcurrentHashMap<>();
    private @Deprecated final @Nullable String token;
    private final OkHttpClient httpClient;
    /**
     * A {@link CompletableFuture} that completes once lazy loading was finished.
     * <p>
     * Lazy loading is calling {@link #requestLanguages()} and {@link #requestCategories()} on API construction.
     */
    public final CompletableFuture<Void> lazyLoading = CompletableFuture.allOf(requestLanguages(), requestCategories());

    private CodeBottleAPI(@Nullable String token, OkHttpClient httpClient) {
        this.token = token;
        this.httpClient = httpClient;
    }

    /**
     * Waits for {@linkplain #lazyLoading lazy loading} to finish using {@link CompletableFuture#join()}.
     * This method is intended to be used for API chaining, otherwise it is recommended to wait for {@link #lazyLoading} yourself.
     *
     * @return this {@link CodeBottleAPI} instance.
     */
    public CodeBottleAPI waitForLazyLoading() {
        lazyLoading.join();

        return this;
    }

    /**
     * @deprecated Might be used later.
     */
    @Deprecated
    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    /**
     * Returns the {@link Language} matching the given {@code id} if found.
     * Before {@link #lazyLoading} was completed, this will always return {@link Optional#empty()}.
     *
     * @param id is the ID of the desired language.
     *
     * @return the language matching the given {@code id}.
     */
    public Optional<Language> getLanguageByID(String id) {
        return Optional.ofNullable(languageCache.get(id));
    }

    /**
     * Returns all cached {@link Language}s.
     * Before {@link #lazyLoading} was completed, the returned {@link Collection} will always be empty.
     *
     * @return a {@link Collection} of all cached languages.
     */
    public Collection<Language> getLanguages() {
        return languageCache.values();
    }

    /**
     * Requests the language by the given {@code id} and synchronizes the cache when deserializing the result.
     *
     * @param id is the {@code id} of the {@link Language} to request.
     *
     * @return a future that will complete with the requested {@link Language} after updating it in the cache.
     */
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

    /**
     * Requests all {@link Language}s and refreshes them in the cache.
     *
     * @return a future that will complete with all cached {@link Language}s.
     */
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

    /**
     * Returns the {@link Category} matching the given {@code id} if found in cache.
     * Before {@link #lazyLoading} was completed, this will always return {@link Optional#empty()}.
     *
     * @param id is the ID of the desired category.
     *
     * @return the category matching the given {@code id}.
     */
    public Optional<Category> getCategoryByID(String id) {
        return Optional.ofNullable(categoryCache.get(id));
    }

    /**
     * Returns all cached {@linkplain Category categories}.
     * Before {@link #lazyLoading} was completed, the returned {@link Collection} will always be empty.
     *
     * @return a {@link Collection} of all cached categories.
     */
    public Collection<Category> getCategories() {
        return categoryCache.values();
    }

    /**
     * Requests the category by the given {@code id} and synchronizes the cache when deserializing the result.
     *
     * @param id is the {@code id} of the {@link Category} to request.
     *
     * @return a future that will complete with the requested {@link Category} after updating it in the cache.
     */
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

    /**
     * Requests all {@link Category}s and refreshes them in the cache.
     *
     * @return a future that will complete with all cached {@linkplain Category categories}.
     */
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

    /**
     * Returns the {@link Snippet} matching the given {@code id} if found.
     *
     * @param id is the ID of the desired snippet.
     *
     * @return the snippet matching the given {@code id}.
     */
    public Optional<Snippet> getSnippetByID(String id) {
        return Optional.ofNullable(snippetCache.get(id));
    }

    /**
     * Returns all cached {@link Snippet}s.
     *
     * @return a {@link Collection} of all cached snippets.
     */
    public Collection<Snippet> getSnippets() {
        return snippetCache.values();
    }

    /**
     * Requests the snippet by the given {@code id} and synchronizes the cache when deserializing the result.
     *
     * @param id is the {@code id} of the {@link Snippet} to request.
     *
     * @return a future that will complete with the requested {@link Snippet} after updating it in the cache.
     */
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

    /**
     * Requests all {@link Snippet}s and refreshes them in the cache.
     *
     * @return a future that will complete with all cached {@link Snippet}s.
     */
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

    /**
     * Returns the {@link Snippet.Revision} matching the given {@code id pair} if found in cache.
     * Before this method will ever not return {@link Optional#empty()}, you must {@linkplain #requestAllRevisions() request all revisions}.
     *
     * @param id is the ID of the desired language.
     *
     * @return the language matching the given {@code id}.
     */
    public Optional<Snippet.Revision> getSnippetRevisionByID(String snippetId, int id) throws IndexOutOfBoundsException {
        return Optional.ofNullable(snippetCache.get(snippetId))
                .flatMap(snippet -> snippet.getRevisionByID(id));
    }

    /**
     * Returns all cached {@link Language}s.
     *
     * @return a {@link Collection} of all cached languages.
     */
    public Collection<Snippet.Revision> getSnippetRevisions() {
        return snippetCache.values()
                .stream()
                .flatMap(snippet -> snippet.getRevisions().stream())
                .collect(Collectors.toList());
    }

    /**
     * Requests the snippet revision by the given {@code id} and synchronizes the cache when deserializing the result.
     *
     * @param id is the {@code id} of the {@link Snippet.Revision} to request.
     *
     * @return a future that will complete with the requested {@link Snippet.Revision} after updating it in the cache.
     */
    public CompletableFuture<Snippet.Revision> requestSnippetRevisionByID(String snippetId, int id) {
        return getSnippetByID(snippetId)
                .orElseGet(() -> requestSnippetByID(snippetId).join())
                .requestRevision(id);
    }

    /**
     * Requests all {@link Snippet.Revision}s of the defined {@linkplain Snippet snippet id} and refreshes them in the cache.
     *
     * @return a future that will complete with all cached {@link Snippet.Revision}s.
     */
    public CompletableFuture<List<Snippet.Revision>> requestSnippetRevisions(String snippetId) {
        return getSnippetByID(snippetId)
                .orElseGet(() -> requestSnippetByID(snippetId).join())
                .requestRevisions();
    }

    /**
     * Requests all {@link Snippet.Revision}s and refreshes them in the cache.
     *
     * @return a future that will complete with all cached {@link Snippet.Revision}s.
     */
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

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public final static class Builder {
        @Deprecated
        private @Nullable String token = null;
        private OkHttpClient httpClient = new OkHttpClient.Builder().build();

        @Deprecated
        public Optional<String> getToken() {
            return Optional.ofNullable(token);
        }

        @Deprecated
        public void setToken(@Nullable String token) {
            this.token = token;
        }

        public void removeToken() {
            setToken(null);
        }

        public Optional<OkHttpClient> getHttpClient() {
            return Optional.ofNullable(httpClient);
        }

        public void setHttpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public CodeBottleAPI build() {
            return new CodeBottleAPI(token, httpClient);
        }
    }
}
