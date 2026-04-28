package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanPageDTO {

    private Long demandeId;
    private String reference;
    private String statut;
    private DemandeurDTO demandeur;
    private PasseportDTO passeport;
    private VisaDTO visa;
    private List<DocumentDTO> documents;
    private Integer nombreDocumentsFournis;
    private Integer nombreDocumentsAttendus;
    private Map<Long, String> piecesParType;
    private Boolean dossierVerrouille;
}
