package io.codebottle.api.rest;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebottle.api.CodeBottle;
import io.codebottle.api.rest.exception.UnexpectedStatusCodeException;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.intellij.lang.annotations.MagicConstant;

public final class CodeBottleRequest<T> {
    /*
    Fluent API Design:
    
    GET-Request without body to 'endpoint':
        new CodeBottleRequest(context)
                .to(endpoint)
                .then([remap] JsonNode::remap);
                
    PUT-Request with body to 'endpoint':
        new CodeBottleRequest(context)
                .make(PUT, [withData] json)
                .to(endpoint, [at] id)
                .then([remap] JsonNode::remap);
    
    DELETE-Request with body to 'endpoint' with differing expected response code '204':
        new CodeBottleRequest(context) 
                .make(PUT, [withData] json)
                .to(endpoint, [at] id)
                .andExpect([code] 204)
                .then([remap] JsonNode::remap);
     */

    private final static ObjectMapper objectMapper;

    private final Request.Builder httpRequest;
    private final CodeBottle context;

    private int expected = HTTPCodes.OK;

    static {
        objectMapper = new ObjectMapper();
    }

    public CodeBottleRequest(CodeBottle context) {
        this.context = context;
        this.httpRequest = new Request.Builder()
                .addHeader("Accept", "application/vnd.codebottle.v1+json"); // fixed request header

        // Experimental Feature:
        // Add the token as 'Authorization' header ifPresent
        context.getToken().ifPresent(token -> httpRequest.addHeader("Authorization", token));
    }

    public CodeBottle getContext() {
        return context;
    }

    public CodeBottleRequest<T> make(Method method, JsonNode withData) {
        httpRequest.method(method.name(), method == Method.GET ? null : RequestBody.create(withData.toString(), MediaType.parse("application/json")));

        return this;
    }

    public CodeBottleRequest<T> makeGET() {
        //noinspection ConstantConditions
        return make(Method.GET, null);
    }

    public CodeBottleRequest<T> to(Endpoint endpoint, Object... at) throws IllegalArgumentException {
        final URL url = endpoint.url(at);
        httpRequest.url(url);

        return this;
    }

    public CodeBottleRequest<T> andExpect(@MagicConstant(valuesFromClass = HTTPCodes.class) int responseCode) throws IllegalArgumentException {
        this.expected = responseCode;

        return this;
    }

    public CompletableFuture<T> then(Function<JsonNode, T> remap) {
        return CompletableFuture.supplyAsync(() -> {
            final Request request = httpRequest.build();

            try {
                final Call call = context.getHttpClient()
                        .newCall(request);

                final Response response = call.execute();
                final ResponseBody body = response.body();

                if (body == null) return null;
                final String str = body.string();
                final JsonNode data = objectMapper.readTree(str);

                final int code = response.code();

                if (code == expected) {
                    if (expected == 204) // 204 always has null response
                        return null;

                    return remap.apply(data);
                } else throw new UnexpectedStatusCodeException(code, data.path("error").asText("No error message received"));
            } catch (JsonProcessingException e) {
                throw new AssertionError("Received invalid JSON data", e);
            } catch (IOException e) {
                throw new AssertionError("Unexpected IOException occurred", e);
            }
        });
    }
}
