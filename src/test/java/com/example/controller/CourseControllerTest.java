package com.example.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.core.config.SecurityConfig;
import com.example.core.security.JwtService;
import com.example.entity.Course;
import com.example.service.CourseService;
import com.example.service.EnrollmentService;
import com.example.service.StudentService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
public class CourseControllerTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Autowired @NonNull private MockMvc mockMvc;

  @MockitoBean @NonNull private CourseService courseService;

  @MockitoBean private StudentService studentService;

  @MockitoBean private EnrollmentService enrollmentService;

  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  @Test
  void listReturnsCourses() throws Exception {
    Course course = new Course(1L, "Computer Science");
    given(courseService.list()).willReturn(List.of(course));

    mockMvc
        .perform(get("/api/courses"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Computer Science")));
  }

  @Test
  void getReturnsCourseWhenFound() throws Exception {
    Course course = new Course(1L, "History");
    given(courseService.get(1L)).willReturn(Optional.of(course));

    mockMvc
        .perform(get("/api/courses/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("History"));
  }

  @Test
  void createReturnsSavedCourse() throws Exception {
    Course course = new Course(1L, "Biology");
    given(courseService.create(any(Course.class))).willReturn(course);

    mockMvc
        .perform(
            post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Biology\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Biology"));
  }

  @Test
  void updateReturnsUpdatedCourse() throws Exception {
    Course course = new Course(1L, "Art");
    given(courseService.updateName(eq(1L), eq("Art"))).willReturn(Optional.of(course));

    mockMvc
        .perform(
            put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Art\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Art"));
  }
}
