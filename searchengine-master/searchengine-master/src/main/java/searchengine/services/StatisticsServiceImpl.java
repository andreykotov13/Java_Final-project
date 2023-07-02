package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;

    private Long localDataTimeToMills(LocalDateTime dateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    DetailedStatisticsItem getSiteStatistic(Site site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(pageService.getPagesCount(site.getId()));
        item.setLemmas(lemmaService.getLemmasCount(site.getId()));
        item.setStatusTime(localDataTimeToMills(site.getStatusTime()));
        item.setStatus(site.getStatus().toString());
        item.setError(site.getLastError());
        return item;
    }

    @Override
    public StatisticsResponse getStatistics() {
        List<Site> sites = siteService.getAll();
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.size());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for (Site site : sites) {
            DetailedStatisticsItem item = getSiteStatistic(site);
            total.setPages(total.getPages() + item.getPages());
            total.setLemmas(total.getLemmas() + item.getLemmas());
            detailed.add(item);
        }

        total.setIndexing(siteService.isIndexing());
        StatisticsData statisticsData = new StatisticsData();
        statisticsData.setTotal(total);
        statisticsData.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(statisticsData);
        response.setResult(true);
        return response;
    }
}
