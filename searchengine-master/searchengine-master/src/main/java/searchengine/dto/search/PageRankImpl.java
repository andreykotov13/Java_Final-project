package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageRankImpl implements IPageRank {

    private Integer pageId;
    private Integer lemmaRank;

    @Override
    public Integer getPageId() {
        return this.pageId;
    }

    @Override
    public Integer getLemmaRank() {
        return this.lemmaRank;
    }
}
