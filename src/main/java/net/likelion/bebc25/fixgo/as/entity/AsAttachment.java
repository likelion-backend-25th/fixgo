package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;

@Entity
@Table(indexes = {@Index(name = "IDX_AS_ATTACHMENT_REQUEST", columnList = "as_request_id")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private AsRequest asRequest;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column
    private String originalName;

    public AsAttachment(AsRequest asRequest, String fileUrl, String originalName) {
        this.asRequest = asRequest;
        this.fileUrl = fileUrl;
        this.originalName = originalName;
    }
}