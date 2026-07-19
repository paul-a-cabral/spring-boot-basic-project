package com.example.core.security;

import com.example.core.security.dto.AuthenticationRequest;
import com.example.core.security.dto.AuthenticationResponse;
import com.example.core.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "authentication", description = "Authentication operations")
public class AuthenticationController {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final EmployeeService employeeService;

  public AuthenticationController(
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      JwtService jwtService,
      EmployeeService employeeService) {
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.jwtService = jwtService;
    this.employeeService = employeeService;
  }

  @GetMapping("/authorities")
  public List<String> getAuthorities() {
    List<String> authorities =
        employeeService.getCurrentUserAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

    logger.info("Current user authorities: {}", authorities);
    return authorities;
  }

  // other than the annotation @PostMapping, the rest of the annotations
  // are OpenAPI annotations to the login endpoint now so Swagger shows a clear
  // summary, request schema, and response codes
  @Operation(
      summary = "Authenticate user credentials",
      description =
          "Validates username and password and returns a success message when authentication passes")
  @SecurityRequirements
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @PostMapping("/login")
  public AuthenticationResponse login(@RequestBody AuthenticationRequest request) {

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    UserDetails user = userDetailsService.loadUserByUsername(request.username());

    String jwt = jwtService.generateToken(user);

    return new AuthenticationResponse(jwt);
  }
}
