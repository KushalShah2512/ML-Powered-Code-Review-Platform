package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt; // <--- NEW IMPORT
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- PAGE ROUTES ---
    @GetMapping("/signup")
    public String showSignupPage() { return "signup"; }

    @GetMapping("/login")
    public String showLoginPage() { return "login"; }
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() { return "forgot_password"; }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            model.addAttribute("username", sessionUser.getUsername());
            return "dashboard";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- 1. SECURE REGISTRATION ---
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, 
                               @RequestParam String email, 
                               @RequestParam String password) {
        if (userRepository.findByEmail(email) != null) {
            return "redirect:/signup?error=exists";
        }
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        
        // 🔒 HASHING HAPPENS HERE
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        newUser.setPassword(hashedPassword);
        
        userRepository.save(newUser);
        return "redirect:/login?success=registered"; 
    }

    // --- 2. SECURE LOGIN ---
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                            @RequestParam String password,
                            HttpSession session) {
        
        User user = userRepository.findByEmail(email);

        // Check password (using BCrypt)
        if (user != null && org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPassword())) {
            session.setAttribute("loggedInUser", user);
            
            // --- NEW: ROLE CHECK ---
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin"; // Admins go here
            } else {
                return "redirect:/dashboard"; // Normal users go here
            }
            // -----------------------
            
        } else {
            return "redirect:/login?error=true";
        }
    }
    
    // --- 3. SECURE PASSWORD RESET ---
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // 🔒 HASH NEW PASSWORD BEFORE SAVING
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            
            userRepository.save(user);
            return "redirect:/login?success=reset";
        }
        return "redirect:/forgot-password?error=notfound";
    }
    
    // --- USER PROFILE PAGE ---
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
    	User user = (User) session.getAttribute("loggedInUser");
    	if (user == null) return "redirect:/login";
    	
	// Refresh user data from DB to get latest info
    	User currentUser = userRepository.findById(user.getId()).orElse(user);
    	
    	model.addAttribute("user", currentUser);
    	model.addAttribute("projectCount",currentUser.getProjects() != null ? currentUser.getProjects().size() : 0);
    	
    	return "profile"; // WE will create profile.html next
    }
}