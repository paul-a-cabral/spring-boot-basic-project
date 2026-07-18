package com.example.core.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.dto.EmployeeCompensationDto;
import com.example.core.dto.EmployeeDto;
import com.example.core.security.EmployeeAuthorization;
import com.example.core.security.JwtService;
import com.example.core.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Autowired @NonNull private MockMvc mockMvc;

  @MockitoBean private EmployeeService employeeService;

  @MockitoBean(name = "employeeAuthorization")
  private EmployeeAuthorization employeeAuthorization;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  @Test
  void testGetAllEmployeesRequiresAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(
            jsonPath("$.message").value("Authentication is required to access this resource"))
        .andExpect(jsonPath("$.path").value("/api/employees"));
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetAllEmployees() throws Exception {
        List<EmployeeDto> employees =
                List.of(
                        new EmployeeDto(1L, "John Doe", 50000.0, "owner-1"),
                        new EmployeeDto(2L, "Jane Smith", 60000.0, "owner-2"));

        given(employeeService.findAll()).willReturn(employees);

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
  @WithMockUser(authorities = "CAN_READ")
  void testGetAllEmployeesEmpty() throws Exception {
        given(employeeService.findAll()).willReturn(List.of());

    mockMvc
        .perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetEmployeeByQueryParam() throws Exception {
        given(employeeService.findById(1L)).willReturn(new EmployeeDto(1L, "Alice Johnson", 70000.0));

    mockMvc
        .perform(get("/api/employees").param("id", "1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Alice Johnson"))
        .andExpect(jsonPath("$.salary").value(70000.0));
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetEmployeeByQueryParamNotFound() throws Exception {
        given(employeeService.findById(999L))
                .willThrow(new com.example.core.exception.EmployeeNotFoundException("Employee not found with ID: 999"));

    mockMvc
        .perform(get("/api/employees").param("id", "999").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetEmployeeByPathVariable() throws Exception {
        Long employeeId = 1L;
        given(employeeService.findById(employeeId))
                .willReturn(new EmployeeDto(employeeId, "Bob Wilson", 55000.0));

    mockMvc
        .perform(get("/api/employees/{id}", employeeId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Bob Wilson"))
        .andExpect(jsonPath("$.salary").value(55000.0));
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetEmployeeByPathVariableNotFound() throws Exception {
        given(employeeService.findById(999L))
                .willThrow(new com.example.core.exception.EmployeeNotFoundException("Employee not found with ID: 999"));

    mockMvc
        .perform(get("/api/employees/{id}", 999).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetEmployeeByPathVariableTypeMismatch() throws Exception {
    mockMvc
        .perform(get("/api/employees/{id}", "abc").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(containsString("Parameter 'id' must be of type Long")));
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetEmployeeByPathVariableMinViolation() throws Exception {
    mockMvc
        .perform(get("/api/employees/{id}", -1).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.*", hasItem(containsString("must be greater than or equal to 1"))));
  }

    @Test
    void testGetOwnedEmployeeRequiresAuthentication() throws Exception {
        mockMvc
                .perform(get("/api/employees/{id}/owned", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void testGetOwnedEmployeeAllowsOwnerAfterReturnObjectCheck() throws Exception {
        EmployeeDto employee = new EmployeeDto(1L, "Owner Employee", 72000.0, "owner");

        given(employeeService.findById(1L)).willReturn(employee);
        given(employeeAuthorization.canAccessEmployee(eq(employee), any())).willReturn(true);

        mockMvc
                .perform(get("/api/employees/{id}/owned", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Owner Employee"))
                .andExpect(jsonPath("$.createdBy").value("owner"));
    }

    @Test
    @WithMockUser(username = "other-user", roles = "USER")
    void testGetOwnedEmployeeRejectsNonOwnerAfterReturnObjectCheck() throws Exception {
        EmployeeDto employee = new EmployeeDto(1L, "Owner Employee", 72000.0, "owner");

        given(employeeService.findById(1L)).willReturn(employee);
        given(employeeAuthorization.canAccessEmployee(eq(employee), any())).willReturn(false);

        mockMvc
                .perform(get("/api/employees/{id}/owned", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
    }

    @Test
    void testGetEmployeeCompensationRequiresAuthentication() throws Exception {
        mockMvc
                .perform(get("/api/employees/{id}/compensation", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void testGetEmployeeCompensationAllowsOwnerAfterReturnObjectCheck() throws Exception {
        EmployeeCompensationDto compensation = new EmployeeCompensationDto(1L, 72000.0, "owner");

        given(employeeService.findCompensationById(1L)).willReturn(compensation);
        given(employeeAuthorization.canAccessCompensation(eq(compensation), any())).willReturn(true);

        mockMvc
                .perform(get("/api/employees/{id}/compensation", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.salary").value(72000.0))
                .andExpect(jsonPath("$.createdBy").value("owner"));
    }

    @Test
    @WithMockUser(username = "other-user", roles = "USER")
    void testGetEmployeeCompensationRejectsNonOwnerAfterReturnObjectCheck() throws Exception {
        EmployeeCompensationDto compensation = new EmployeeCompensationDto(1L, 72000.0, "owner");

        given(employeeService.findCompensationById(1L)).willReturn(compensation);
        given(employeeAuthorization.canAccessCompensation(eq(compensation), any())).willReturn(false);

        mockMvc
                .perform(get("/api/employees/{id}/compensation", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
    }

  @Test
  @WithMockUser(roles = "USER")
  void testGetEmployeesForbiddenWithoutReadAuthority() throws Exception {
    mockMvc
        .perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(authorities = "CAN_AUDIT")
  void testAuditEmployees() throws Exception {
    mockMvc
        .perform(get("/api/employees/audit").accept(MediaType.TEXT_PLAIN))
        .andExpect(status().isOk())
        .andExpect(content().string("Audit results for employees"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void testAuditEmployeesForbiddenWithoutAuditAuthority() throws Exception {
    mockMvc
        .perform(get("/api/employees/audit").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  void testGetAuthoritiesWhenAnonymous() throws Exception {
    given(employeeService.getCurrentUserAuthorities()).willReturn(List.of());

    mockMvc
        .perform(get("/api/employees/authorities").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser(
      username = "reader",
      authorities = {"CAN_READ", "CAN_AUDIT"})
  void testGetAuthorities() throws Exception {
    List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("CAN_READ"), new SimpleGrantedAuthority("CAN_AUDIT"));

    doReturn(authorities).when(employeeService).getCurrentUserAuthorities();

    mockMvc
        .perform(get("/api/employees/authorities").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$", hasItem("CAN_READ")))
        .andExpect(jsonPath("$", hasItem("CAN_AUDIT")));
  }

  @Test
  @WithMockUser(authorities = "CAN_WRITE")
  void testCreateEmployee() throws Exception {
    EmployeeDto newEmployeeDto = new EmployeeDto(null, "Charlie Brown", 65000.0);
        EmployeeDto savedEmployee = new EmployeeDto(3L, "Charlie Brown", 65000.0);

        given(employeeService.save(any(EmployeeDto.class))).willReturn(savedEmployee);

    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEmployeeDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(3L))
        .andExpect(jsonPath("$.name").value("Charlie Brown"))
        .andExpect(jsonPath("$.salary").value(65000.0));
  }

  @Test
  @WithMockUser(roles = "USER")
  void testCreateEmployeeForbiddenWithoutWriteAuthority() throws Exception {
    EmployeeDto newEmployeeDto = new EmployeeDto(null, "Charlie Brown", 65000.0);

    mockMvc
        .perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEmployeeDto)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(authorities = "CAN_WRITE")
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
                .value(
                    containsString(
                        "name=Name is required when creating or replacinga new employee")));
  }

  @Test
  @WithMockUser(authorities = "CAN_WRITE")
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
  @WithMockUser(authorities = "CAN_WRITE")
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
  @WithMockUser(authorities = "CAN_WRITE")
  void testCreateEmployeeWithMissingBody() throws Exception {
    mockMvc
        .perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(containsString("The required request body payload is entirely missing.")));
  }

  @Test
  @WithMockUser(authorities = "CAN_WRITE")
  void testReplaceEmployeeWhenEmployeeExists() throws Exception {
    Long employeeId = 1L;
    EmployeeDto replaceEmployeeDto = new EmployeeDto(employeeId, "Updated Name", 75000.0);
    EmployeeDto savedEmployeeDto = new EmployeeDto(employeeId, "Updated Name", 75000.0);

    given(employeeService.existsById(employeeId)).willReturn(true);
    given(employeeService.saveOrReplace(any(EmployeeDto.class))).willReturn(savedEmployeeDto);

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
  @WithMockUser(authorities = "CAN_EDIT")
  void testReplaceEmployeeCreatesWhenEmployeeDoesNotExist() throws Exception {
    Long employeeId = 9L;
    EmployeeDto replaceEmployeeDto = new EmployeeDto(employeeId, "New Employee", 71000.0);
    EmployeeDto savedEmployeeDto = new EmployeeDto(employeeId, "New Employee", 71000.0);

    given(employeeService.existsById(employeeId)).willReturn(false);
    given(employeeService.saveOrReplace(any(EmployeeDto.class))).willReturn(savedEmployeeDto);

    mockMvc
        .perform(
            put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replaceEmployeeDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(employeeId))
        .andExpect(jsonPath("$.name").value("New Employee"))
        .andExpect(jsonPath("$.salary").value(71000.0));
  }

  @Test
  @WithMockUser(roles = "USER")
  void testReplaceEmployeeForbiddenWithoutWriteOrEditAuthority() throws Exception {
    EmployeeDto replaceEmployeeDto = new EmployeeDto(1L, "Updated Name", 75000.0);

    mockMvc
        .perform(
            put("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replaceEmployeeDto)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(authorities = "CAN_EDIT")
  void testPatchEmployeeNameOnly() throws Exception {
    Long employeeId = 1L;
    EmployeeDto patchedEmployee = new EmployeeDto(employeeId, "Patched Name", 55000.0);

    given(employeeService.update(any(EmployeeDto.class))).willReturn(patchedEmployee);

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
  @WithMockUser(roles = "USER")
  void testPatchEmployeeForbiddenWithoutEditAuthority() throws Exception {
    mockMvc
        .perform(
            patch("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Patched Name\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(authorities = "CAN_EDIT")
  void testPatchEmployeeNotFound() throws Exception {
    given(employeeService.update(any(EmployeeDto.class)))
        .willThrow(
            new jakarta.persistence.EntityNotFoundException("Employee not found with ID: 999"));

    mockMvc
        .perform(
            patch("/api/employees/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":999,\"name\":\"Patched Name\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.error")
                .value(
                    containsString(
                        "An unexpected error occurred in EmployeeController.patchEmployee(): Employee not found with ID: 999")));
  }

  @Test
  @WithMockUser(authorities = "CAN_EDIT")
  void testPatchEmployeeWithoutId() throws Exception {
    mockMvc
        .perform(
            patch("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Patched Name\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error")
                .value(containsString("id=ID is required when updating an existing employee")));
  }

  @Test
  @WithMockUser(authorities = "CAN_EDIT")
  void testPatchEmployeeInvalidSalaryType() throws Exception {
    mockMvc
        .perform(
            patch("/api/employees/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"salary\":\"not-a-number\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "CAN_DELETE")
  void testDeleteEmployee() throws Exception {
    doNothing().when(employeeService).delete(1L);

    mockMvc.perform(delete("/api/employees/{id}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "owner", roles = "USER")
  void testDeleteEmployeeAllowedWhenUserIsOwner() throws Exception {
    given(employeeAuthorization.canDeleteOwnEmployee(eq(1L), any())).willReturn(true);
    doNothing().when(employeeService).delete(1L);

    mockMvc.perform(delete("/api/employees/{id}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  void testDeleteEmployeeForbiddenWithoutDeleteAuthority() throws Exception {
    given(employeeAuthorization.canDeleteOwnEmployee(eq(1L), any())).willReturn(false);

    mockMvc
        .perform(delete("/api/employees/{id}", 1L))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(authorities = "CAN_READ")
  void testGetAllEmployeesUnexpectedError() throws Exception {
        given(employeeService.findAll()).willThrow(new RuntimeException("DAO exploded"));

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
