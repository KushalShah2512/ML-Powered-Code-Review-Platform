package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "code_files")
public class CodeFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    // Fixed: Only declared ONCE with @Lob for large text
    @Lob 
    @Column(columnDefinition = "TEXT")
    private String content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    @OneToMany(mappedBy = "codeFile", cascade = CascadeType.ALL)
    private List<CodeReview> reviews;

    // --- GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // --- NEW: Added Feedback Getters/Setters ---
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public List<CodeReview> getReviews() { return reviews; }
    public void setReviews(List<CodeReview> reviews) { this.reviews = reviews; }
}