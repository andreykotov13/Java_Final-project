package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO lemmas (site_id, lemma, frequency) " +
            "VALUES (?1, ?2, ?3) " +
            "ON DUPLICATE KEY UPDATE `frequency`=`frequency` + ?3", nativeQuery = true)
    void merge(int siteId, String lemma, int frequency);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE lemmas\n" +
            "SET frequency = frequency - 1\n" +
            "WHERE id = :lemmaId", nativeQuery = true)
    void decreaseFrequencyByLemmaId(int lemmaId);

    @Query(value = "SELECT * " +
            "FROM lemmas " +
            "WHERE site_id = :siteId AND lemma LIKE :lemma", nativeQuery = true)
    Lemma get(int siteId, String lemma);

    @Query(value = "SELECT COUNT(*) " +
            "FROM lemmas " +
            "WHERE site_id = :siteId", nativeQuery = true)
    Integer getLemmasCount(int siteId);

    @Query(value = "SELECT SUM(l.frequency) " +
            "FROM lemmas l " +
            "WHERE l.lemma LIKE :lemma " +
            "GROUP BY l.lemma", nativeQuery = true)
    Integer getLemmaFrequency(String lemma);

    @Query(value = "SELECT l.frequency " +
            "FROM lemmas l " +
            "WHERE l.lemma LIKE :lemma " +
            "AND l.site_id = :siteId", nativeQuery = true)
    Integer getLemmaFrequency(String lemma, int siteId);

}
