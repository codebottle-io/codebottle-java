package io.codebottle.api.test;

import java.util.concurrent.CompletableFuture;

import io.codebottle.api.CodeBottle;
import io.codebottle.api.model.Snippet;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.currentTimeMillis;

public class PerformanceTest {
    private CodeBottle codeBottle;

    @Before
    public void setUp() {
        this.codeBottle = new CodeBottle();
    }
    
    @Test
    public void requestEverything() {
        long start, end;
        
        System.out.printf("Started requesting everything at %d epoch\n", start = currentTimeMillis());

        codeBottle.requestLanguages().join();
        codeBottle.requestCategories().join();
        codeBottle.requestSnippets().join()
                .stream()
                .map(Snippet::requestRevisions)
                .forEachOrdered(CompletableFuture::join);
        
        System.out.printf("Finished ingesting everything at %d epoch; took %d milliseconds\n\n", end = currentTimeMillis(), end - start);

        final int languageCount = codeBottle.getLanguages().size();
        final int categoryCount = codeBottle.getCategories().size();
        final int snippetCount = codeBottle.getSnippets().size();
        final int revisionCount = codeBottle.getSnippetRevisions().size();

        System.out.printf("Requested:\n\tLanguages:\t%d\n\tCategories:\t%d\n\tSnippets:\t%d\n\tRevisions:\t%d\n", languageCount, categoryCount, snippetCount, revisionCount);
    } 
}
