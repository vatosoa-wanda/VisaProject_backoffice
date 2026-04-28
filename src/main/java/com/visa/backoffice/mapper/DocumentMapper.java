package com.visa.backoffice.mapper;

import com.visa.backoffice.dto.DocumentDTO;
import com.visa.backoffice.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    /**
     * Convertit une entité Document en DTO
     */
    public DocumentDTO toDTO(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentDTO.builder()
            .id(document.getId())
            .pieceId(document.getPiece().getId())
            .pieceNom(document.getPiece().getNom())
            .nomOriginal(document.getNomOriginal())
            .dateCreation(document.getDateCreation())
            .build();
    }

    /**
     * Convertit un DTO DocumentDTO en entité Document
     */
    public Document toEntity(DocumentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Document.builder()
            .id(dto.getId())
            .nomOriginal(dto.getNomOriginal())
            .dateCreation(dto.getDateCreation())
            .build();
    }
}

