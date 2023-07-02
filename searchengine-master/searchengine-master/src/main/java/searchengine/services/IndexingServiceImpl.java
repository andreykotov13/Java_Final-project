package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.config.ParserConfig;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.*;
import searchengine.utils.Parser;
import searchengine.utils.ThreadUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserConfig parserConfig;
    private final NetworkService networkService;
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final MorphologyService morphologyService;

    public IndexingServiceImpl(SitesList sites, ParserConfig parserConfig,
                               NetworkService networkService, SiteService siteService,
                               PageService pageService, LemmaService lemmaService,
                               IndexService indexService, MorphologyService morphologyService) {
        this.sites = sites;
        this.parserConfig = parserConfig;
        this.networkService = networkService;
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.morphologyService = morphologyService;
        siteService.dropIndexingStatus();
    }

    @Override
    public IndexingResponse startIndexing() {
        if (siteService.isIndexing()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        Parser.setIsCanceled(false);

        Thread thread = new Thread(() -> {
            indexService.deleteAll();
            lemmaService.deleteAll();
            pageService.deleteAll();
            siteService.deleteAll();

            List<Site> sitesToParsing = siteService.getSitesToParsing(sites);
            siteService.saveAll(sitesToParsing);

            for (Site site : sitesToParsing) {
                if (site.getStatus() == Status.INDEXING) {
                    ThreadUtil task = new ThreadUtil(new ForkJoinPool(parserConfig.getParallelism()),
                            parserConfig, networkService, siteService,
                            this, site, site.getUrl() + "/");
                    Thread parseSite = new Thread(task);
                    parseSite.start();
                }
            }
        });
        thread.start();
        return new IndexingResponse(true, "");
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!siteService.isIndexing()){
            return new IndexingResponse(false, "Индексация не запущена");
        }
        Parser.setIsCanceled(true);
        return new IndexingResponse(true, "");
    }

    @Override
    public IndexingResponse indexPage(String url) {
        String finalUrl = url;
        Optional<SiteConfig> findUrl = sites.getSites().stream()
                .filter(s -> finalUrl.startsWith(s.getUrl()))
                .findFirst();
        if (findUrl.isEmpty()) {
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
        }
        SiteConfig siteConfig = findUrl.get();
        url = url.equals(siteConfig.getUrl()) ? url + "/" : url;

        Connection.Response response = networkService.getResponse(url);
        if (!networkService.isAvailable(response)) {
            return new IndexingResponse(false, "Ошибка обработки страницы - " + url);
        }

        Thread thread = new Thread(() -> {
            Site site = siteService.getByUrl(siteConfig.getUrl());
            if (site == null) {
                site = siteService.createSite(siteConfig, Status.INDEXED, "");
                site = siteService.save(site);
            }

            try {
                parsePage(site, response);
            } catch (Exception e) {
                site.setLastError(e.getMessage());
                site.setStatus(Status.FAILED);
                siteService.save(site);
                log.error(e.getMessage());
            }
        });
        thread.start();
        return new IndexingResponse(true, "");
    }

    @Override
    public void parsePage(Site site, Connection.Response response) throws Exception {
        Page page = pageService.addPage(site, response);

        Map<String, Integer> lemmaMap = morphologyService.collectLemmas(page.getContent());
        List<Integer> lemmaIdList = indexService.getLemmaIdListByPageId(page.getId());
        if (lemmaIdList.size() > 0) {
            indexService.deleteByPageId(page.getId());
            for (Integer lemmaId : lemmaIdList) {
                lemmaService.decreaseFrequencyByLemmaId(lemmaId);
            }
        }

        List<Lemma> lemmas = lemmaService.createLemmas(lemmaMap.keySet(), site);
        lemmaService.mergeFrequency(lemmas);

        List<Index> indexes = indexService.addIndexes(lemmaMap, site, page);
        indexService.saveAll(indexes);
    }

}
