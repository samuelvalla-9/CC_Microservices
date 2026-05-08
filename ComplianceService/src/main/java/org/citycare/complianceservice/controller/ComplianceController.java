package org.citycare.complianceservice.controller;

import org.citycare.complianceservice.dto.response.ApiResponse;
import org.citycare.complianceservice.dto.request.AuditRequest;
import org.citycare.complianceservice.dto.request.ComplianceRecordRequest;
import org.citycare.complianceservice.entity.Audit;
import org.citycare.complianceservice.entity.AuditLog;
import org.citycare.complianceservice.entity.ComplianceRecord;
import org.citycare.complianceservice.ServiceImplementation.ComplianceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceServiceImpl complianceServiceImpl;

    // ── Compliance Records ────────────────────────────────────────────────────

    @PostMapping("/records")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') and #officerId.toString() == authentication.name")
    public ResponseEntity<ApiResponse<ComplianceRecord>> createRecord(
            @Valid @RequestBody ComplianceRecordRequest request,
            @RequestHeader("X-Auth-UserId") Long officerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Compliance record created",
                        complianceServiceImpl.createRecord(officerId, request)));
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<ComplianceRecord>>> getAllRecords() {
        return ResponseEntity.ok(ApiResponse.ok("All compliance records", complianceServiceImpl.getAllRecords()));
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<ComplianceRecord>> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Compliance record", complianceServiceImpl.getRecordById(id)));
    }

    @GetMapping("/records/entity/{entityId}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<ComplianceRecord>>> getByEntity(@PathVariable Long entityId) {
        return ResponseEntity.ok(ApiResponse.ok("Records for entity " + entityId,
                complianceServiceImpl.getRecordsByEntity(entityId)));
    }

    @GetMapping("/records/type/{type}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<ComplianceRecord>>> getByType(
            @PathVariable ComplianceRecord.EntityType type) {
        return ResponseEntity.ok(ApiResponse.ok("Records by type: " + type,
                complianceServiceImpl.getRecordsByType(type)));
    }

    // ── Audits ────────────────────────────────────────────────────────────────

    @PostMapping("/audits")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER') and #officerId.toString() == authentication.name")
    public ResponseEntity<ApiResponse<Audit>> createAudit(
            @Valid @RequestBody AuditRequest request,
            @RequestHeader("X-Auth-UserId") Long officerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Audit created", complianceServiceImpl.createAudit(officerId, request)));
    }

    @GetMapping("/audits")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<Audit>>> getAllAudits() {
        return ResponseEntity.ok(ApiResponse.ok("All audits", complianceServiceImpl.getAllAudits()));
    }

    @GetMapping("/audits/{id}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<Audit>> getAudit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Audit", complianceServiceImpl.getAuditById(id)));
    }

    @PatchMapping("/audits/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @complianceSecurity.canAccessAudit(authentication, #id)")
    public ResponseEntity<ApiResponse<Audit>> updateAuditStatus(
            @PathVariable Long id,
            @RequestParam Audit.Status status,
            @RequestParam(required = false) String findings) {
        return ResponseEntity.ok(ApiResponse.ok("Audit updated",
                complianceServiceImpl.updateAuditStatus(id, status, findings)));
    }

    // ── Audit Logs ────────────────────────────────────────────────────────────

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLogs() {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs", complianceServiceImpl.getAllLogs()));
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Logs for user " + userId,
                complianceServiceImpl.getLogsByUser(userId)));
    }
}

