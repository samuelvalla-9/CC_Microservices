package org.citycare.complianceservice.ServiceImplementation;

import org.citycare.complianceservice.dto.request.AuditRequest;
import org.citycare.complianceservice.dto.request.ComplianceRecordRequest;
import org.citycare.complianceservice.entity.Audit;
import org.citycare.complianceservice.entity.AuditLog;
import org.citycare.complianceservice.entity.ComplianceRecord;
import org.citycare.complianceservice.exception.ResourceNotFoundException;
import org.citycare.complianceservice.feign.EmergencyClient;
import org.citycare.complianceservice.feign.FacilityClient;
import org.citycare.complianceservice.feign.NotificationClient;
import org.citycare.complianceservice.feign.PatientClient;
import org.citycare.complianceservice.repository.AuditLogRepository;
import org.citycare.complianceservice.repository.AuditRepository;
import org.citycare.complianceservice.repository.ComplianceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements org.citycare.complianceservice.service.ComplianceService {

    private final ComplianceRecordRepository recordRepository;
    private final AuditRepository auditRepository;
    private final AuditLogRepository auditLogRepository;

    // OpenFeign clients for cross-service entity validation
    private final FacilityClient facilityClient;
    private final PatientClient patientClient;
    private final EmergencyClient emergencyClient;
    private final NotificationClient notificationClient;

    // ── Compliance Records ────────────────────────────────────────────────────

    @Transactional
    public ComplianceRecord createRecord(Long officerId, ComplianceRecordRequest req) {
        // Validate the target entity exists in the respective service via OpenFeign
        validateEntity(req.getType(), req.getEntityId());

        ComplianceRecord record = ComplianceRecord.builder()
                .entityId(req.getEntityId())
                .type(req.getType())
                .result(req.getResult())
                .date(req.getDate())
                .notes(req.getNotes())
                .officerId(officerId)
                .build();
        ComplianceRecord saved = recordRepository.save(record);
        logAction(officerId, "CREATE_COMPLIANCE_RECORD", "compliance_records/" + saved.getComplianceId());
        try {
            notificationClient.sendComplianceEvent(new NotificationClient.ComplianceEventPayload(
                saved.getComplianceId(), saved.getEntityId(), saved.getType().name(),
                saved.getResult() != null ? saved.getResult().name() : null,
                "RECORD_CREATED", officerId, saved.getNotes(), null
            ));
        } catch (Exception e) {
            log.warn("Could not send compliance record created notification", e);
        }
        return saved;
    }

    public List<ComplianceRecord> getAllRecords() {
        return recordRepository.findAll();
    }

    public ComplianceRecord getRecordById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", id));
    }

    public List<ComplianceRecord> getRecordsByEntity(Long entityId) {
        return recordRepository.findByEntityId(entityId);
    }

    public List<ComplianceRecord> getRecordsByType(ComplianceRecord.EntityType type) {
        return recordRepository.findByType(type);
    }

    // ── Audits ────────────────────────────────────────────────────────────────

    @Transactional
    public Audit createAudit(Long officerId, AuditRequest req) {
        Audit audit = Audit.builder()
                .officerId(officerId)
                .scope(req.getScope())
                .findings(req.getFindings())
                .date(req.getDate())
                .status(Audit.Status.SCHEDULED)
                .build();
        Audit saved = auditRepository.save(audit);
        logAction(officerId, "CREATE_AUDIT", "audits/" + saved.getAuditId());
        try {
            notificationClient.sendComplianceEvent(new NotificationClient.ComplianceEventPayload(
                null, null, null, null,
                "AUDIT_CREATED", officerId, req.getFindings(), null
            ));
        } catch (Exception e) {
            log.warn("Could not send audit created notification", e);
        }
        return saved;
    }

    public List<Audit> getAllAudits() {
        return auditRepository.findAll();
    }

    public Audit getAuditById(Long id) {
        return auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit", id));
    }

    @Transactional
    public Audit updateAuditStatus(Long id, Audit.Status status, String findings) {
        Audit audit = getAuditById(id);
        audit.setStatus(status);
        if (findings != null) audit.setFindings(findings);
        return auditRepository.save(audit);
    }

    // ── Audit Logs ────────────────────────────────────────────────────────────

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Validates the target entity (FACILITY / PATIENT / EMERGENCY) exists
     * in the responsible microservice via OpenFeign before creating a compliance record.
     */
    private void validateEntity(ComplianceRecord.EntityType type, Long entityId) {
        try {
            switch (type) {
                case FACILITY -> {
                    var facility = facilityClient.getFacilityById(entityId);
                    log.info("Compliance record targeting FACILITY id={} name={}", entityId, facility.getName());
                }
                case PATIENT -> {
                    var patient = patientClient.getPatientById(entityId);
                    log.info("Compliance record targeting PATIENT id={} status={}", entityId, patient.getStatus());
                }
                case EMERGENCY -> {
                    var emergency = emergencyClient.getEmergencyById(entityId);
                    log.info("Compliance record targeting EMERGENCY id={} status={}", entityId, emergency.getStatus());
                }
            }
        } catch (Exception e) {
            log.warn("Could not validate {} entityId={} via OpenFeign: {}", type, entityId, e.getMessage());
            // Non-fatal: allow creation even if remote service is down
        }
    }

    private void logAction(Long userId, String action, String resource) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
