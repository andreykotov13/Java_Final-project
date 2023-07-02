package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.ParserConfig;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.IndexingService;
import searchengine.services.NetworkService;
import searchengine.services.SiteService;

import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@RequiredArgsConstructor
public class ThreadUtil implements Runnable {

    private final ForkJoinPool forkJoinPool;
    private final ParserConfig parserConfig;
    private final NetworkService networkService;
    private final SiteService siteService;
    private final IndexingService indexingService;
    private final Site site;
    private final String startUrl;

    @Override
    public void run() {
        try {
            Parser parser = new Parser(site, startUrl, new HashSet<>(),
                    networkService, indexingService, parserConfig);
            long start = System.currentTimeMillis();
            if (forkJoinPool.invoke(parser)) {
                siteService.updateSiteStatus(site, Status.INDEXED, "");
                System.out.println(site.getUrl() + " - " + (System.currentTimeMillis() - start) + "ms");
            } else {
                siteService.updateSiteStatus(site, Status.FAILED, "Индексация остановлена пользователем");
            }
        } catch (Exception e) {
            log.info("Ошибка индексации - " + e.getMessage());
        }
    }
}
