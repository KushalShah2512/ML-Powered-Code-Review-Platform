package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_reviews")
public class CodeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime reviewDate;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private CodeFile codeFile;

    @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL)
    private List<ReviewResult> results;
}