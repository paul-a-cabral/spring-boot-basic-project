package com.example.controller;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.controller.StudentController;
import com.example.entity.Student;
import com.example.service.CourseService;
import com.example.service.EnrollmentService;
import com.example.service.StudentService;

// @WebMvcTest(StudentController.class)
// class StudentControllerTest {

//     @MockBean
//     private StudentService studentService;

//     // Add these two mocks:
//     @MockBean
//     private CourseService courseService;

//     @MockBean
//     private EnrollmentService enrollmentService;

//     // ... your tests
// }

@WebMvcTest(StudentController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class StudentControllerTest {

    @Autowired
    @NonNull
    private MockMvc mockMvc;

    @MockBean
    @NonNull
    private StudentService studentService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private EnrollmentService enrollmentService;

    @Test
    void searchByEmailReturnsStudentWhenFound() throws Exception {
        Student student = new Student(1L, "Jane Doe", "jane.doe@example.com");

        given(studentService.findByEmail("jane.doe@example.com")).willReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/search")
                        .param("email", "jane.doe@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jane.doe@example.com")));
    }

    @Test
    void searchByEmailReturnsNotFoundWhenMissing() throws Exception {
        given(studentService.findByEmail("missing@example.com")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/students/search")
                        .param("email", "missing@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}