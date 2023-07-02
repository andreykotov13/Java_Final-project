package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.Page;

@Data
@AllArgsConstructor
public class PageRelevanceResponse {
    private Page page;
    private double relevance;
}
