package com.example.core.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.data.EmployeeDAO;
import com.example.core.data.EmployeeEntity;
import com.example.core.dto.EmployeeDto;
import com.example.core.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
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

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Autowired @NonNull private MockMvc mockMvc;

  @MockitoBean @NonNull private EmployeeDAO employeeDAO;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  @Test
  void testGetAllEmployees() throws Exception {
    EmployeeEntity employee1 = new EmployeeEntity(1L, "John Doe", 50000.0);
    EmployeeEntity employee2 = new EmployeeEntity(2L, "Jane Smith", 60000.0);
    List<EmployeeEntity> employees = Arrays.asList(employee1, employee2);

    given(employeeDAO.findAll()).willReturn(employees);

    mockMvc
        .perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("John Doe"))
        .andExpect(jsonPath("$[0].salary").value(50000.0))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].name").value("Jane Smith"))
        .andExpect(jsonPath("$[1].salary").value(60000.0));
  }

  @Test
  void testGetAllEmployeesEmpty() throws Exception {
    given(employeeDAO.findAll()).willReturn(Arrays.asList());

    mockMvc
        .perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void testGetEmployeeByQueryParam() throws Exception {
    Long employeeId = 1L;
    EmployeeEntity employee = new EmployeeEntity(employeeId, "Alice Johnson", 70000.0);

    given(employeeDAO.findById(employeeId)).willReturn(Optional.of(employee));

    mockMvc
        .perform(get("/api/employees").param("id", "1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Alice Johnson"))
        .andExpect(jsonPath("$.salary").value(70000.0));
  }

  @Test
  void testGetEmployeeByQueryParamNotFound() throws Exception {
    given(employeeDAO.findById(999L)).willReturn(Optional.empty());

    mockMvc
        .perform(get("/api/employees").param("id", "999").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetEmployeeByPathVariable() throws Exception {
    Long employeeId = 1L;
    EmployeeEntity employee = new EmployeeEntity(employeeId, "Bob Wilson", 55000.0);

    given(employeeDAO.findById(employeeId)).willReturn(Optional.of(employee));

    mockMvc
        .perform(get("/api/employees/{id}", employeeId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Bob Wilson"))
        .andExpect(jsonPath("$.salary").value(55000.0));
  }

  @Test
  void testGetEmployeeByPathVariableNotFound() throws Exception {
    given(employeeDAO.findById(999L)).willReturn(Optional.empty());

    mockMvc
        .perform(get("/api/employees/{id}", 999).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetEmployeeByPathVariableTypeMismatch() throws Exception {
    mockMvc
        .perform(get("/api/employees/{id}", "abc").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(containsString("Parameter 'id' must be of type Long")));
  }

  @Test
  void testGetEmployeeByPathVariableMinViolation() throws Exception {
    mockMvc
        .perform(get("/api/employees/{id}", -1).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.*", hasItem(containsString("must be greater than or equal to 1"))));
  }

  @Test
  void testSearchEmployees() throws Exception {
    mockMvc
        .perform(get("/api/employees/search").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Search results for employees")));
  }

  @Test
  void testCreateEmployee() throws Exception {
    EmployeeDto newEmployeeDto = new EmployeeDto(null, "Charlie Brown", 65000.0);
    EmployeeEntity savedEmployee = new EmployeeEntity(3L, "Charlie Brown", 65000.0);

    given(employeeDAO.save(any(EmployeeEntity.class))).willReturn(savedEmployee);

    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEmployeeDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3L))
        .andExpect(jsonPath("$.name").value("Charlie Brown"))
        .andExpect(jsonPath("$.salary").value(65000.0));
  }

  @Test
  void testCreateEmployeeWithoutName() throws Exception {
    EmployeeDto invalidEmployeeDto = new EmployeeDto(null, null, 65000.0);

    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployeeDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(containsString("name=Name is required when creating a new employee")));
  }

  @Test
  void testCreateEmployeeWithId() throws Exception {
    EmployeeDto invalidEmployeeDto = new EmployeeDto(1L, "Diana Prince", 80000.0);

    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployeeDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(
                    containsString("id=ID must be null when creating or replacing an employee")));
  }

  @Test
  void testCreateEmployeeWithMalformedJsonBody() throws Exception {
    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":null,\"name\":\"Alice\",\"salary\":65000"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(
                    containsString(
                        "The request body is malformed or contains invalid JSON syntax.")));
  }

  @Test
  void testCreateEmployeeWithMissingBody() throws Exception {
    mockMvc
        .perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(containsString("The required request body payload is entirely missing.")));
  }

  @Test
  void testReplaceEmployee() throws Exception {
    Long employeeId = 1L;
    EmployeeDto replaceEmployeeDto = new EmployeeDto(null, "Updated Name", 75000.0);
    EmployeeEntity updatedEmployee = new EmployeeEntity(employeeId, "Updated Name", 75000.0);

    given(employeeDAO.findById(employeeId)).willReturn(Optional.of(updatedEmployee));
    given(employeeDAO.save(any(EmployeeEntity.class))).willReturn(updatedEmployee);

    mockMvc
        .perform(
            put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replaceEmployeeDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(employeeId))
        .andExpect(jsonPath("$.name").value("Updated Name"))
        .andExpect(jsonPath("$.salary").value(75000.0));
  }

  @Test
  void testReplaceEmployeeNotFound() throws Exception {
    Long employeeId = 999L;
    EmployeeDto replaceEmployeeDto = new EmployeeDto(null, "Updated Name", 75000.0);

    given(employeeDAO.findById(employeeId)).willReturn(Optional.empty());

    mockMvc
        .perform(
            put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replaceEmployeeDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testPatchEmployeeNameOnly() throws Exception {
    Long employeeId = 1L;
    EmployeeEntity existingEmployee = new EmployeeEntity(employeeId, "Old Name", 55000.0);
    EmployeeEntity patchedEmployee = new EmployeeEntity(employeeId, "Patched Name", 55000.0);

    given(employeeDAO.findById(employeeId)).willReturn(Optional.of(existingEmployee));
    given(employeeDAO.save(any(EmployeeEntity.class))).willReturn(patchedEmployee);

    mockMvc
        .perform(
            patch("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Patched Name\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(employeeId))
        .andExpect(jsonPath("$.name").value("Patched Name"))
        .andExpect(jsonPath("$.salary").value(55000.0));
  }

  @Test
  void testPatchEmployeeNotFound() throws Exception {
    Long employeeId = 999L;
    given(employeeDAO.findById(employeeId)).willReturn(Optional.empty());

    mockMvc
        .perform(
            patch("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":999,\"name\":\"Patched Name\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testPatchEmployeeWithoutId() throws Exception {
    Long employeeId = 1L;

    mockMvc
        .perform(
            patch("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Patched Name\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(containsString("id=ID is required when updating an existing employee")));
  }

  @Test
  void testPatchEmployeeInvalidSalaryType() throws Exception {
    mockMvc
        .perform(
            patch("/api/employees/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"salary\":\"not-a-number\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetAllEmployeesUnexpectedError() throws Exception {
    given(employeeDAO.findAll()).willThrow(new RuntimeException("DAO exploded"));

    mockMvc
        .perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.error")
                .value(
                    containsString(
                        "An unexpected error occurred in EmployeeController.getEmployees(): DAO exploded")));
  }
}
