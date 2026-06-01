package com.easyloc.controller;

import com.easyloc.dao.ContratRepository;
import com.easyloc.model.Contrat;
import com.easyloc.model.Reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/contrats")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ContratController {

    private final ContratRepository repo;

    public ContratController(ContratRepository repo) {
        this.repo = repo;
    }

    /** Récupérer le contrat d'une réservation */
    @GetMapping("/reservation/{resId}")
    public ResponseEntity<?> getByReservation(@PathVariable Integer resId) {
        return repo.findByReservationIdReservation(resId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

  
    @PutMapping("/{id}/signer")
    public ResponseEntity<?> signer(@PathVariable Integer id,
                                     @RequestBody Map<String, String> body) {
        return repo.findById(id).map(c -> {
            String signatureImage = body.get("signatureImage");

            if (signatureImage == null || signatureImage.isBlank())
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "Signature vide."));

            if (!signatureImage.startsWith("data:image/"))
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "Format de signature invalide. Attendu : data:image/png;base64,..."));

            c.setSignatureImage(signatureImage);
            c.setDateSignature(LocalDateTime.now());
            repo.save(c);

            return ResponseEntity.ok(Map.of(
                "message",       "Contrat signé avec succès.",
                "idContrat",     c.getIdContrat(),
                "dateSignature", c.getDateSignature().toString(),
                "signe",         true
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Vérifier si un contrat est signé */
    @GetMapping("/{id}/statut-signature")
    public ResponseEntity<?> statutSignature(@PathVariable Integer id) {
        return repo.findById(id).map(c -> ResponseEntity.ok(Map.of(
            "signe",         c.isSigne(),
            "dateSignature", c.getDateSignature() != null ? c.getDateSignature().toString() : null
        ))).orElse(ResponseEntity.notFound().build());
    }
    // Exemple de méthode de génération de contrat dans Spring Boot
public Contrat genererContratPourReservation(Reservation reservation) {
    Contrat contrat = new Contrat();
    contrat.setReservation(reservation);
    contrat.setSignatureImage(null); 
    
   
   
       String contenuHtml = String.format(
    "<div style='font-family: \"Times New Roman\", serif; line-height: 1.6; color: #333;'>" +
    
    "<h2 style='text-align: center; color: #0a192f; border-bottom: 2px solid #00b4d8; padding-bottom: 10px; margin-bottom: 20px;'>" +
    "CONTRAT DE LOCATION DE VÉHICULE" +
    "</h2>" +
    
    "<h3 style='color: #0a192f; font-size: 1.2rem;'>ARTICLE 1 : LES PARTIES</h3>" +
    "<p>Le présent contrat est conclu entre la société <strong>EasyLoc</strong> (ci-après dénommée \"Le Loueur\") et <strong>%s %s</strong> (ci-après dénommé \"Le Locataire\").</p>" +
    
    "<h3 style='color: #0a192f; font-size: 1.2rem; margin-top: 20px;'>ARTICLE 2 : LE VÉHICULE</h3>" +
    "<p>Le Loueur met à disposition du Locataire le véhicule désigné ci-dessous :</p>" +
    "<ul>" +
    "  <li><strong>Véhicule :</strong> %s (%s)</li>" +
    "  <li><strong>Immatriculation :</strong> %s</li>" +
    "</ul>" +
    
    "<h3 style='color: #0a192f; font-size: 1.2rem; margin-top: 20px;'>ARTICLE 3 : MODALITÉS DE LA LOCATION</h3>" +
    "<ul>" +
    "  <li><strong>Date et heure de début :</strong> %s</li>" +
    "  <li><strong>Date et heure de fin :</strong> %s</li>" +
    "  <li><strong>Montant total convenu :</strong> <strong>%s DA</strong></li>" +
    "</ul>" +
    
    "<h3 style='color: #0a192f; font-size: 1.2rem; margin-top: 20px;'>ARTICLE 4 : CONDITIONS GÉNÉRALES</h3>" +
    "<ul style='margin-bottom: 30px;'>" +
    "  <li style='margin-bottom: 10px;'><strong>État du véhicule :</strong> Le Locataire reconnaît avoir pris réception du véhicule en parfait état de marche et de propreté. Il s'engage à le restituer dans le même état.</li>" +
    "  <li style='margin-bottom: 10px;'><strong>Carburant :</strong> Le véhicule doit être restitué avec le même niveau de carburant qu'au départ. Dans le cas contraire, le carburant manquant sera facturé avec une majoration.</li>" +
    "  <li style='margin-bottom: 10px;'><strong>Responsabilité :</strong> Le Locataire est entièrement responsable des amendes, contraventions et délits liés au code de la route commis durant la période de location.</li>" +
    "  <li style='margin-bottom: 10px;'><strong>Pénalités de retard :</strong> Tout dépassement de la date de restitution sans l'accord préalable du Loueur entraînera une facturation de pénalités journalières.</li>" +
    "</ul>" +
    
    "<p style='text-align: right; font-style: italic; font-size: 1.1rem;'>" +
    "Fait à Béjaïa, le %s" +
    "</p>" +
    
    "</div>",
    
 
        reservation.getClient().getNom(),
        reservation.getClient().getPrenom(),
        reservation.getVehicule().getNom(),
        reservation.getVehicule().getMarque().getNom(),
        reservation.getVehicule().getPlaqueImmat(),
        reservation.getDateDebut().toString(),
        reservation.getDateFin().toString(),
        reservation.getMontantTotal(),
        java.time.LocalDate.now().toString()
    );
    
    contrat.setContenu(contenuHtml);
    return repo.save(contrat);
}
}
