package com.easyloc.controller;

import com.easyloc.dao.*;
import com.easyloc.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReservationController {

    private final ReservationRepository rRepo;
    private final VehiculeRepository    vRepo;
    private final ClientRepository      cRepo;
    private final ContratRepository     ctRepo;
    private final NotificationRepository notifRepo;
    private final PaiementRepository pRepo;

    public ReservationController(ReservationRepository rRepo,
                                  VehiculeRepository vRepo,
                                  ClientRepository cRepo,
                                  ContratRepository ctRepo,
                                  NotificationRepository notifRepo,PaiementRepository pRepo) {
        this.rRepo     = rRepo;
        this.vRepo     = vRepo;
        this.cRepo     = cRepo;
        this.ctRepo    = ctRepo;
        this.notifRepo = notifRepo;
        this.pRepo = pRepo;

        
    }

    // ══════════════════════════════════════════════════════════
    //  LISTE & STATS
    // ══════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(rRepo.findAll());
    }

    @GetMapping("/mes")
    public ResponseEntity<?> getMes(@RequestParam Integer clientId) {
        return ResponseEntity.ok(rRepo.findByClientIdUserOrderByDateReservationDesc(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return rRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String,Object>> getStats() {
        List<Reservation> toutes = rRepo.findAll();
        float revenuTotal = toutes.stream()
            .filter(r -> r.getStatutReservation() == Reservation.Statut.TERMINEE)
            .map(r -> (r.getMontantTotal() != null ? r.getMontantTotal() : 0f)
                    + (r.getMontantPenalite() != null ? r.getMontantPenalite() : 0f))
            .reduce(0f, Float::sum);

        Map<String, Float> parMois = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime mois = LocalDateTime.now().minusMonths(i);
            String label = mois.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.FRENCH) + " " + mois.getYear();
            final int fi = i;
            float rev = toutes.stream()
                .filter(r -> r.getStatutReservation() == Reservation.Statut.TERMINEE
                          && r.getDateReservation() != null
                          && r.getDateReservation().getMonth() == LocalDateTime.now().minusMonths(fi).getMonth()
                          && r.getDateReservation().getYear()  == LocalDateTime.now().minusMonths(fi).getYear())
                .map(r -> (r.getMontantTotal() != null ? r.getMontantTotal() : 0f))
                .reduce(0f, Float::sum);
            parMois.put(label, rev);
        }

        return ResponseEntity.ok(Map.of(
            "confirmees", rRepo.countByStatutReservation(Reservation.Statut.CONFIRMEE),
            "enAttente", rRepo.countByStatutReservation(Reservation.Statut.EN_ATTENTE),
            "terminees", rRepo.countByStatutReservation(Reservation.Statut.TERMINEE),
            "total", rRepo.count(),
            "revenuTotal", revenuTotal,
            "revenuParMois", parMois
        ));
    }

    // ══════════════════════════════════════════════════════════
    //  CRÉATION
    // ══════════════════════════════════════════════════════════


    @PostMapping
    public ResponseEntity<?> creer(@RequestBody Map<String, Object> body) {
        try {
            Object clientIdObj = body.get("clientId");
            if (clientIdObj == null) return err("ID Client manquant dans la requête.");

            Integer clientId = Integer.valueOf(clientIdObj.toString());
            Integer vehiculeId = Integer.valueOf(body.get("vehiculeId").toString());


            LocalDateTime debut = LocalDateTime.parse(body.get("dateDebut").toString());
            LocalDateTime fin   = LocalDateTime.parse(body.get("dateFin").toString());

            if (!fin.isAfter(debut)) return err("La date de fin doit être postérieure.");
            
            Client client = cRepo.findById(clientId).orElseThrow(()->new RuntimeException("Client introuvable."));
            Vehicule vehicule = vRepo.findById(vehiculeId).orElseThrow(()->new RuntimeException("Véhicule introuvable."));
            
            // --- 1. VÉRIFICATION DE L'ÂGE (21 ANS MINIMUM) ---
            if (client.getDateNaissance() == null) {
                return err("Veuillez renseigner votre date de naissance dans votre profil avant de réserver.");
            }
            long age = java.time.temporal.ChronoUnit.YEARS.between(
                client.getDateNaissance(), 
                java.time.LocalDate.now()
            );
            if (age < 21) {
                return err("Vous devez avoir au moins 21 ans pour effectuer une réservation.");
            }

           // --- VÉRIFICATIONS STRICTES ---
            if (!client.hasAgeRequis(21)) {
                return err("Vous devez avoir au moins 21 ans pour effectuer une réservation.");
            }
            if (client.getNumPermis() == null || client.getNumPermis().trim().isEmpty()) {
                return err("Les informations de votre permis de conduire (numéro) sont obligatoires.");
            }
            if (!client.hasPermisValide()) {
                return err("Votre permis de conduire est expiré ou la date d'expiration n'est pas renseignée.");
            }

            if (vehicule.getStatut() == Vehicule.Statut.EN_PANNE) return err("Véhicule en panne.");
            if (!vRepo.isDisponiblePeriode(vehiculeId, debut, fin)) return err("Véhicule déjà réservé.");

            Reservation r = new Reservation();
            r.setClient(client);
            r.setVehicule(vehicule);
            r.setDateDebut(debut);
            r.setDateFin(fin);
            // --- ICI TU AJOUTES LE CALCUL SÉCURISÉ ---
    
 
            r.setStatutReservation(Reservation.Statut.EN_ATTENTE);
            r.setMontantPenalite(0f);

            float montantBase = r.calculerMontantBase();
            float remise = client.getTauxRemise();
            r.setMontantTotal(client.appliquerRemise(montantBase));

            Reservation saved = rRepo.save(r);

            Contrat contrat = new Contrat();
            contrat.setReservation(saved);
            contrat.setContenu(genererContenu(saved, remise));
            ctRepo.save(contrat);

            envoyerNotification(client, "Réservation créée", "Votre réservation #" + saved.getIdReservation() + " est en attente.");

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    
    // ══════════════════════════════════════════════════════════
    //  ACTIONS ADMIN (MODIFIÉ POUR LA SÉCURITÉ)
    // ══════════════════════════════════════════════════════════

   @PutMapping("/{id}/confirmer")
public ResponseEntity<?> confirmer(@PathVariable Integer id) {
    return rRepo.findById(id).map(r -> {
        Client c = r.getClient();

   
        Integer nbActuel = c.getNbLocationsTerminees();
        if (nbActuel == null) {
            nbActuel = 0;
        }
       
        c.setNbLocationsTerminees(nbActuel + 1);
        cRepo.save(c); 
        // ────────────────────────────────────────────────────────────

        r.setStatutReservation(Reservation.Statut.CONFIRMEE);
        rRepo.save(r);
       
        List<Reservation> conflits = rRepo.findConflits(
            r.getVehicule().getIdVehicule(),
            r.getDateDebut(),
            r.getDateFin(),
            r.getIdReservation()
        );

        for (Reservation conflit : conflits) {
            conflit.setStatutReservation(Reservation.Statut.ANNULEE);
            rRepo.save(conflit);
            envoyerNotification(
                conflit.getClient(), 
                "Demande annulée", 
                "Votre demande pour le véhicule " + r.getVehicule().getNom() + " a été annulée car ce dernier vient d'être loué à un autre client pour ces dates."
            );
        }
        // ────────────────────────────────────────────────────────────
        envoyerNotification(c, "Confirmée ✅", "Votre réservation #" + id + " est confirmée !");
        return ResponseEntity.ok(Map.of("message", "Confirmée"));
    }).orElse(ResponseEntity.notFound().build());
}

@PostMapping("/{id}/payer")
public ResponseEntity<?> payer(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
    return rRepo.findById(id).map(r -> {
        try {
            
            Paiement p = new Paiement();
            p.setReservation(r);
            p.setMontant(r.getMontantTotal());
            p.setMethode(body.get("methode").toString());
            p.setStatutPaiement(Paiement.Statut.PAYE);
            pRepo.save(p); 

          
            r.setStatutReservation(Reservation.Statut.TERMINEE);
            rRepo.save(r); 

            envoyerNotification(r.getClient(), "Paiement Reçu 💳", 
                "Votre paiement a été validé. Merci de votre confiance !");

            return ResponseEntity.ok(Map.of("message", "Paiement enregistré et statut mis à jour"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }).orElse(ResponseEntity.notFound().build());
}

    @PutMapping("/{id}/terminer")
    public ResponseEntity<?> terminer(@PathVariable Integer id) {
        return rRepo.findById(id).map(r -> {
            float penalite = r.calculerPenalite();
            r.setMontantPenalite(penalite);
            r.setStatutReservation(Reservation.Statut.TERMINEE);
            rRepo.save(r);

            Client c = r.getClient();
            c.setNbLocationsTerminees((c.getNbLocationsTerminees() != null ? c.getNbLocationsTerminees() : 0) + 1);
            cRepo.save(c);

            envoyerNotification(c, "Terminée", "Votre location #" + id + " est finie.");
            return ResponseEntity.ok(Map.of("message", "Terminée"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/sync-retards")
    public ResponseEntity<?> syncRetards() {
        List<Reservation> enCours = rRepo.findAll().stream()
            .filter(r -> r.getStatutReservation() == Reservation.Statut.CONFIRMEE || r.getStatutReservation() == Reservation.Statut.RETARD)
            .toList();

        int count = 0;
        for (Reservation r : enCours) {
            if (r.getDateFin() != null && r.getDateFin().isBefore(LocalDateTime.now())) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(r.getDateFin(), LocalDateTime.now());
                r.setStatutReservation(Reservation.Statut.RETARD);
                r.setJoursRetard((int) jours);
                r.setMontantPenalite(r.calculerPenalite());
                rRepo.save(r);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of("message", count + " retards synchronisés."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> annuler(@PathVariable Integer id) {
        return rRepo.findById(id).map(r -> {
            r.setStatutReservation(Reservation.Statut.ANNULEE);
            rRepo.save(r);
            return ResponseEntity.ok(Map.of("message", "Annulée"));
        }).orElse(ResponseEntity.notFound().build());
    }

    

    private ResponseEntity<Map<String, String>> err(String msg) {
        return ResponseEntity.badRequest().body(Map.of("erreur", msg));
    }

    private void envoyerNotification(Utilisateur user, String titre, String message) {
        try {
            Notification n = new Notification();
            n.setUser(user); n.setTitre(titre); n.setMessage(message);
            notifRepo.save(n);
        } catch (Exception ignored) {}
    }
private String genererContenu(Reservation r, float remise) {
    return String.format(
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
      r.getClient().getPrenom(), 
        r.getClient().getNom(),    
        r.getVehicule().getMarque().getNom(), 
        r.getVehicule().getNom(),             
        r.getVehicule().getPlaqueImmat(),     
        r.getDateDebut().toString(),          
        r.getDateFin().toString(),            
        r.getMontantTotal(),                  
        java.time.LocalDate.now().toString()
    );
}
}