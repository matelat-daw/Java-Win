package com.ejemplo.modulo3demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {
    
    private static final System.Logger log = System.getLogger(DemoController.class.getName());
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public DemoController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/publico")
    public String publico() {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint publico");
        return "Ruta publica: cualquiera puede verla";
    }

    @GetMapping("/privado")
    public String privado() {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint privado");
        return "Ruta privada: solo usuarios autenticados";
    }

    @GetMapping("/admin")
    public String admin() {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint de administracion");
        return "Ruta de administracion: solo ADMIN";
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint de login para {0}", request.username());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            var securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            return ResponseEntity.ok(AuthUserResponse.from(authentication));
        } catch (AuthenticationException exception) {
            log.log(System.Logger.Level.WARNING, "Login fallido para {0}", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiMessageResponse("Credenciales inválidas"));
        }
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiMessageResponse("No hay una sesión activa"));
        }

        return ResponseEntity.ok(AuthUserResponse.from(authentication));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiMessageResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint de logout");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logoutHandler.logout(httpRequest, httpResponse, authentication);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(new ApiMessageResponse("Sesión cerrada correctamente"));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record AuthUserResponse(String username, List<String> roles, boolean authenticated) {
        static AuthUserResponse from(Authentication authentication) {
            return new AuthUserResponse(
                    authentication.getName(),
                    authentication.getAuthorities().stream().map(authority -> authority.getAuthority()).toList(),
                    authentication.isAuthenticated());
        }
    }

    public record ApiMessageResponse(String message) {
    }
}