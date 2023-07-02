package searchengine.services;

import org.jsoup.Connection;
import searchengine.dto.search.PageRelevanceResponse;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.List;

public interface PageService {
    List<PageRelevanceResponse> getPagesRelevance(List<String> lemmas, Integer siteId);

    void deleteAll();

    int getPagesCount(int siteId);

    Page addPage(Site site, Connection.Response response) throws IOException;
}
