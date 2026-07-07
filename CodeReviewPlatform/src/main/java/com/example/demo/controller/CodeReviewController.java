package com.example.demo.controller;

import com.example.demo.model.CodeFile;
import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.repository.CodeFileRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CodeAnalysisService;
import com.example.demo.service.PdfService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // <--- NEW IMPORT FOR ENVIRONMENT VARIABLES
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

// --- IMPORTS FOR THE AI BRIDGE ---
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
// -------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CodeReviewController {

    private final CodeFileRepository codeFileRepository;
    private final ProjectRepository projectRepository;
    private final CodeAnalysisService codeAnalysisService;
    private final UserRepository userRepository;

    @Autowired
    private PdfService pdfService; 

    // --- CLOUD READY ENVIRONMENT VARIABLE ---
    // This pulls the URL from application.properties so it works locally AND in the cloud!
    @Value("${ai.engine.url}")
    private String aiEngineUrl;

    public CodeReviewController(CodeFileRepository codeFileRepository, 
                                ProjectRepository projectRepository, 
                                CodeAnalysisService codeAnalysisService,
                                UserRepository userRepository) {
        this.codeFileRepository = codeFileRepository;
        this.projectRepository = projectRepository;
        this.codeAnalysisService = codeAnalysisService;
        this.userRepository = userRepository;
    }

    // --- 1. HANDLE FILE UPLOAD ---
    @PostMapping("/analyze-file")
    public String analyzeFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        if (file.isEmpty()) {
            System.out.println("DEBUG: File is empty!");
            return "redirect:/dashboard?error=emptyfile";
        }

        try {
            // Read file content
            String content = new BufferedReader(new InputStreamReader(file.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            System.out.println("DEBUG: File received. Size: " + content.length());

            // Pass to existing logic
            return analyzeCode(content, session);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/dashboard?error=readerror";
        }
    }

    // --- 2. ANALYZE TEXT (THE PYTHON BRIDGE) ---
    @PostMapping("/analyze")
    public String analyzeCode(@RequestParam String codeContent, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login"; 

        // Reload user to get fresh data
        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null) return "redirect:/login";

        // ==========================================
        // 🚀 THE AI BRIDGE: JAVA TO PYTHON
        // ==========================================
        int score = 0;
        String feedbackText = "";

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // 👉 THE FIX: Uses the dynamic variable instead of a hardcoded string
            String pythonAiUrl = aiEngineUrl + "/analyze";

            // 1. Package the code into JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("codeContent", codeContent);
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 2. Send to Python and wait for the response
            System.out.println("DEBUG: Sending code to Python AI Engine at " + pythonAiUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonAiUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // 3. Extract the data Python sent back
            if (responseBody != null) {
                score = (Integer) responseBody.getOrDefault("score", 0);
                feedbackText = (String) responseBody.getOrDefault("feedback", "No feedback provided.");
                
                // Attach the issues
                List<String> issues = (List<String>) responseBody.get("issues");
                if (issues != null && !issues.isEmpty()) {
                    feedbackText += " | Detected: " + String.join(", ", issues);
                }
                
                // Attach the actionable fixes!
                List<String> fixes = (List<String>) responseBody.get("fixes");
                if (fixes != null && !fixes.isEmpty()) {
                    feedbackText += " | Action Required: " + String.join(" ", fixes);
                }
            }
        } catch (Exception e) {
            System.out.println("🚨 ERROR: Could not connect to Python AI Engine.");
            System.out.println("Error details: " + e.getMessage());
            score = 0;
            feedbackText = "AI Engine Offline: Ensure the Python server is running at " + aiEngineUrl;
        }
        // ==========================================

        // Find/Create "Sandbox" Project
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
            System.out.println("DEBUG: Creating new Sandbox Project");
            sandboxProject = new Project();
            sandboxProject.setProjectName("Sandbox");
            sandboxProject.setUser(currentUser);
            projectRepository.save(sandboxProject);
        }

        // Save File
        CodeFile newFile = new CodeFile();
        newFile.setContent(codeContent);
        newFile.setFileName("Scan_" + LocalDateTime.now().toString()); 
        newFile.setProject(sandboxProject);
        newFile.setFeedback(feedbackText + " [Score: " + score + "/100]"); 
        
        codeFileRepository.save(newFile);
        System.out.println("DEBUG: File saved to DB with ID: " + newFile.getId());

        return "redirect:/dashboard"; 
    }

    // --- 3. GET HISTORY API ---
    @GetMapping("/api/history")
    @ResponseBody
    public List<CodeFile> getUserHistory(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return List.of(); 

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (currentUser == null) return List.of();

        List<CodeFile> allFiles = new ArrayList<>();
        if (currentUser.getProjects() != null) {
            for (Project p : currentUser.getProjects()) {
                allFiles.addAll(p.getCodeFiles());
            }
        }
        return allFiles;
    }

    // --- 4. DELETE FILE ---
    @GetMapping("/delete-file")
    public String deleteFile(@RequestParam Long id, HttpSession session) {
        codeFileRepository.deleteById(id);
        return "redirect:/dashboard"; 
    }
    
    // --- 5. FULL HISTORY PAGE ---
    @GetMapping("/history")
    public String showHistoryPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        User currentUser = userRepository.findById(user.getId()).orElse(user);
        
        List<CodeFile> allFiles = new ArrayList<>();
        if (currentUser.getProjects() != null) {
            for (Project p : currentUser.getProjects()) {
                allFiles.addAll(p.getCodeFiles());
            }
        }
        
        model.addAttribute("files", allFiles);
        return "history";
    }

    // --- 6. PDF DOWNLOAD ---
    @GetMapping("/download-pdf/{id}")
    public void generatePdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        CodeFile file = codeFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Code_Analysis_" + file.getId() + ".pdf";
        response.setHeader(headerKey, headerValue);

        pdfService.export(response, file);
    }
}