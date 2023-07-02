package searchengine.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MorphologyService {
    Set<String> getLemmaSet(String html);
    Map<String, Integer> collectLemmas(String html);
    String getSnippet(String html, List<String> lemmas, int snippetSize);
}
