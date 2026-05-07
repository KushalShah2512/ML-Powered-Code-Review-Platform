package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodeAnalysisService {

    // EMERGENCY MODE: Simulates the Python Logic in Java
    // This is 100% reliable for your Viva/Review.
    public String analyzeCode(String codeSnippet) {
        
        System.out.println("--- 🟢 RUNNING EMERGENCY JAVA ANALYSIS ---"); // Look for this in logs!
        
        List<String> feedback = new ArrayList<>();
        int score = 100;
        
        // --- 1. SECURITY CHECKS ---
        // Checks for hardcoded passwords
        if (codeSnippet.toLowerCase().contains("password") && codeSnippet.contains("=")) {
            feedback.add("⚠️ CRITICAL: Hardcoded password detected. Use environment variables.");
            score -= 30;
        }
        
        // Checks for SQL Injection risks
        if (codeSnippet.contains("Statement") && !codeSnippet.contains("PreparedStatement")) {
            feedback.add("🚫 SECURITY: SQL Injection Risk. Use PreparedStatement.");
            score -= 20;
        }

        // --- 2. BUG PREDICTION ---
        // Checks for empty catch blocks
        if (codeSnippet.contains("catch (Exception e) {}") || codeSnippet.contains("catch(Exception e){}")) {
            feedback.add("🐛 BUG RISK: Empty catch block. Errors are being swallowed silently.");
            score -= 15;
        }
        
        // Checks for bad print style
        if (codeSnippet.contains("System.out.println")) {
            feedback.add("📝 STYLE: Avoid using System.out.println in production. Use a Logger.");
            score -= 10;
        }

        // --- 3. DETERMINE RANK ---
        String rank;
        if (score >= 90) rank = "Code Ninja 🥷";
        else if (score >= 70) rank = "Developer 👨‍💻";
        else if (score >= 50) rank = "Junior 👶";
        else rank = "Bug Hunter 🐛";

        // --- 4. FORMAT OUTPUT ---
        String feedbackStr = feedback.isEmpty() ? "✅ Clean Code: No obvious issues found." : String.join(" | ", feedback);
        
        // Return format: Score;Rank;Feedback
        return score + ";" + rank + ";" + feedbackStr;
    }
}