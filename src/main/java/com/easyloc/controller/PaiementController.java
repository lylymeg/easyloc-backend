package com.easyloc.controller;

import com.easyloc.dao.PaiementRepository;
import com.easyloc.dao.ReservationRepository;
import com.easyloc.model.Paiement;
import com.easyloc.model.Reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PaiementController {

    private final PaiementRepository pRepo;
    private final ReservationRepository rRepo;

    public PaiementController(PaiementRepository pRepo, ReservationRepository rRepo) {
        this.pRepo = pRepo;
        this.rRepo = rRepo;
    }

    @GetMapping
    public ResponseEntity<List<Paiement>> getAll() {
        return ResponseEntity.ok(pRepo.findAll());
    }

    @GetMapping("/reservation/{resId}")
    public ResponseEntity<?> getByReservation(@PathVariable Integer resId) {
        return pRepo.findByReservationIdReservation(resId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional 
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("reservationId") || body.get("reservationId") == null) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "ID de réservation manquant."));
            }

            Integer resId = Integer.valueOf(body.get("reservationId").toString());
            
            // 🛡️ Récupération ultra-sécurisée (évite les NullPointerException)
            Object methodeObj = body.get("methode");
            String methode = methodeObj != null ? methodeObj.toString() : "CARTE";
            
            Object numeroObj = body.get("numeroCarte");
            String numeroCarte = numeroObj != null ? numeroObj.toString() : "";
            
            Object dateExpObj = body.get("dateExpiration");
            String dateExp = dateExpObj != null ? dateExpObj.toString() : "";
            
            Object ribObj = body.get("rib");
            String rib = ribObj != null ? ribObj.toString() : "";

            return rRepo.findById(resId).map(res -> {
                
                if (pRepo.findByReservationIdReservation(resId).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erreur", "Un paiement existe déjà pour cette réservation."));
                }

                Paiement p = new Paiement();
                p.setReservation(res);
                
                Float montantDb = res.getMontantTotal(); 
                p.setMontant((montantDb == null || montantDb == 0f) ? 5000f : montantDb); 
                p.setMethode(methode);
                p.setDatePaiement(LocalDateTime.now());

                boolean carteExpiree = false;
                
                if ("CARTE".equals(methode) && dateExp.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
                    String[] parts = dateExp.split("/");
                    int moisExp = Integer.parseInt(parts[0]);
                    int anneeExp = 2000 + Integer.parseInt(parts[1]);
                    
                    java.time.YearMonth expiration = java.time.YearMonth.of(anneeExp, moisExp);
                    java.time.YearMonth maintenant = java.time.YearMonth.now();
                    
                    if (expiration.isBefore(maintenant)) {
                        carteExpiree = true;
                    }
                }

                boolean carteRefusee = "CARTE".equals(methode) && (numeroCarte.startsWith("0000") || carteExpiree);
                boolean virementRefuse = "VIREMENT".equals(methode) && (rib.startsWith("0000") || rib.trim().isEmpty());

                if (carteRefusee || virementRefuse) {
                    p.setStatutPaiement(Paiement.Statut.ECHOUE);
                    pRepo.save(p); 

                    res.setStatutReservation(Reservation.Statut.ANNULEE);
                    rRepo.save(res);

                    String raison = "Paiement refusé.";
                    if (carteExpiree) raison = "Carte expirée.";
                    if ("CARTE".equals(methode) && numeroCarte.startsWith("0000")) raison = "Fonds insuffisants.";
                    if (virementRefuse) raison = "RIB invalide ou compte introuvable.";

                    return ResponseEntity.badRequest()
                            .body(Map.of("erreur", raison + " La réservation a été annulée et le véhicule libéré."));
                }

                p.setStatutPaiement(Paiement.Statut.PAYE);
                pRepo.save(p);
                
                res.setStatutReservation(Reservation.Statut.TERMINEE); 
                rRepo.save(res);
                
                

                return ResponseEntity.ok(Map.of("message", "Paiement validé avec succès !"));

            }).orElse(ResponseEntity.status(404).body(Map.of("erreur", "Réservation introuvable.")));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("erreur", "Crash Serveur: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> changerStatut(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return pRepo.findById(id).map(p -> {
            try {
                String nouveauStatut = body.get("statut").toUpperCase();
                p.setStatutPaiement(Paiement.Statut.valueOf(nouveauStatut));
                return ResponseEntity.ok(pRepo.save(p));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "Statut invalide."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}