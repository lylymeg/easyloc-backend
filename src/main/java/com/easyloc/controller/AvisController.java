package com.easyloc.controller;

import com.easyloc.dao.AvisEvaluationRepository;
import com.easyloc.dao.ClientRepository;
import com.easyloc.dao.VehiculeRepository;
import com.easyloc.model.AvisEvaluation;
import com.easyloc.model.Client;
import com.easyloc.model.Vehicule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avis")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class AvisController {

    private final AvisEvaluationRepository aRepo;
    private final ClientRepository cRepo;
    private final VehiculeRepository vRepo;


    public AvisController(AvisEvaluationRepository aRepo,
                          ClientRepository cRepo,
                          VehiculeRepository vRepo) {
        this.aRepo = aRepo;
        this.cRepo = cRepo;
        this.vRepo = vRepo;
    }

    
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<?> getByVehicule(@PathVariable Integer vehiculeId) {
        List<AvisEvaluation> avis = aRepo.findByVehiculeIdVehiculeOrderByDateCreationDesc(vehiculeId);
        Double moyenne = aRepo.moyenneNoteParVehicule(vehiculeId);
        
        return ResponseEntity.ok(Map.of(
            "avis",    avis,
            "moyenne", moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0,
            "count",   avis.size()
        ));
    }

   
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AvisEvaluation>> getByClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(aRepo.findByClientIdUser(clientId));
    }

   
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Integer clientId   = Integer.valueOf(body.get("clientId").toString());
            Integer vehiculeId = Integer.valueOf(body.get("vehiculeId").toString());
            int note = Integer.parseInt(body.get("note").toString());

           
            if (note < 1 || note > 5) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "La note doit être comprise entre 1 et 5."));
            }

            Client client = cRepo.findById(clientId).orElse(null);
            Vehicule vehicule = vRepo.findById(vehiculeId).orElse(null);

            if (client == null || vehicule == null) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Client ou Véhicule introuvable."));
            }

            AvisEvaluation avis = new AvisEvaluation();
            avis.setClient(client);
            avis.setVehicule(vehicule);
            avis.setNote(note);
            avis.setContenu(body.getOrDefault("contenu", "").toString());
            
            return ResponseEntity.ok(aRepo.save(avis));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Données invalides dans le formulaire."));
        }
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        return aRepo.findById(id).map(a -> {
            aRepo.delete(a);
            return ResponseEntity.ok(Map.of("message", "Avis supprimé avec succès."));
        }).orElse(ResponseEntity.notFound().build());
    }
}