package com.mvogt.quincaillerie.client.service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.mvogt.quincaillerie.client.model.LigneVenteResponse;
import com.mvogt.quincaillerie.client.model.VenteResponse;

/** Genere un recu (ticket de caisse) PDF apres une vente et l'envoie a l'imprimante par defaut. */
public class ReceiptService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    // Largeur ~80mm (ticket de caisse standard), hauteur extensible.
    private static final Rectangle TICKET_SIZE = new Rectangle(227f, 700f);

    public File generer(VenteResponse vente) throws IOException {
        Path dossier = Path.of("receipts");
        Files.createDirectories(dossier);
        File fichier = dossier.resolve("vente_" + vente.id() + ".pdf").toFile();

        Document document = new Document(TICKET_SIZE, 10, 10, 10, 10);
        try (FileOutputStream out = new FileOutputStream(fichier)) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.COURIER, 8);
            Font gras = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);

            ajouterCentre(document, "Quincaillerie Mvogt", titre);
            ajouterCentre(document, "Yaounde", normal);
            ajouter(document, new Paragraph(" "));
            ajouter(document, new Paragraph("Vente #" + vente.id(), normal));
            ajouter(document, new Paragraph("Date : " + DATE_FORMAT.format(vente.dateVente()), normal));
            ajouter(document, new Paragraph("Vendeur : " + vente.vendeurLogin(), normal));
            ajouter(document, new Paragraph("------------------------------", normal));

            for (LigneVenteResponse ligne : vente.lignes()) {
                ajouter(document, new Paragraph(ligne.produitNom(), normal));
                String detail = String.format("  %d x %s = %s", ligne.quantite(), ligne.prixVenteUnitaire(), ligne.sousTotal());
                ajouter(document, new Paragraph(detail, normal));
            }

            ajouter(document, new Paragraph("------------------------------", normal));
            ajouter(document, new Paragraph("TOTAL : " + vente.montantTotal() + " FCFA", gras));
            ajouter(document, new Paragraph(" "));
            ajouterCentre(document, "Merci de votre achat !", normal);

            document.close();
        } catch (DocumentException e) {
            throw new IOException(e);
        }
        return fichier;
    }

    public void imprimer(File fichier) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.PRINT)) {
            desktop.print(fichier);
        } else if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(fichier);
        }
    }

    public void genererEtImprimer(VenteResponse vente, String vendeurLogin) throws IOException {
        File fichier = generer(vente);
        imprimer(fichier);
    }

    private void ajouterCentre(Document document, String texte, Font font) throws DocumentException {
        Paragraph paragraphe = new Paragraph(texte, font);
        paragraphe.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraphe);
    }

    private void ajouter(Document document, Element element) throws DocumentException {
        document.add(element);
    }
}
