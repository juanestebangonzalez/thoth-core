package com.thoth.adapter.in.rest.controller;

import com.thoth.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/technicians")
@Tag(name = "Technicians", description = "Lista de tecnicos disponibles")
@RequiredArgsConstructor
public class TechnicianController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar tecnicos activos")
    public ResponseEntity<List<UserService.TechnicianInfo>> listTechnicians() {
        return ResponseEntity.ok(userService.listTechnicians());
    }
}
