package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Override
    public void saveAll(List<Index> indexes) {
        indexRepository.saveAll(indexes);
    }

    @Override
    public void deleteAll() {
        indexRepository.deleteAll();
    }

    @Override
    public List<Index> addIndexes(Map<String, Integer> lemmaMap, Site site, Page page) {
        List<Index> indexes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
            Lemma lemma = lemmaService.get(site.getId(), entry.getKey());
            int rank = entry.getValue();
            Index index = new Index();
            index.setRank(rank);
            index.setPage(page);
            index.setLemma(lemma);
            indexes.add(index);
        }
        return indexes;
    }

    @Override
    public List<Integer> getLemmaIdListByPageId(int pageId) {
        return indexRepository.getLemmaIdListByPageId(pageId);
    }

    @Override
    public void deleteByPageId(int pageId) {
        indexRepository.deleteByPageId(pageId);
    }
}
