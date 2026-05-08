package org.citycare.citizenservice.serviceImplementation;

import org.citycare.citizenservice.dto.request.CitizenProfileRequest;
import org.citycare.citizenservice.dto.request.UserProfileUpdateRequest;
import org.citycare.citizenservice.dto.response.CitizenDocumentResponse;
import org.citycare.citizenservice.dto.response.CitizenResponse;
import org.citycare.citizenservice.entity.Citizen;
import org.citycare.citizenservice.entity.CitizenDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.citizenservice.exception.ResourceNotFoundException;
import org.citycare.citizenservice.feign.AuthClient;
import org.citycare.citizenservice.feign.NotificationClient;
import org.citycare.citizenservice.repository.CitizenDocumentRepository;
import org.citycare.citizenservice.repository.CitizenRepository;
import org.citycare.citizenservice.service.CitizenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenServiceImplementation implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final CitizenDocumentRepository documentRepository;
    private final AuthClient authClient;
    private final NotificationClient notificationClient;

    // ── Called by auth-service via OpenFeign on citizen registration ──────────

    /**
     * Auto-creates a minimal citizen profile when a new user registers.
     * Called internally by auth-service via OpenFeign — not by the user directly.
     * If a profile already exists for this userId (e.g. idempotency), it is returned as-is.
     */
    @Transactional
    public Citizen createCitizenFromRegistration(Long userId, String name, String contactInfo) {
        // Idempotent: if profile already exists, return it
        return citizenRepository.findById(userId).orElseGet(() -> {
            log.info("Auto-creating citizen profile for userId={}, name={}", userId, name);
            Citizen citizen = Citizen.builder()
                    .citizenId(userId)
                    .name(name)
                    .contactInfo(contactInfo)
                    .status(Citizen.Status.ACTIVE)
                    .build();
            return citizenRepository.save(citizen);
        });
    }

    // ── Standard CRUD ─────────────────────────────────────────────────────────

    @Transactional
    public Citizen createOrUpdateProfile(Long userId, CitizenProfileRequest req) {

        log.info("Updating profile for userId={}, request={}", userId, req);

        Citizen citizen = citizenRepository.findById(userId)
                .orElse(new Citizen());

        citizen.setCitizenId(userId);
        
        // Only update fields if they are provided (not null)
        if (req.getName() != null && !req.getName().isEmpty()) {
            citizen.setName(req.getName());
        }
        if (req.getDateOfBirth() != null) {
            citizen.setDateOfBirth(req.getDateOfBirth());
        }
        if (req.getGender() != null) {
            citizen.setGender(req.getGender());
        }
        if (req.getAddress() != null && !req.getAddress().isEmpty()) {
            citizen.setAddress(req.getAddress());
        }
        if (req.getContactInfo() != null && !req.getContactInfo().isEmpty()) {
            citizen.setContactInfo(req.getContactInfo());
        }
        
        citizen.setStatus(Citizen.Status.ACTIVE);

        // Save citizen first
        Citizen saved = citizenRepository.save(citizen);
        log.info("Citizen saved successfully: {}", saved.getCitizenId());

        // Try to update auth service, but don't fail if it errors
        try {
            if (req.getName() != null || req.getContactInfo() != null) {
                authClient.updateUserProfile(
                        userId,
                        new UserProfileUpdateRequest(
                                req.getName() != null ? req.getName() : saved.getName(),
                                req.getContactInfo() != null ? req.getContactInfo() : saved.getContactInfo()
                        )
                );
                log.info("Auth service profile updated successfully");
            }
        } catch (Exception e) {
            log.warn("Failed to update auth service profile, but citizen profile saved: {}", e.getMessage());
        }

        return saved;
    }

    public Citizen getProfile(Long userId) {
        return citizenRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found. Please complete your profile.", userId));
    }

    public Citizen getById(Long citizenId) {
        return citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", citizenId));
    }


    @Override
    public CitizenResponse getCitizenResponseById(Long citizenId) {

        Citizen citizen = citizenRepository.getById(citizenId);

        return CitizenResponse.builder()
                .citizenId(citizen.getCitizenId())
                .name(citizen.getName())
                .contactInfo(citizen.getContactInfo())
                .status("ACTIVE")
                .build();

    }
        public List<Citizen> getAll() {
        return citizenRepository.findAll();
    }

    @Transactional
    public CitizenDocument uploadDocument(Long citizenId, byte[] documentData, String contentType) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", citizenId));

        CitizenDocument doc = CitizenDocument.builder()
                .citizen(citizen)
                .documentData(documentData)
                .contentType(contentType)
                .uploadedDate(LocalDate.now())
                .verificationStatus(CitizenDocument.VerificationStatus.PENDING)
                .build();

        CitizenDocument saved = documentRepository.save(doc);

        // Notify admins via NotificationService event endpoint (it resolves admin users internally)
        try {
            notificationClient.documentEvent(Map.of(
                    "documentId", saved.getDocumentId(),
                    "citizenId", citizenId,
                    "citizenName", citizen.getName(),
                    "eventType", "DOCUMENT_UPLOADED"
            ));
            log.info("Sent document upload event for citizen #{}", citizenId);
        } catch (Exception e) {
            log.warn("Failed to send document upload event: {}", e.getMessage());
        }

        return saved;
    }

    public CitizenDocument getDocumentWithBlob(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    @Transactional
    public CitizenDocumentResponse verifyDocument(Long documentId,
                                                  CitizenDocument.VerificationStatus status) {

        CitizenDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        doc.setVerificationStatus(status);

        // Notify the citizen about their document verification result
        try {
            Long citizenId = doc.getCitizen().getCitizenId();
            String statusMessage = status == CitizenDocument.VerificationStatus.VERIFIED
                    ? "Your document has been verified. You can now report emergencies."
                    : "Your document has been rejected. Please upload a valid document to report emergencies.";

            notificationClient.createNotification(Map.of(
                    "userId", citizenId,
                    "entityId", doc.getDocumentId(),
                    "title", "Document " + status.name(),
                    "message", statusMessage,
                    "category", "EMERGENCY"
            ));
            log.info("Notified citizen #{} about document {} status: {}", citizenId, documentId, status);
        } catch (Exception e) {
            log.warn("Failed to notify citizen about document verification: {}", e.getMessage());
        }

        return CitizenDocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .verificationStatus(doc.getVerificationStatus().name())
                .uploadedDate(doc.getUploadedDate())
                .build();
    }

    public List<CitizenDocumentResponse> getDocuments(Long citizenId) {

        List<CitizenDocument> documents =
                documentRepository.findByCitizenCitizenId(citizenId);

        return documents.stream()
                .map(doc -> CitizenDocumentResponse.builder()
                        .documentId(doc.getDocumentId())
                        .verificationStatus(doc.getVerificationStatus().name())
                        .uploadedDate(doc.getUploadedDate())
                        .build())
                .toList();
    }

    @Override
    public boolean isCitizenDocumentVerified(Long citizenId) {
        return documentRepository.existsByCitizenCitizenIdAndVerificationStatus(
                citizenId, CitizenDocument.VerificationStatus.VERIFIED);
    }
}
