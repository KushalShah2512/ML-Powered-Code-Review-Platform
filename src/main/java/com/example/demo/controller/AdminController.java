package com.example.demo.controller;

import com.example.demo.model.CodeFile;
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

    // --- 1. ADMIN DASHBOARD ---
    @GetMapping("/admin")
    public String showAdminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalScans", codeFileRepository.count());
        model.addAttribute("adminName", user.getUsername());

        return "admin_dashboard";
    }

    // --- 2. DELETE USER ---
    @GetMapping("/admin/delete-user")
    public String deleteUser(@RequestParam Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/login";
        }

        userRepository.deleteById(id);
        return "redirect:/admin";
    }

    // --- 3. VIEW SPECIFIC USER'S FILES ---
    @GetMapping("/admin/user-files")
    public String viewUserFiles(@RequestParam Long userId, HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/login";
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) return "redirect:/admin";

        // Fetch files directly using the Repository Method we created earlier
        List<CodeFile> allFiles = codeFileRepository.findByProject_User_Id(userId);

        model.addAttribute("files", allFiles);
        model.addAttribute("targetUsername", targetUser.getUsername());
        
        return "admin_user_history"; 
    }
}
