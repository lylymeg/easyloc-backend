package com.easyloc.controller;

import com.easyloc.dao.ClientRepository;
import com.easyloc.dao.NotificationRepository;
import com.easyloc.dao.ReservationRepository;
import com.easyloc.model.Client;
import com.easyloc.model.Notification;
import com.easyloc.model.Utilisateur;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class ClientController {

    private final ClientRepository cRepo;
    private final ReservationRepository rRepo;
    private final NotificationRepository notifRepo;

    public ClientController(ClientRepository cRepo, ReservationRepository rRepo, NotificationRepository notifRepo) {
        this.cRepo = cRepo;
        this.rRepo = rRepo;
        this.notifRepo = notifRepo;
    }

    
    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        List<Client> clients = cRepo.findAll();
        clients.forEach(c -> c.setPassword(null)); 
        return ResponseEntity.ok(clients);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return cRepo.findById(id).map(c -> {
            c.setPassword(null);
            return ResponseEntity.ok(c);
        }).orElse(ResponseEntity.notFound().build());
    }

   
    @PutMapping("/{id}/sync-fidelite")
    @Transactional
    public ResponseEntity<?> synchroniserFidelite(@PathVariable Integer id) {
        return cRepo.findById(id).map(client -> {
          
            long nbTerminees = rRepo.findByClientIdUserOrderByDateReservationDesc(id)
                    .stream()
                    .filter(res -> "TERMINEE".equals(res.getStatutReservation().toString())) 
                    .count();

            int ancienCompteur = client.getNbLocationsTerminees() != null ? client.getNbLocationsTerminees() : 0;
            
           
            client.setNbLocationsTerminees((int) nbTerminees);
            cRepo.save(client);

           
            if (nbTerminees > 3 && ancienCompteur <= 3) {
                envoyerNotification(client, 
                    "Nouveau Statut : Client Fidèle ! ✨", 
                    "Félicitations ! Votre assiduité est récompensée. Vous bénéficiez maintenant de -10% sur toutes vos réservations.");
                
                return ResponseEntity.ok(Map.of(
                    "status", "PROMOTE",
                    "message", "Fidélité activée automatiquement",
                    "nbLocations", nbTerminees
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "SYNC",
                "message", "Compteur synchronisé avec la base de données",
                "nbLocations", nbTerminees
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

@GetMapping("/me")
public ResponseEntity<?> getMyProfile() {
   
    return ResponseEntity.ok("Utilisateur connecté"); 
}

@PutMapping("/{id}")
public ResponseEntity<?> updateClient(@PathVariable Integer id, @RequestBody Client details) {
    return cRepo.findById(id).map(client -> {
        client.setNom(details.getNom());
        client.setPrenom(details.getPrenom());
        client.setEmail(details.getEmail());
        client.setTelephone(details.getTelephone());
        client.setPhotoUrl(details.getPhotoUrl()); 
        client.setNumPermis(details.getNumPermis());
        client.setDateNaissance(details.getDateNaissance());
        client.setLicenceDate(details.getLicenceDate()); 
        client.setLicenceExpiry(details.getLicenceExpiry()); 
        
       
        client.setPermisDoc(details.getPermisDoc());
        
        return ResponseEntity.ok(cRepo.save(client));
    }).orElse(ResponseEntity.notFound().build());
}

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        return cRepo.findById(id).map(c -> {
            cRepo.delete(c);
            return ResponseEntity.ok(Map.of("message", "Client supprimé"));
        }).orElse(ResponseEntity.notFound().build());
    }

   
    private void envoyerNotification(Utilisateur user, String titre, String message) {
        if (user == null) return;
        try {
            Notification n = new Notification();
            n.setUser(user);
            n.setTitre(titre);
            n.setMessage(message);
            n.setLu(false);
            notifRepo.save(n);
        } catch (Exception e) {
            System.err.println("Erreur notification : " + e.getMessage());
        }
    }
}