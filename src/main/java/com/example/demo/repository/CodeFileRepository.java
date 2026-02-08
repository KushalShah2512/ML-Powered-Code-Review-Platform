package com.example.demo.repository;

import com.example.demo.model.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {
    
    // ✨ MAGIC METHOD: Spring Boot automatically converts this name into a SQL query!
    // It finds files where Project -> User -> ID matches the parameter.
    List<CodeFile> findByProject_User_Id(Long userId);
}