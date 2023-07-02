package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.Status;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    @Query(value = "SELECT * from sites where url LIKE %:url% LIMIT 1", nativeQuery = true)
    Site getByUrl(String url);

    @Query(value = "SELECT * from sites where status LIKE %:status%  LIMIT 1", nativeQuery = true)
    Site findAnyStatus(Status status);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Site s SET s.status = ?2 WHERE s.status LIKE ?1")
    void updateStatus(Status statusFrom, Status statusTo);
}

