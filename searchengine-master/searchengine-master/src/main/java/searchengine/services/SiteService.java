package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;

@Service
public interface SiteService {
    Site save(Site site);

    void saveAll(List<Site> sites);

    Site getByUrl(String url);

    Site createSite(SiteConfig siteConfig, Status status, String lastError);

    List<Site> getSitesToParsing(SitesList sites);

    List<Site> getAll();

    void deleteAll();

    boolean isIndexing();

    void dropIndexingStatus();

    void updateSiteStatus(Site site, Status status, String lastError);
}
