package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.config.SearchConfig;

public interface SearchService {
    ResponseEntity search(SearchConfig searchConfig);

}
