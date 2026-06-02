package com.futureprograms.clients.controller;

import com.futureprograms.clients.service.MyIkeaCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/myikea/customer")
@RequiredArgsConstructor
public class MyIkeaCustomerController {

    private final MyIkeaCustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PREMIUM')")
    public ResponseEntity<?> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MyIkeaCustomerService.PagedResult<Map<String, Object>> result = customerService.listCustomers(page, size);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "customers", result.items(),
                "pagination", Map.of(
                        "currentPage", result.currentPage(),
                        "totalItems", result.totalItems(),
                        "totalPages", result.totalPages(),
                        "pageSize", result.pageSize(),
                        "hasNext", result.hasNext(),
                        "hasPrevious", result.hasPrevious()
                )
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PREMIUM')")
    public ResponseEntity<?> getCustomerById(@PathVariable int id) {
        Map<String, Object> customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado"));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "customer", customer,
                "data", customer
        ));
    }

    @GetMapping("/search/firstName/{firstName}")
    @PreAuthorize("hasAnyRole('ADMIN','PREMIUM')")
    public ResponseEntity<?> searchByFirstName(
            @PathVariable String firstName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MyIkeaCustomerService.PagedResult<Map<String, Object>> result = customerService.searchByFirstName(firstName, page, size);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "customers", result.items(),
                "pagination", Map.of(
                        "currentPage", result.currentPage(),
                        "totalItems", result.totalItems(),
                        "totalPages", result.totalPages(),
                        "pageSize", result.pageSize(),
                        "hasNext", result.hasNext(),
                        "hasPrevious", result.hasPrevious()
                )
        ));
    }

    @GetMapping("/search/lastName/{lastName}")
    @PreAuthorize("hasAnyRole('ADMIN','PREMIUM')")
    public ResponseEntity<?> searchByLastName(
            @PathVariable String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MyIkeaCustomerService.PagedResult<Map<String, Object>> result = customerService.searchByLastName(lastName, page, size);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "customers", result.items(),
                "pagination", Map.of(
                        "currentPage", result.currentPage(),
                        "totalItems", result.totalItems(),
                        "totalPages", result.totalPages(),
                        "pageSize", result.pageSize(),
                        "hasNext", result.hasNext(),
                        "hasPrevious", result.hasPrevious()
                )
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomer(@PathVariable int id) {
        customerService.deleteCustomerById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Customer eliminado exitosamente"
        ));
    }
}

