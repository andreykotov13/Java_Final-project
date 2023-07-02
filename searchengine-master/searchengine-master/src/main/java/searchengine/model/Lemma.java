package searchengine.model;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "lemmas")
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(columnDefinition = "VARCHAR(255), UNIQUE KEY uk_site_lemma(site_id, lemma(255))")
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    @Override
    public int compareTo(Lemma o) {
        return Integer.compare(this.frequency, o.getFrequency());
    }
}
