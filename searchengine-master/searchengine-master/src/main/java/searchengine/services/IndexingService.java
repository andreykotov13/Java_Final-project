package searchengine.services;

import org.jsoup.Connection;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;

public interface IndexingService {
    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexPage(String url);

    void parsePage(Site site, Connection.Response response) throws Exception;
}
