package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.dto.search.IPageRank;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT * " +
            "FROM pages " +
            "WHERE site_id = :siteId " +
            "AND path LIKE :path LIMIT 1", nativeQuery = true)
    Page getPagesByPath(int siteId, String path);

    @Query(value = "SELECT COUNT(*) " +
            "FROM pages " +
            "WHERE site_id = :siteId", nativeQuery = true)
    Integer getPagesCount(int siteId);

    @Query(value = "SELECT p.id as `pageId`, i.rank_index as `lemmaRank` " +
            "FROM lemmas l " +
            "LEFT JOIN indexes i ON l.id = i.lemma_id " +
            "LEFT JOIN pages p ON p.id = i.page_id " +
            "WHERE l.lemma LIKE :lemma", nativeQuery = true)
    List<IPageRank> getPagesByLemma(String lemma);

    @Query(value = "SELECT p.id as `pageId`, i.rank_index as `lemmaRank` " +
            "FROM lemmas l " +
            "LEFT JOIN indexes i ON l.id = i.lemma_id " +
            "LEFT JOIN pages p ON p.id = i.page_id " +
            "WHERE l.site_id = :siteId " +
            "AND l.lemma LIKE :lemma", nativeQuery = true)
    List<IPageRank> getPagesByLemma(String lemma, Integer siteId);

    @Query(value = "SELECT p.id as `pageId`, i.rank_index as `lemmaRank` " +
            "FROM lemmas l " +
            "LEFT JOIN indexes i ON l.id = i.lemma_id " +
            "LEFT JOIN pages p ON p.id = i.page_id " +
            "WHERE p.id IN (:pageIndexes) " +
            "AND l.lemma LIKE :lemma", nativeQuery = true)
    List<IPageRank> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes);

    @Query(value = "SELECT p.id as `pageId`, i.rank_index as `lemmaRank` " +
            "FROM lemmas l " +
            "LEFT JOIN indexes i ON l.id = i.lemma_id " +
            "LEFT JOIN pages p ON p.id = i.page_id " +
            "WHERE l.site_id = :siteId " +
            "AND p.id IN (:pageIndexes) " +
            "AND l.lemma LIKE :lemma", nativeQuery = true)
    List<IPageRank> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes, Integer siteId);
}
