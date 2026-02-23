package com.securemath.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", insertable = false, updatable = false)
    private QuizAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    @Column(name = "response_value", nullable = false, columnDefinition = "TEXT")
    private String responseValue;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "awarded_points", nullable = false)
    private Double awardedPoints;
}
