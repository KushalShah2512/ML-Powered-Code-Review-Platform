package com.example.demo.controller;

import com.example.demo.model.CodeFile; // <--- ADDED THIS IMPORT
import com.example.demo.model.User;
import com.example.demo.repository.CodeFileRepository;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final CodeFileRepository codeFileRepository;

    public AdminController(UserRepository userRepository, CodeFileRepository codeFileRepository) {
        this.userRepository = userRepository;
        this.codeFileRepository = codeFileRepository;
    }

    // --- ADMIN DASHBOARD ---
    @GetMapping("/admin")
    public String showAdminDashboard(HttpSession session, Model model) {
        // 1. Security Check: Is logged in? Is Admin?
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login"; // Kick them out if not admin
        }

        // 2. Fetch Stats
        long totalUsers = userRepository.count();
        long totalScans = codeFileRepository.count();

        // 3. Fetch All Users for the Table
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalScans", totalScans);
        model.addAttribute("adminName", user.getUsername());

        return "admin_dashboard";
    }

    // --- DELETE USER (Admin Only) ---
    @GetMapping("/admin/delete-user")
    public String deleteUser(@RequestParam Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/login";
        }

        userRepository.deleteById(id);
        return "redirect:/admin";
    }
    
    // --- UPDATED: VIEW SPECIFIC USER'S FILES ---
    @GetMapping("/admin/user-files")
    public String viewUserFiles(@RequestParam Long userId, HttpSession session, Model model) {
        // 1. Security Check
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/login";
        }

        // 2. Fetch the Target User
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) return "redirect:/admin";

        // 3. THE FIX: FETCH FILES DIRECTLY
        // This line replaces the old loop. It forces the DB to find the files.
        List<CodeFile> allFiles = codeFileRepository.findByProject_User_Id(userId);

        model.addAttribute("files", allFiles);
        model.addAttribute("targetUsername", targetUser.getUsername());
        
        return "admin_user_history"; 
    }
}