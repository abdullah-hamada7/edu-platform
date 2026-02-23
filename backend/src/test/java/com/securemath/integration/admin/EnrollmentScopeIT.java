package com.securemath.integration.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnrollmentScopeIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String studentToken;
    private String unenrolledStudentToken;
    private UUID courseId;
    private UUID studentId;
    private UUID unenrolledStudentId;

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
        studentId = student.getId();
        studentToken = jwtTokenService.generateToken(student.getId(), student.getEmail(), student.getRole().name());

        UserAccount unenrolledStudent = UserAccount.builder()
            .email("unenrolled@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        unenrolledStudent = userRepository.save(unenrolledStudent);
        unenrolledStudentId = unenrolledStudent.getId();
        unenrolledStudentToken = jwtTokenService.generateToken(unenrolledStudent.getId(), unenrolledStudent.getEmail(), unenrolledStudent.getRole().name());

        Course course = Course.builder()
            .title("Test Course")
            .status(CourseStatus.PUBLISHED)
            .build();
        courseId = courseRepository.save(course).getId();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);
    }

    @Test
    @Order(1)
    void adminCanEnrollStudent() throws Exception {
        EnrollmentRequestDto dto = new EnrollmentRequestDto(unenrolledStudentId);
        
        mockMvc.perform(post("/api/admin/courses/" + courseId + "/enrollments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(2)
    void enrolledStudentCanSeeCourse() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + courseId + "')]").exists());
    }

    @Test
    @Order(3)
    void unenrolledStudentCannotSeeCourse() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + unenrolledStudentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + courseId + "')]").doesNotExist());
    }

    @Test
    @Order(4)
    void adminCannotEnrollSameStudentTwice() throws Exception {
        EnrollmentRequestDto dto = new EnrollmentRequestDto(studentId);
        
        mockMvc.perform(post("/api/admin/courses/" + courseId + "/enrollments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict());
    }

    @Test
    @Order(5)
    void adminCanRemoveEnrollment() throws Exception {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).orElseThrow();
        
        mockMvc.perform(delete("/api/admin/courses/" + courseId + "/enrollments/" + enrollment.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
        
        Enrollment removed = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assert removed.getStatus() == EnrollmentStatus.REMOVED;
    }

    @Test
    @Order(6)
    void inactiveEnrollmentDoesNotGrantAccess() throws Exception {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).orElseThrow();
        enrollment.setStatus(EnrollmentStatus.REMOVED);
        enrollmentRepository.save(enrollment);

        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + courseId + "')]").doesNotExist());
    }
}
