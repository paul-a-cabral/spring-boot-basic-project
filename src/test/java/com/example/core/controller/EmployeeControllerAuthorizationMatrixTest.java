package com.example.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.data.EmployeeDAO;
import com.example.core.data.EmployeeEntity;
import com.example.core.dto.EmployeeDto;
import com.example.core.security.JwtService;
import com.example.core.service.EmployeeService;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeControllerAuthorizationMatrixTest {

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean
  private EmployeeDAO employeeDAO;
  @MockitoBean
  private EmployeeService employeeService;
  @MockitoBean
  private JwtService jwtService;
  @MockitoBean
  private UserDetailsService userDetailsService;

  @Autowired
  private MockMvc mockMvc;

  Stream<Scenario> authorizationMatrix() {
    return Stream.of(
        scenario(
            "GET /api/employees with CAN_READ",
            get("/api/employees").accept(MediaType.APPLICATION_JSON)
                .with(user("reader").authorities(new SimpleGrantedAuthority("CAN_READ"))),
            200,
            () -> given(employeeDAO.findAll())
                .willReturn(List.of(new EmployeeEntity(1L, "Jane", 50000.0)))),
        scenario(
            "GET /api/employees with ROLE_ADMIN",
            get("/api/employees").accept(MediaType.APPLICATION_JSON).with(user("admin").roles("ADMIN")),
            200,
            () -> given(employeeDAO.findAll())
                .willReturn(List.of(new EmployeeEntity(1L, "Jane", 50000.0)))),
        scenario(
            "GET /api/employees/audit with CAN_AUDIT",
            get("/api/employees/audit").accept(MediaType.APPLICATION_JSON)
                .with(user("auditor").authorities(new SimpleGrantedAuthority("CAN_AUDIT"))),
            200,
            () -> {
            }),
        scenario(
            "POST /api/employees with CAN_WRITE",
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Employee\",\"salary\":65000.0}")
                .with(user("writer").authorities(new SimpleGrantedAuthority("CAN_WRITE"))),
            201,
            () -> given(employeeDAO.save(any(EmployeeEntity.class)))
                .willReturn(new EmployeeEntity(10L, "New Employee", 65000.0))),
        scenario(
            "PUT /api/employees/{id} with CAN_EDIT",
            put("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Updated Employee\",\"salary\":70000.0}")
                .with(user("editor").authorities(new SimpleGrantedAuthority("CAN_EDIT"))),
            200,
            () -> {
              given(employeeDAO.existsById(1L)).willReturn(true);
              given(employeeService.saveOrReplace(any(EmployeeDto.class)))
                  .willReturn(new EmployeeDto(1L, "Updated Employee", 70000.0));
            }),
        scenario(
            "PATCH /api/employees/{id} with CAN_EDIT",
            patch("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Patched Employee\"}")
                .with(user("editor").authorities(new SimpleGrantedAuthority("CAN_EDIT"))),
            200,
            () -> given(employeeService.update(any(EmployeeDto.class)))
                .willReturn(new EmployeeDto(1L, "Patched Employee", 50000.0))),
        scenario(
            "DELETE /api/employees/{id} with CAN_DELETE",
            delete("/api/employees/{id}", 1L)
                .with(user("deleter").authorities(new SimpleGrantedAuthority("CAN_DELETE"))),
            204,
            () -> doNothing().when(employeeService).delete(1L)));
  }

  Stream<Scenario> forbiddenMatrix() {
    return Stream.of(
        scenario("GET /api/employees without CAN_READ",
            get("/api/employees").accept(MediaType.APPLICATION_JSON).with(user("user").roles("USER"))),
        scenario("GET /api/employees/audit without CAN_AUDIT",
            get("/api/employees/audit").accept(MediaType.APPLICATION_JSON).with(user("user").roles("USER"))),
        scenario("POST /api/employees without CAN_WRITE",
            post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Employee\",\"salary\":65000.0}").with(user("user").roles("USER"))),
        scenario("PUT /api/employees/{id} without CAN_WRITE or CAN_EDIT",
            put("/api/employees/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Updated Employee\",\"salary\":70000.0}")
                .with(user("user").roles("USER"))),
        scenario("PATCH /api/employees/{id} without CAN_EDIT",
            patch("/api/employees/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Patched Employee\"}").with(user("user").roles("USER"))),
        scenario("DELETE /api/employees/{id} without CAN_DELETE",
            delete("/api/employees/{id}", 1L).with(user("user").roles("USER"))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("authorizationMatrix")
  void protectedEndpointsAllowExpectedAuthorities(
      Scenario scenario)
      throws Exception {
    scenario.setup().apply();

    mockMvc
        .perform(scenario.request())
        .andExpect(status().is(scenario.expectedStatus()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("forbiddenMatrix")
  void protectedEndpointsRejectMissingAuthorities(
      Scenario scenario)
      throws Exception {
    scenario.setup().apply();

    mockMvc
        .perform(scenario.request())
        .andExpect(status().isForbidden());
  }

  private Scenario scenario(
      String name,
      MockHttpServletRequestBuilder request,
      int expectedStatus,
      ScenarioSetup setup) {
    return new Scenario(name, request, setup, expectedStatus);
  }

  private Scenario scenario(String name, MockHttpServletRequestBuilder request) {
    return new Scenario(name, request, () -> {
    }, 403);
  }

  private record Scenario(
      String name,
      MockHttpServletRequestBuilder request,
      ScenarioSetup setup,
      int expectedStatus) {
  }

  @FunctionalInterface
  private interface ScenarioSetup {
    void apply() throws Exception;
  }
}
