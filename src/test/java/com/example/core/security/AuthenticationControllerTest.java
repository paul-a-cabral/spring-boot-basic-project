package com.example.core.security;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
class AuthenticationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private UserDetailsService userDetailsService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private EmployeeService employeeService;

  @Test
  void getAuthoritiesReturnsEmptyListForAnonymousRequest() throws Exception {
    given(employeeService.getCurrentUserAuthorities()).willReturn(List.of());

    mockMvc
        .perform(get("/api/auth/authorities").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser(
      username = "reader",
      authorities = {"CAN_READ", "CAN_AUDIT"})
  void getAuthoritiesReturnsAuthoritiesForAuthenticatedUser() throws Exception {
    List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("CAN_READ"), new SimpleGrantedAuthority("CAN_AUDIT"));

    doReturn(authorities).when(employeeService).getCurrentUserAuthorities();

    mockMvc
        .perform(get("/api/auth/authorities").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$", hasItem("CAN_READ")))
        .andExpect(jsonPath("$", hasItem("CAN_AUDIT")));
  }
}
