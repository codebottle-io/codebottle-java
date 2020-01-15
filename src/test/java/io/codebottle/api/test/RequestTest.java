package io.codebottle.api.test;

import io.codebottle.api.CodeBottleAPI;
import io.codebottle.api.model.Snippet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RequestTest {
    private CodeBottleAPI codeBottleAPI;

    @Before
    public void setUp() {
        codeBottleAPI = new CodeBottleAPI.Builder()
                .build()
                .waitForLazyLoading();
    }

    @Test
    public void testRequestSnippetByID() {
        final Snippet snippet = codeBottleAPI.requestSnippetByID("1b6c6604d6").join();

        assertNotNull("Requested Snippet is null!", snippet);
    }

    @Test
    public void testRequestRevisionByID() {
        final Snippet.Revision revision = codeBottleAPI.requestSnippetRevisionByID("1b6c6604d6", 1).join();

        assertNotNull("Requested Revision is null!", revision);
    }
}
