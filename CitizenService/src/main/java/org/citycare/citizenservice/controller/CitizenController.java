package org.citycare.citizenservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.citycare.citizenservice.dto.request.CitizenCreateRequest;
import org.citycare.citizenservice.dto.request.CitizenProfileRequest;
import org.citycare.citizenservice.dto.response.ApiResponse;
import org.citycare.citizenservice.dto.response.CitizenDocumentResponse;
import org.citycare.citizenservice.dto.response.CitizenResponse;
import org.citycare.citizenservice.entity.Citizen;
import org.citycare.citizenservice.entity.CitizenDocument;
import org.citycare.citizenservice.service.CitizenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/citizens")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenService citizenService;

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Citizen>> updateProfile(
            @Valid @RequestBody CitizenProfileRequest request,
            @RequestHeader("X-Auth-UserId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Profile saved", citizenService.createOrUpdateProfile(userId, request)));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Citizen>> getMyProfile(
            @RequestHeader("X-Auth-UserId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Citizen profile", citizenService.getProfile(userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Citizen>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All citizens", citizenService.getAll()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Citizen>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Citizen", citizenService.getById(id)));
    }


    @GetMapping("/internal/{id}")
    public CitizenResponse getByIdInternal(@PathVariable Long id) {
        return citizenService.getCitizenResponseById(id);
    }

    @PostMapping("/internal/create")
    public CitizenResponse createCitizenFromRegistration(@RequestBody CitizenCreateRequest request) {
        Citizen citizen = citizenService.createCitizenFromRegistration(
            request.getUserId(), 
            request.getName(), 
            request.getContactInfo()
        );
        return CitizenResponse.builder()
            .citizenId(citizen.getCitizenId())
            .name(citizen.getName())
            .contactInfo(citizen.getContactInfo())
            .status(citizen.getStatus().name())
            .build();
    }


    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CitizenDocument>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        CitizenDocument doc = citizenService.uploadDocument(id, file.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document uploaded", doc));
    }


    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<CitizenDocumentResponse>>> getDocuments(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Documents",
                        citizenService.getDocuments(id)
                )
        );
    }

    @GetMapping("/{id}/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        
        CitizenDocument doc = citizenService.getDocumentWithBlob(docId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(doc.getDocumentData());
    }



    @PatchMapping("/documents/{docId}/verify")
    public ResponseEntity<ApiResponse<CitizenDocumentResponse>> verifyDocument(
            @PathVariable Long docId,
            @RequestParam CitizenDocument.VerificationStatus status) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Document status updated to " + status,
                        citizenService.verifyDocument(docId, status)
                )
        );
    }


}
