package searchengine.services;

import searchengine.config.SearchConfig;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LemmaService {
    void deleteAll();

    void mergeFrequency(List<Lemma> lemmas);

    void decreaseFrequencyByLemmaId(int lemmaId);

    Lemma get(int siteId, String lemma);

    List<Lemma> createLemmas(Set<String> lemmaSet, Site site);

    Map<String, Integer> collectLemmaFrequency(SearchConfig searchConfig, Integer siteId);

    Integer getLemmasCount(int siteId);
}