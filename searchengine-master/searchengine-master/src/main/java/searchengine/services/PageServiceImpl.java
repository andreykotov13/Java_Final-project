package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.IPageRank;
import searchengine.dto.search.PageRankImpl;
import searchengine.dto.search.PageRelevanceResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService, Serializable {

    private final PageRepository pageRepository;


    @Override
    public void deleteAll() {
        pageRepository.deleteAll();
    }

    @Override
    public int getPagesCount(int siteId) {
        return pageRepository.getPagesCount(siteId);
    }

    @Override
    public Page addPage(Site site, Connection.Response response) throws IOException {
        Document document = response.parse();
        String url = response.url().toString();
        String path = url.substring(site.getUrl().length());
        Page page = pageRepository.getPagesByPath(site.getId(), path);
        if (page == null) {
            page = new Page();
            page.setPath(path);
            page.setSite(site);
        }
        page.setCode(response.statusCode());
        page.setContent(document.toString());
        return save(page);
    }

    private Page save(Page page) {
        return pageRepository.saveAndFlush(page);
    }

    public List<PageRelevanceResponse> makePagesRelevance(List<IPageRank> totalRankPages) {
        int absRelevance = totalRankPages.stream()
                .mapToInt(IPageRank::getLemmaRank)
                .sum();

        List<PageRelevanceResponse> pagesRelevance = new ArrayList<>();
        for (IPageRank item : totalRankPages) {
            Page page = pageRepository.findById(item.getPageId()).get();
            double relevance = Double.valueOf(item.getLemmaRank()) / absRelevance;
            pagesRelevance.add(new PageRelevanceResponse(page, relevance));
        }
        return pagesRelevance;
    }

    @Override
    public List<PageRelevanceResponse> getPagesRelevance(List<String> lemmas, Integer siteId) {
        if (lemmas.isEmpty()) {
            return Collections.emptyList();
        }
        List<IPageRank> totalRankPages = null;
        for (String lemma : lemmas) {
            if (totalRankPages == null) {
                totalRankPages = getPagesByLemma(lemma, siteId);
                continue;
            }
            List<Integer> pageIndexes = totalRankPages.stream().map(IPageRank::getPageId).toList();
            List<IPageRank> lemmaRankPages = findPagesByIdAndLemma(lemma, pageIndexes, siteId);
            totalRankPages = mergeAndIncrementRank(totalRankPages, lemmaRankPages);
            if (totalRankPages.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return makePagesRelevance(totalRankPages);
    }

    private List<IPageRank> getPagesByLemma(String lemma, Integer siteId) {
        return siteId == null ? pageRepository.getPagesByLemma(lemma)
                : pageRepository.getPagesByLemma(lemma, siteId);
    }

    private List<IPageRank> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes, Integer siteId) {
        return siteId == null ? pageRepository.findPagesByIdAndLemma(lemma, pageIndexes)
                : pageRepository.findPagesByIdAndLemma(lemma, pageIndexes, siteId);
    }

    private IPageRank findExistItem(List<IPageRank> currentList, IPageRank findItem) {
        for (IPageRank item : currentList) {
            if (item.getPageId().equals(findItem.getPageId())) {
                return item;
            }
        }
        return null;
    }

    private List<IPageRank> mergeAndIncrementRank(List<IPageRank> totalRankPages,
                                                  List<IPageRank> lemmaRankPages) {
        List<IPageRank> result = new ArrayList<>();
        for (IPageRank newPageRank : lemmaRankPages) {
            IPageRank foundItem = findExistItem(totalRankPages, newPageRank);
            if (foundItem != null) {
                IPageRank sumRankItem = new PageRankImpl(foundItem.getPageId(),
                        foundItem.getLemmaRank() + newPageRank.getLemmaRank());
                result.add(sumRankItem);
            }
        }
        return result;
    }
}