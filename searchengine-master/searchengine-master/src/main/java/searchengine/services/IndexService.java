package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;

@Service
public interface IndexService {
    void saveAll(List<Index> indexes);

    void deleteAll();

    List<Index> addIndexes(Map<String, Integer> lemmaMap, Site site, Page page);

    List<Integer> getLemmaIdListByPageId(int pageId);

    void deleteByPageId(int pageId);
}
