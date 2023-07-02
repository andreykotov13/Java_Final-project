package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "status_time", nullable = false)
    LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    String name;
}
