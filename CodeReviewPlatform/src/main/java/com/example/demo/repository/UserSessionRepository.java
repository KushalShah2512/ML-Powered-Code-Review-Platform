package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long>{

}
