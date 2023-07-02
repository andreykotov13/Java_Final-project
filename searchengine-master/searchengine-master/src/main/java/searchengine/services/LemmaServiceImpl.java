package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SearchConfig;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {
    private final LemmaRepository lemmaRepository;
    private final MorphologyService morphologyService;

    @Override
    public void deleteAll() {
        lemmaRepository.deleteAll();
    }

    @Override
    public Lemma get(int siteId, String lemma) {
        return lemmaRepository.get(siteId, lemma);
    }

    @Override
    public Map<String, Integer> collectLemmaFrequency(SearchConfig searchConfig, Integer siteId) {
        Map<String, Integer> lemmasFrequency = new HashMap<>();
        try {
            Set<String> queryLemmas = morphologyService.getLemmaSet(searchConfig.getQuery());
            for (String lemma : queryLemmas) {
                Integer frequency = siteId == null ? lemmaRepository.getLemmaFrequency(lemma)
                        : lemmaRepository.getLemmaFrequency(lemma, siteId);
                if (frequency != null) {
                    lemmasFrequency.put(lemma, frequency);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyMap();
        }

        if (lemmasFrequency.size() > 1) {
            lemmasFrequency.values().removeIf(ser -> ser > searchConfig.getThreshold());
        }
        return lemmasFrequency;
    }

    @Override
    public List<Lemma> createLemmas(Set<String> lemmaSet, Site site) {
        List<Lemma> lemmas = new ArrayList<>();
        for (String lemmaName : lemmaSet) {
            Lemma lemma = new Lemma();
            lemma.setLemma(lemmaName);
            lemma.setSite(site);
            lemmas.add(lemma);
        }
        return lemmas;
    }

    @Override
    public void mergeFrequency(List<Lemma> lemmas) {
        for (Lemma lemma : lemmas) {
            lemmaRepository.merge(lemma.getSite().getId(),
                    lemma.getLemma(),
                    1);
        }
    }

    @Override
    public void decreaseFrequencyByLemmaId(int lemmaId) {
        lemmaRepository.decreaseFrequencyByLemmaId(lemmaId);
    }

    @Override
    public Integer getLemmasCount(int siteId) {
        Integer count = lemmaRepository.getLemmasCount(siteId);
        return count == null ? 0 : count;
    }
}
