package com.securemath.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
import com.securemath.repository.*;
import com.securemath.security.JwtTokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleBoundaryIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String studentToken;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        UserAccount admin = UserAccount.builder()
            .email("admin@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.ADMIN)
            .status(AccountStatus.ACTIVE)
            .build();
        admin = userRepository.save(admin);
        adminToken = jwtTokenService.generateToken(admin.getId(), admin.getEmail(), admin.getRole().name());

        UserAccount student = UserAccount.builder()
            .email("student@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        student = userRepository.save(student);
        studentToken = jwtTokenService.generateToken(student.getId(), student.getEmail(), student.getRole().name());

        Course course = Course.builder()
            .title("Test Course")
            .status(CourseStatus.PUBLISHED)
            .build();
        courseId = courseRepository.save(course).getId();
    }

    @Test
    @Order(1)
    void adminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void studentCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void studentCanAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void adminCannotAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void unauthenticatedCannotAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/courses"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/student/courses"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void publicEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"test\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    void invalidTokenRejected() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void expiredTokenRejected() throws Exception {
        String expiredToken = jwtTokenService.generateToken(
            UUID.randomUUID(), "test@test.com", "ADMIN"
        );
        
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + expiredToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void inactiveUserCannotAuthenticate() throws Exception {
        UserAccount inactive = UserAccount.builder()
            .email("inactive@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.INACTIVE)
            .build();
        inactive = userRepository.save(inactive);
        String inactiveToken = jwtTokenService.generateToken(
            inactive.getId(), inactive.getEmail(), inactive.getRole().name()
        );

        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + inactiveToken))
            .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void crossRoleAccessDenied() throws Exception {
        mockMvc.perform(post("/api/admin/courses/" + courseId + "/enrollments")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":\"" + UUID.randomUUID() + "\"}"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/student/grades")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());
    }
}
