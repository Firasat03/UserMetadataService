package com.example.userservice.service;

import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.dto.UserResponseDTO;
import com.example.userservice.exception.DuplicateUserException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // @Transactional
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackCreateUser")
    @Retry(name = "userService", fallbackMethod = "createUserFallback")
    public String createUser(UserRequestDTO userRequestDTO) {
        log.info("UserService.createUser called with: " + userRequestDTO);

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPhone(userRequestDTO.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        try {
            User savedUser = userRepository.save(user);
            return "User created successfully";
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException("User with email " + userRequestDTO.getEmail() + " already exists");
        }
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt());
    }

    public Optional<UserResponseDTO> getUser(Long id) {
        return userRepository.findById(id).map(this::mapToResponseDTO);
    }

    // Fallback method for Circuit Breaker
    public String fallbackCreateUser(UserRequestDTO userRequestDTO, Throwable t) {
        if (t instanceof DuplicateUserException || t instanceof IllegalArgumentException) {
            throw (RuntimeException) t;
        }
        // Log the error
        System.err.println("Fallback triggered for createUser: " + t.getMessage());
        throw new RuntimeException("Service Unavailable: " + t.getMessage());
    }

    public String createUserFallback(UserRequestDTO userRequestDTO, Throwable t) {
        if (t instanceof DuplicateUserException || t instanceof IllegalArgumentException) {
            throw (RuntimeException) t;
        }
        // Log the error
        System.err.println("Fallback triggered for createUser: " + t.getMessage());
        throw new RuntimeException("Service Unavailable: " + t.getMessage());
    }
}
