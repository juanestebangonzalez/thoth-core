package com.thoth.adapter.in.rest.controller;

import com.thoth.application.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/qr")
@Tag(name = "QR Code", description = "Generacion de codigos QR para equipos")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @GetMapping("/{equipmentId}")
    @Operation(summary = "Generar QR code PNG para un equipo")
    public ResponseEntity<byte[]> generateQr(
            @PathVariable UUID equipmentId,
            @RequestParam(required = false, defaultValue = "https://thoth-core.netlify.app") String baseUrl) {
        try {
            byte[] qrImage = qrCodeService.generateQrCode(equipmentId, baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.set("Content-Disposition", "inline; filename=qr-" + equipmentId + ".png");

            return ResponseEntity.ok().headers(headers).body(qrImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{equipmentId}/download")
    @Operation(summary = "Descargar QR code PNG para imprimir")
    public ResponseEntity<byte[]> downloadQr(
            @PathVariable UUID equipmentId,
            @RequestParam(required = false, defaultValue = "https://thoth-core.netlify.app") String baseUrl) {
        try {
            byte[] qrImage = qrCodeService.generateQrCode(equipmentId, baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.set("Content-Disposition", "attachment; filename=QR-" + equipmentId + ".png");

            return ResponseEntity.ok().headers(headers).body(qrImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}