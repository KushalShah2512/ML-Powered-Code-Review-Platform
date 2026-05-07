package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSessionRepository;

import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.UUID;
 
@Controller
public class UserController {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository; 

    public UserController(UserRepository userRepository, UserSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    // --- PAGE ROUTES ---
    
    @GetMapping("/")
    public String home() { return "redirect:/login";}
    
    @GetMapping("/signup")
    public String showSignupPage() { return "signup"; }

    @GetMapping("/login")
    public String showLoginPage() { return "login"; }
    
    // --- DASHBOARD WITH CHARTS LOGIC ---
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            User currentUser = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
            
            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("role", currentUser.getRole());

            int safeCount = 0;
            int issueCount = 0;
            int totalScans = 0;

            if (currentUser.getProjects() != null) {
                for (com.example.demo.model.Project p : currentUser.getProjects()) {
                    for (com.example.demo.model.CodeFile f : p.getCodeFiles()) {
                        totalScans++;
                        String feedback = (f.getFeedback() != null) ? f.getFeedback().toLowerCase() : "";
                        
                        if (feedback.contains("critical") || feedback.contains("risk") || feedback.contains("vulnerability")) {
                            issueCount++;
                        } else {
                            safeCount++;
                        }
                    }
                }
            }
            
            model.addAttribute("totalScans", totalScans);
            model.addAttribute("safeCount", safeCount);
            model.addAttribute("issueCount", issueCount);

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

    // --- SECURE REGISTRATION ---
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
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        newUser.setPassword(hashedPassword);
        
        userRepository.save(newUser);
        return "redirect:/login?success=registered"; 
    }

    // --- SECURE LOGIN WITH DEVICE TRACKING ---
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                            @RequestParam String password,
                            HttpSession session,
                            HttpServletRequest request) { 
        
        User user = userRepository.findByEmail(email);

        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            session.setAttribute("loggedInUser", user);
            
            // --- CAPTURE DEVICE INFO ---
            String userAgent = request.getHeader("User-Agent");
            String device = "Unknown Device";
            
            if (userAgent != null) {
                if (userAgent.contains("Windows")) device = "Windows PC";
                else if (userAgent.contains("Mac")) device = "MacBook";
                else if (userAgent.contains("Android")) device = "Android Mobile";
                else if (userAgent.contains("iPhone")) device = "iPhone";
                else if (userAgent.contains("Linux")) device = "Linux Machine";
            }

            com.example.demo.model.UserSession userSession = new com.example.demo.model.UserSession();
            userSession.setDeviceName(device);
            userSession.setIpAddress(request.getRemoteAddr());
            userSession.setLoginTime(java.time.LocalDateTime.now());
            userSession.setUser(user);
            
            sessionRepository.save(userSession); 
            // --------------------------------
            
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin"; 
            } else {
                return "redirect:/dashboard"; 
            }
            
        } else {
            return "redirect:/login?error=true";
        }
    }
    
    // ==========================================
    // --- 10. ADVANCED FORGOT PASSWORD FLOW ---
    // ==========================================
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() { 
        return "forgot_password"; 
    }

    // Step 1: User submits their email
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // Generate a secure, random token
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user);

            // SIMULATE SENDING EMAIL (Prints to your Eclipse Console)
            System.out.println("\n========================================");
            System.out.println("🚨 PASSWORD RESET EMAIL INTERCEPTED 🚨");
            System.out.println("To: " + user.getEmail());
            System.out.println("Click this link to reset your password:");
            System.out.println("http://localhost:8080/reset-password?token=" + token);
            System.out.println("========================================\n");
        }
        // Always show success even if email doesn't exist (prevents hackers from guessing valid emails)
        return "redirect:/login?msg=resetLinkSent";
    }

    // Step 2: User clicks the link in their "email"
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam("token") String token, Model model) {
        // Find user by token
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken()))
                .findFirst().orElse(null);
        
        if (user == null) {
            return "redirect:/login?error=invalidToken";
        }
        
        // Pass the valid token to the HTML form
        model.addAttribute("token", token);
        return "reset_password";
    }

    // Step 3: User submits their new password
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken()))
                .findFirst().orElse(null);

        if (user != null) {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            user.setResetToken(null); // Destroy the token so it can't be used again
            userRepository.save(user);
            return "redirect:/login?success=passwordResetSuccess";
        }
        return "redirect:/login?error=invalidToken";
    }
    // ==========================================
    
    // --- USER PROFILE PAGE ---
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login";
        
        User user = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
        
        if (user.getApiKey() == null || user.getApiKey().isEmpty()) {
            user.setApiKey("sk-cra-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            userRepository.save(user);
        }

        int scanCount = 0;
        int javaCount = 0;
        int pythonCount = 0;

        if (user.getProjects() != null) {
            for (com.example.demo.model.Project p : user.getProjects()) {
                for (com.example.demo.model.CodeFile f : p.getCodeFiles()) {
                    scanCount++;
                    if (f.getContent().contains("public class") || f.getContent().contains("System.out")) {
                        javaCount++;
                    } else {
                        pythonCount++;
                    }
                }
            }
        }

        String topLang = (scanCount == 0) ? "None" : (javaCount >= pythonCount ? "Java" : "Python");
        String rank = scanCount < 5 ? "Lvl 1: Novice" : (scanCount < 15 ? "Lvl 2: Secure Coder" : "Lvl 3: Security Master");

        model.addAttribute("user", user);
        model.addAttribute("projectCount", scanCount);
        model.addAttribute("topLanguage", topLang);
        model.addAttribute("rank", rank);
        
        return "profile"; 
    }
    
    // --- REGENERATE API KEY ---
    @PostMapping("/regenerate-api-key")
    public String regenerateApiKey(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            User user = userRepository.findById(sessionUser.getId()).orElse(null);
            if (user != null) {
                user.setApiKey("sk-cra-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16));
                userRepository.save(user);
            }
        }
        return "redirect:/profile?success=keyrolled";
    }
    
    // --- UPDATE NOTIFICATIONS ---
    @PostMapping("/update-preferences")
    public String updatePreferences(@RequestParam(required = false) String criticalAlerts, 
            @RequestParam(required = false) String weeklyReports, 
            HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            User user = userRepository.findById(sessionUser.getId()).orElse(null);
            if (user != null) {
                user.setCriticalAlerts(criticalAlerts != null);
                user.setWeeklyReports(weeklyReports != null);
                userRepository.save(user);
            }
        }
        return "redirect:/profile?success=prefs";
    }
    
    // --- DELETE ACCOUNT LOGIC ---
    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            userRepository.deleteById(sessionUser.getId());
            session.invalidate();
        }
        return "redirect:/login?success=deleted";
    }
    
    // --- UPDATE PROFILE DETAILS ---
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String username, @RequestParam String email, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            User user = userRepository.findById(sessionUser.getId()).orElse(null);
            if (user != null) {
                user.setUsername(username);
                user.setEmail(email);
                userRepository.save(user);
                session.setAttribute("loggedInUser", user);
            }
        }
        return "redirect:/profile?success=profileUpdated";
    }

    // --- ADVANCED SECURE PASSWORD UPDATE ---
    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String currentPassword, 
                                 @RequestParam String newPassword, 
                                 @RequestParam String confirmPassword, 
                                 HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null) {
            User user = userRepository.findById(sessionUser.getId()).orElse(null);
            if (user != null) {
                if (!org.mindrot.jbcrypt.BCrypt.checkpw(currentPassword, user.getPassword())) {
                    return "redirect:/profile?error=wrongpassword";
                }
                if (!newPassword.equals(confirmPassword)) {
                    return "redirect:/profile?error=mismatch";
                }
                String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
                user.setPassword(hashedPassword);
                userRepository.save(user);
            }
        }
        return "redirect:/profile?success=passwordUpdated";
    }

    // --- NEW: REVOKE SESSION ---
    @PostMapping("/revoke-session")
    public String revokeSession(@RequestParam Long sessionId) {
        sessionRepository.deleteById(sessionId);
        return "redirect:/profile?success=sessionRevoked";
    }
    
    // --- NEW: GDPR DATA EXPORT (CSV) ---
    @GetMapping("/export-data")
    public void exportUserData(HttpSession session, HttpServletResponse response) throws IOException {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            response.sendRedirect("/login");
            return;
        }

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"userdata_" + user.getUsername() + ".csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("--- ACCOUNT DETAILS ---");
            writer.println("User ID,Username,Email,Role,Account Created");
            writer.println(user.getId() + "," + user.getUsername() + "," + user.getEmail() + "," + user.getRole() + ",Active");
            writer.println();

            writer.println("--- CODE SCAN HISTORY ---");
            writer.println("Scan ID,File Name,Score & Feedback");

            if (user.getProjects() != null) {
                for (com.example.demo.model.Project p : user.getProjects()) {
                    for (com.example.demo.model.CodeFile f : p.getCodeFiles()) {
                        String safeFeedback = (f.getFeedback() != null) ? f.getFeedback().replace("\"", "\"\"") : "No feedback";
                        writer.println(f.getId() + "," + f.getFileName() + ",\"" + safeFeedback + "\"");
                    }
                }
            }
        }
    }
    
    // --- UPLOAD PROFILE PICTURE ---
    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") org.springframework.web.multipart.MultipartFile file, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null && !file.isEmpty()) {
            try {
                User user = userRepository.findById(sessionUser.getId()).orElse(null);
                if (user != null) {
                    user.setProfilePicture(file.getBytes());
                    userRepository.save(user);
                    session.setAttribute("loggedInUser", user);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return "redirect:/profile?error=uploadfailed";
            }
        }
        return "redirect:/profile?success=avatarUploaded";
    }

    // --- DISPLAY PROFILE PICTURE ---
    @GetMapping("/avatar/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> getAvatar(@org.springframework.web.bind.annotation.PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && user.getProfilePicture() != null) {
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                    .body(user.getProfilePicture());
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}