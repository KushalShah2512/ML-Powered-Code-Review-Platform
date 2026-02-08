package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodeAnalysisService {

    public List<String> analyzeCode(String sourceCode) {
        List<String> issues = new ArrayList<>();

        if (sourceCode == null) return List.of("⚠️ Error: No code provided.");

        // Rule 1: Check for System.out.println
        if (sourceCode.contains("System.out.println")) {
            issues.add("⚠️ Critical: Avoid using 'System.out.println'. Use a Logger (SLF4J) instead.");
        }

        // Rule 2: Check for hardcoded passwords
        if (sourceCode.contains("password = \"") || sourceCode.contains("password=\"")) {
            issues.add("🔒 Security: Hardcoded password detected. Use environment variables.");
        }

        // Rule 3: Check for empty catch blocks
        if (sourceCode.contains("catch (Exception e) {}") || sourceCode.contains("catch(Exception e){}")) {
            issues.add("🐛 Bug Risk: Empty catch block detected. Always log exceptions.");
        }

        // If no issues found
        if (issues.isEmpty()) {
            issues.add("✅ Clean Code: No obvious issues found.");
        }

        return issues;
    }
}