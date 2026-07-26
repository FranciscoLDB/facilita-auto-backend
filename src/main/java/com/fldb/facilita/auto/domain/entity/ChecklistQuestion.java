package com.fldb.facilita.auto.domain.entity;

import com.fldb.facilita.auto.domain.enums.ResponseType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checklist_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(name = "position_order", nullable = false)
    private Integer positionOrder;

    @Column(name = "question_text", nullable = false, length = 255)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", nullable = false, length = 20)
    private ResponseType responseType;

    @Builder.Default
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "jsonb")
    private List<String> optionsJson;

    @Builder.Default
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
