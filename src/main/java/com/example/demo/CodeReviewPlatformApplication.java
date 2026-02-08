package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class CodeReviewPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewPlatformApplication.class, args);
    }

    // This method runs automatically when the server is ready
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8080/signup";
        System.out.println("🚀 App Started! Opening browser to: " + url);

        try {
            // Check if the generic Desktop API is supported (Standard Java way)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback specifically for Windows if the above fails
                Runtime.getRuntime().exec("cmd /c start " + url);
            }
        } catch (Exception e) {
            System.err.println("❌ Could not open browser automatically: " + e.getMessage());
        }
    }
}