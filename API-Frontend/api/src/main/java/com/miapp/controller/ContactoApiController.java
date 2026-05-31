package com.miapp.controller;

import com.miapp.dto.ContactoRequestDto;
import com.miapp.dto.ContactoResponseDto;
import com.miapp.service.ContactoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContactoApiController {

    private final ContactoService contactoService;

    public ContactoApiController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @PostMapping("/contact")
    public ResponseEntity<ContactoResponseDto> enviar(@Valid @RequestBody ContactoRequestDto request) {
        contactoService.enviar(request);
        return ResponseEntity.ok(new ContactoResponseDto("Nos pondremos en contacto contigo en breve."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problem.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }
}
