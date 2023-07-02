package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Query(value = "SELECT lemma_id " +
            "FROM indexes " +
            "where page_id = :pageId " +
            "group by lemma_id", nativeQuery = true)
    List<Integer> getLemmaIdListByPageId(int pageId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM indexes " +
            "WHERE page_id = :pageId", nativeQuery = true)
    void deleteByPageId(int pageId);
}

