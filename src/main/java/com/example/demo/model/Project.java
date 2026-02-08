package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CodeFile> codeFiles;

    // --- GETTERS & SETTERS (These fix your red lines!) ---
    public Long getId() { return id; }
    
    public String getProjectName() { return projectName; } // Fixes getProjectName() error
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; } // Fixes setUser() error

    public List<CodeFile> getCodeFiles() { return codeFiles; }
    public void setCodeFiles(List<CodeFile> codeFiles) { this.codeFiles = codeFiles; }
}