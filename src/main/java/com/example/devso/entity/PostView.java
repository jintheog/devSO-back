package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_view_trackers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_view_post_viewer",
                        columnNames = {"post_id", "viewer_key"}
                )
        }
)
@Getter
@NoArgsConstructor
public class PostView extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "viewer_key", nullable = false, length = 128)
    private String viewerKey;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    public PostView(Post post, String viewerKey, LocalDateTime lastViewedAt) {
        this.post = post;
        this.viewerKey = viewerKey;
        this.lastViewedAt = lastViewedAt;
    }
}


