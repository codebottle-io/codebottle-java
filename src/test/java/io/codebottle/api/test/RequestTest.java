package io.codebottle.api.test;

import io.codebottle.api.CodeBottle;
import io.codebottle.api.model.Snippet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RequestTest {
    private CodeBottle codeBottle;

    @Before
    public void setUp() {
        codeBottle = new CodeBottle.Builder()
                .build()
                .waitForLazyLoading();
    }

    @Test
    public void testRequestSnippetByID() {
        final Snippet snippet = codeBottle.requestSnippetByID("1b6c6604d6").join();

        assertNotNull("Requested Snippet is null!", snippet);
    }

    @Test
    public void testRequestRevisionByID() {
        final Snippet.Revision revision = codeBottle.requestSnippetRevisionByID("1b6c6604d6", 1).join();

        assertNotNull("Requested Revision is null!", revision);
    }
}
