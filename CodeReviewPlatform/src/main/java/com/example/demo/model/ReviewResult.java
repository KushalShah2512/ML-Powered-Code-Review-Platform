package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "review_results")
public class ReviewResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String issueType;
    private Integer lineNumber;
    
    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private CodeReview codeReview;
}