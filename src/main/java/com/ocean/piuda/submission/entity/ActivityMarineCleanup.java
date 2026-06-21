package com.ocean.piuda.submission.entity;

import com.ocean.piuda.submission.enums.CleanupMethodType;
import com.ocean.piuda.submission.enums.UncollectedScale;
import com.ocean.piuda.submission.enums.WasteType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_marine_cleanup")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ActivityMarineCleanup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "marine_cleanup_waste_types",
        joinColumns = @JoinColumn(name = "activity_id")
    )
    @Column(name = "waste_type")
    @Builder.Default
    private List<WasteType> wasteTypes = new ArrayList<>();  // 폐기물 유형 (복수)

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private CleanupMethodType method;  // 인양 방식

    @Column(name = "collection_amount", nullable = false, length = 200)
    private String collectionAmount;  // 수거량

    @Enumerated(EnumType.STRING)
    @Column(name = "uncollected_scale")
    private UncollectedScale uncollectedScale;  // 미수거 폐기물 규모

    public void updateSubmission(Submission submission) {
        this.submission = submission;
    }
}
