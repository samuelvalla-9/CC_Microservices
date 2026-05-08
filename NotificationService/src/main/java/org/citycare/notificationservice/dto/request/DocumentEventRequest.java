package org.citycare.notificationservice.dto.request;

import lombok.Data;

/**
 * Inbound event payload sent by CitizenService when a document is uploaded.
 */
@Data
public class DocumentEventRequest {
    private Long documentId;
    private Long citizenId;
    private String citizenName;
    private String eventType;  // DOCUMENT_UPLOADED | DOCUMENT_VERIFIED | DOCUMENT_REJECTED
}
