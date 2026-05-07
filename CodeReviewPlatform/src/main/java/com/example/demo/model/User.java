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
    private String apiKey;
    
    private String resetToken;
    // --- RELATIONSHIP: One User has Many Projects ---
    // mappedBy = "user" refers to the 'user' field in the Project.java class
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Project> projects;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserSession> activeSessions;
    
    private boolean criticalAlerts = true;
    private boolean weeklyReports = false;
    
    // --- NEW: PROFILE PICTURE STORAGE ---
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profilePicture;
    
    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public void setApiKey(String apiKey) {this.apiKey = apiKey; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public void setActiveSessions(List<UserSession> activeSessions) { this.activeSessions = activeSessions; }
    
    public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }
    // --- GETTERS ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getApiKey() { return apiKey; }
    
    public boolean isCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(boolean criticalAlerts) { this.criticalAlerts = criticalAlerts; }

    public boolean isWeeklyReports() { return weeklyReports; }
    public void setWeeklyReports(boolean weeklyReports) { this.weeklyReports = weeklyReports; }
   
    // This is the method the Controller needs to find the "Sandbox" project!
    public List<Project> getProjects() { return projects; }
    
    public List<UserSession> getActiveSessions() { return activeSessions; }
    
    public byte[] getProfilePicture() { return profilePicture; }
    
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    
}