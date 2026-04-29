package com.visa.backoffice.service;

import com.visa.backoffice.exception.ResourceNotFoundException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Document;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.VisaTransformable;
import com.visa.backoffice.repository.DemandeRepository;
import com.visa.backoffice.repository.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PdfService {

    private final DemandeRepository demandeRepository;
    private final DocumentRepository documentRepository;

    public PdfService(DemandeRepository demandeRepository, DocumentRepository documentRepository) {
        this.demandeRepository = demandeRepository;
        this.documentRepository = documentRepository;
    }

    public byte[] genererAttestation(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + demandeId));

        String reference = "DEM-" + String.format("%06d", demandeId);
        String statut = demande.getStatutDemande() != null ? demande.getStatutDemande().getLibelle() : "INCONNU";

        List<Document> documents = documentRepository.findByDemandeId(demandeId);
        documents.sort(Comparator.comparing(d -> d.getPiece() != null ? d.getPiece().getNom() : ""));

        List<String> lines = new ArrayList<>();
        lines.add("ATTESTATION DE DOSSIER");
        lines.add("");
        lines.add("Référence demande : " + reference);
        lines.add("Statut : " + statut);
        lines.add("");

        if (demande.getDemandeur() != null) {
            lines.add("ÉTAT CIVIL");
            lines.add("Nom : " + nullSafe(demande.getDemandeur().getNom()));
            lines.add("Prénom : " + nullSafe(demande.getDemandeur().getPrenom()));
            lines.add("Date de naissance : " + (demande.getDemandeur().getDateNaissance() != null ? demande.getDemandeur().getDateNaissance().toString() : ""));
            lines.add("");
        }

        Passeport passeport = null;
        VisaTransformable visaTransformable = demande.getVisaTransformable();
        if (visaTransformable != null) {
            passeport = visaTransformable.getPasseport();
        }

        if (passeport != null) {
            lines.add("PASSEPORT");
            lines.add("Numéro : " + nullSafe(passeport.getNumero()));
            lines.add("Date de délivrance : " + (passeport.getDateDelivrance() != null ? passeport.getDateDelivrance().toString() : ""));
            lines.add("Date d'expiration : " + (passeport.getDateExpiration() != null ? passeport.getDateExpiration().toString() : ""));
            lines.add("");
        }

        if (visaTransformable != null) {
            lines.add("VISA TRANSFORMABLE");
            lines.add("Type : " + (demande.getTypeVisa() != null ? nullSafe(demande.getTypeVisa().getLibelle()) : ""));
            lines.add("Référence : " + nullSafe(visaTransformable.getReferenceVisa()));
            lines.add("Date d'entrée : " + (visaTransformable.getDateEntree() != null ? visaTransformable.getDateEntree().toString() : ""));
            lines.add("Date d'expiration : " + (visaTransformable.getDateExpiration() != null ? visaTransformable.getDateExpiration().toString() : ""));
            lines.add("");
        }

        lines.add("PIÈCES JUSTIFICATIVES FOURNIES");
        if (documents.isEmpty()) {
            lines.add("- Aucune pièce fournie");
        } else {
            for (Document document : documents) {
                String pieceNom = (document.getPiece() != null) ? nullSafe(document.getPiece().getNom()) : "(pièce inconnue)";
                String fichier = nullSafe(document.getNomOriginal());
                lines.add("- " + pieceNom + (fichier.isBlank() ? "" : " (" + fichier + ")"));
            }
        }

        lines.add("");
        lines.add("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        try {
            return renderPdf(lines);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private byte[] renderPdf(List<String> lines) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float y = yStart;
            float leading = 14;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            for (String line : lines) {
                if (y <= margin) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    y = yStart;
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(sanitize(line));
                contentStream.endText();

                y -= leading;
            }

            contentStream.close();
            document.save(out);
            return out.toByteArray();
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        // PDFBox Type1 fonts support WinAnsi; remove control chars to avoid errors
        return text.replace("\t", " ")
            .replace("\r", " ")
            .replace("\n", " ");
    }
}
