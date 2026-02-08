package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List; // <--- IMPORTANT IMPORT

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String role = "USER";
    // --- RELATIONSHIP: One User has Many Projects ---
    // mappedBy = "user" refers to the 'user' field in the Project.java class
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Project> projects;

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    // --- GETTERS ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    // This is the method the Controller needs to find the "Sandbox" project!
    public List<Project> getProjects() { return projects; }
}