package com.example.demo.controller;

import com.example.demo.model.CodeFile;
import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.repository.CodeFileRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository; // <--- ADDED THIS IMPORT
import com.example.demo.service.CodeAnalysisService;
import jakarta.servlet.http.HttpSession; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CodeReviewController {

    private final CodeFileRepository codeFileRepository;
    private final ProjectRepository projectRepository;
    private final CodeAnalysisService codeAnalysisService;
    private final UserRepository userRepository; // <--- ADDED THIS FIELD

    // Constructor Injection (Updated to include UserRepository)
    public CodeReviewController(CodeFileRepository codeFileRepository, 
                                ProjectRepository projectRepository, 
                                CodeAnalysisService codeAnalysisService,
                                UserRepository userRepository) { // <--- INJECTED HERE
        this.codeFileRepository = codeFileRepository;
        this.projectRepository = projectRepository;
        this.codeAnalysisService = codeAnalysisService;
        this.userRepository = userRepository;
    }

    // --- 1. ANALYZE & SAVE CODE ---
    @PostMapping("/analyze")
    public String analyzeCode(@RequestParam String codeContent, HttpSession session) {
        // 1. Get User from Session
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login"; 

        // 2. RELOAD USER FROM DB (Now this works because userRepository exists!)
        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null) return "redirect:/login";

        // 3. Run Analysis
        List<String> feedback = codeAnalysisService.analyzeCode(codeContent);
        String feedbackString = String.join(" | ", feedback);

        // 4. Find/Create Project
        Project sandboxProject = null;
        if (currentUser.getProjects() != null) {
            for (Project p : currentUser.getProjects()) {
                if (p.getProjectName().equals("Sandbox")) {
                    sandboxProject = p;
                    break;
                }
            }
        }
        
        if (sandboxProject == null) {
            sandboxProject = new Project();
            sandboxProject.setProjectName("Sandbox");
            sandboxProject.setUser(currentUser);
            projectRepository.save(sandboxProject);
        }

        // 5. Save File
        CodeFile newFile = new CodeFile();
        newFile.setContent(codeContent);
        newFile.setFileName("Snippet_" + LocalDateTime.now().toString()); 
        newFile.setProject(sandboxProject);
        newFile.setFeedback(feedbackString);
        
        codeFileRepository.save(newFile);

        return "redirect:/dashboard"; 
    }

    // --- 2. GET HISTORY API ---
    @GetMapping("/api/history")
    @ResponseBody
    public List<CodeFile> getUserHistory(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return List.of(); 

        // Fix: Use the repository to get the fresh user data here too
        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null) return List.of();

        if (currentUser.getProjects() != null) {
            for (Project p : currentUser.getProjects()) {
                if (p.getProjectName().equals("Sandbox")) {
                    return projectRepository.findById(p.getId()).get().getCodeFiles();
                }
            }
        }
        return List.of();
    }

    // --- 3. DELETE FILE ---
    @GetMapping("/delete-file")
    public String deleteFile(@RequestParam Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/login";

        codeFileRepository.deleteById(id);

        return "redirect:/dashboard";
    }
    
    // --- FULL HISTORY PAGE ---
    @GetMapping("/history")
    public String showHistoryPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        // Get fresh data
        User currentUser = userRepository.findById(user.getId()).orElse(user);
        
        // Collect ALL files from ALL projects
        List<CodeFile> allFiles = new ArrayList<>();
        if (currentUser.getProjects() != null) {
            for (Project p : currentUser.getProjects()) {
                allFiles.addAll(p.getCodeFiles());
            }
        }
        
        model.addAttribute("files", allFiles);
        return "history"; // We will create history.html next
    }
}