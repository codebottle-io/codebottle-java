package io.codebottle.api.rest;

import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;

public enum Endpoint {
    // Languages
    LANGUAGES("languages"),
    LANGUAGE_SPECIFIC("language/%s"),

    // Categories
    CATEGORIES("categories"),
    CATEGORY_SPECIFIC("categories/%s"),

    // Snippets
    SNIPPETS("snippets"),
    SNIPPET_SPECIFIC("snippets/%s"),
    // Snippet Revisions
    SNIPPET_REVISIONS("snippets/%s/revisions"),
    SNIPPET_REVISION_SPECIFIC("snippets/%s/revisions/%s");

    public static final String URL_BASE = "https://api.codebottle.io/";

    private final String appendix;

    Endpoint(String appendix) {
        this.appendix = appendix;
    }

    public int getRequiredParameterCount() {
        return appendix.split("%s").length - 1;
    }

    public URL url(Object... args) throws IllegalArgumentException {
        final int parameterCount = getRequiredParameterCount();

        if (args.length != parameterCount)
            throw new IllegalArgumentException(format("Illegal argument count {actual: %d, expected: %d}", args.length, parameterCount));

        Object[] strings = new String[args.length];

        for (int i = 0; i < args.length; i++)
            strings[i] = String.valueOf(args[i]);

        try {
            return new URL(format(URL_BASE + appendix, strings));
        } catch (MalformedURLException e) {
            throw new AssertionError("Unexpected MalformedURLException", e);
        }
    }
}
