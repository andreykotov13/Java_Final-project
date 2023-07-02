package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SearchConfig;
import searchengine.dto.search.PageRelevanceResponse;
import searchengine.dto.search.SearchError;
import searchengine.dto.search.SearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Page;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SearchConfig defaultSearchConfig;
    private final LemmaService lemmaService;
    private final PageService pageService;
    private final SiteService siteService;
    private final MorphologyService morphologyService;

    private String getTitle(String html) {
        return Jsoup.parse(html).title();
    }


    private SearchResponse createResponse(List<PageRelevanceResponse> pagesRelevance, List<String> lemmas, SearchConfig searchCfg) {
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(pagesRelevance.size());

        List<SearchItem> data = new ArrayList<>();
        for (PageRelevanceResponse pageRelevance : pagesRelevance) {
            Page page = pageRelevance.getPage();
            SearchItem searchItem = new SearchItem();
            searchItem.setSite(page.getSite().getUrl());
            searchItem.setSiteName(page.getSite().getName());
            searchItem.setTitle(getTitle(page.getContent()));
            searchItem.setUri(page.getPath());
            searchItem.setSnippet(morphologyService.getSnippet(
                    page.getContent(), lemmas, searchCfg.getSnippetSize()));
            searchItem.setRelevance(pageRelevance.getRelevance());
            data.add(searchItem);
        }
        response.setData(data);
        return response;
    }

    @Override
    public ResponseEntity<?> search(SearchConfig searchConfig) {
        searchConfig.setThreshold(defaultSearchConfig.getThreshold());
        searchConfig.setSnippetSize(defaultSearchConfig.getSnippetSize());
        if (searchConfig.getQuery().isEmpty()) {
            return new ResponseEntity<>(new SearchError(false, "Задан пустой поисковый запрос"), HttpStatus.OK);
        }
        if (searchConfig.getLimit() == 0) {
            searchConfig.setLimit(defaultSearchConfig.getLimit());
        }

        Integer siteId = searchConfig.getSite() == null ? null : siteService.getByUrl(searchConfig.getSite()).getId();

        Map<String, Integer> lemmasFrequency = lemmaService.collectLemmaFrequency(searchConfig, siteId);
        List<String> sortedLemmas = lemmasFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList();

        List<PageRelevanceResponse> pagesRelevance = pageService.getPagesRelevance(sortedLemmas, siteId);
        SearchResponse response = createResponse(pagesRelevance, sortedLemmas, searchConfig);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
