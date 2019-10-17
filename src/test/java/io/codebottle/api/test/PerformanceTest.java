package io.codebottle.api.test;

import java.util.concurrent.CompletableFuture;

import io.codebottle.api.CodeBottle;
import org.junit.Test;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static org.junit.Assert.assertNotEquals;

public class PerformanceTest {
    @Test
    public void requestEverything() {
        final long start = currentTimeMillis();

        out.print("Requesting everything...");

        final CodeBottle codeBottle = CodeBottle.builder().build();

        // wait for all loading to finish
        CompletableFuture.allOf(codeBottle.lazyLoading, codeBottle.requestAllRevisions())
                .join();

        out.print(" OK!\n");

        final int languageCount = codeBottle.getLanguages().size();
        final int categoryCount = codeBottle.getCategories().size();
        final int snippetCount = codeBottle.getSnippets().size();
        final int revisionCount = codeBottle.getSnippetRevisions().size();

        out.print("Checking for cache integrity...");

        assertNotEquals(0, languageCount);
        assertNotEquals(0, categoryCount);
        assertNotEquals(0, snippetCount);
        assertNotEquals(0, revisionCount);

        out.print(" OK!\n");

        out.printf("\nCache Summary:\n\tLanguages:\t%d\n\tCategories:\t%d\n\tSnippets:\t%d\n\tRevisions:\t%d",
                languageCount, categoryCount, snippetCount, revisionCount);

        out.printf("\n\nTook %d milliseconds.", currentTimeMillis() - start);
    }
}
