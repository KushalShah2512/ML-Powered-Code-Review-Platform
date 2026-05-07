package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository; // <--- THIS LINE IS CRITICAL
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final UserRepository userRepository;

    // This "Constructor" tells Spring to give us the UserRepository
    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/test-db")
    public String testDatabase() {
        User user = new User();
        user.setUsername("Student_Dev"); // Works now because we added the setter!
        user.setEmail("test@mca.com");
        user.setPassword("mca123");

        userRepository.save(user); // Works now because we created the repository!
        
        return "Database Connection Working! User saved successfully.";
    }
}