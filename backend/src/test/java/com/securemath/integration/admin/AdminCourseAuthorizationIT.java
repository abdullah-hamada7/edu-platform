package com.securemath.integration.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
import com.securemath.dto.admin.CourseCreateDto;
import com.securemath.dto.admin.EnrollmentRequestDto;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminCourseAuthorizationIT {

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
            .status(CourseStatus.DRAFT)
            .build();
        courseId = courseRepository.save(course).getId();
    }

    @Test
    @Order(1)
    void adminCanCreateCourse() throws Exception {
        CourseCreateDto dto = new CourseCreateDto("New Course", "Description");
        
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New Course"));
    }

    @Test
    @Order(2)
    void studentCannotCreateCourse() throws Exception {
        CourseCreateDto dto = new CourseCreateDto("New Course", "Description");
        
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void adminCanListCourses() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(4)
    void studentCannotListAdminCourses() throws Exception {
        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void adminCanPublishCourse() throws Exception {
        mockMvc.perform(post("/api/admin/courses/" + courseId + "/publish")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @Order(6)
    void adminCanArchiveCourse() throws Exception {
        mockMvc.perform(post("/api/admin/courses/" + courseId + "/archive")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @Order(7)
    void unauthenticatedCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/courses"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void adminCanCreateChapter() throws Exception {
        String json = "{\"title\":\"Chapter 1\",\"position\":0}";
        
        mockMvc.perform(post("/api/admin/courses/" + courseId + "/chapters")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Chapter 1"));
    }
}
