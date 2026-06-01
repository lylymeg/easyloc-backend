package com.easyloc.controller;

import com.easyloc.dao.VehiculeRepository;
import com.easyloc.dao.MarqueRepository; // Import indispensable
import com.easyloc.model.Vehicule;
import com.easyloc.model.Marque; // Import indispensable
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:5173")
@RestController



@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeRepository vRepo;
    private final MarqueRepository mRepo; 

  
    public VehiculeController(VehiculeRepository vRepo, MarqueRepository mRepo) {
        this.vRepo = vRepo;
        this.mRepo = mRepo;
    }

    
    @GetMapping("/marques")
    public ResponseEntity<List<Marque>> getMarques() {
        return ResponseEntity.ok(mRepo.findAll());
    }

  
    @GetMapping
    public ResponseEntity<List<Vehicule>> getVehicules(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String carburant,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Float prixMax,
            @RequestParam(required = false) Integer marqueId) {
        
       
        String searchNom = (nom != null && !nom.trim().isEmpty()) ? nom : null;
        String searchCarb = (carburant != null && !carburant.trim().isEmpty()) ? carburant : null;
        String searchTrans = (transmission != null && !transmission.trim().isEmpty()) ? transmission : null;

        
        if (searchNom == null && searchCarb == null && searchTrans == null 
                && prixMax == null && marqueId == null) {
            return ResponseEntity.ok(vRepo.findAll());
        }
        
    
        return ResponseEntity.ok(
            vRepo.findDisponiblesAvecFiltres(searchNom, searchCarb, searchTrans, prixMax, marqueId, Vehicule.Statut.DISPONIBLE)
        );
    }

   
    @GetMapping("/disponibles")
    public ResponseEntity<List<Vehicule>> getOnlyDisponibles() {
        return ResponseEntity.ok(vRepo.findByStatut(Vehicule.Statut.DISPONIBLE));
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Vehicule> getById(@PathVariable Integer id) {
        return vRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
            "disponibles", vRepo.countByStatut(Vehicule.Statut.DISPONIBLE),
            "enPanne",     vRepo.countByStatut(Vehicule.Statut.EN_PANNE),
            "maintenance", vRepo.countByStatut(Vehicule.Statut.MAINTENANCE),
            "total",       vRepo.count()
        ));
    }

    @PostMapping
    public ResponseEntity<Vehicule> create(@RequestBody Vehicule v) {
        if (v.getStatut() == null) v.setStatut(Vehicule.Statut.DISPONIBLE);
        if (v.getKilometrage() == null) v.setKilometrage(0);
        return ResponseEntity.ok(vRepo.save(v));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicule> update(@PathVariable Integer id, @RequestBody Vehicule body) {
        return vRepo.findById(id).map(v -> {
            v.setNom(body.getNom());
            v.setImage(body.getImage());
            v.setNbSieges(body.getNbSieges());
            v.setCouleur(body.getCouleur());
            v.setPrixJournalier(body.getPrixJournalier());
            v.setTransmission(body.getTransmission());
            v.setCarburant(body.getCarburant());
            v.setPlaqueImmat(body.getPlaqueImmat());
            v.setStatut(body.getStatut());
            v.setDescription(body.getDescription());
            v.setKilometrage(body.getKilometrage());
            if (body.getMarque() != null) v.setMarque(body.getMarque());
            return ResponseEntity.ok(vRepo.save(v));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> changerStatut(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return vRepo.findById(id).map(v -> {
            try {
                Vehicule.Statut s = Vehicule.Statut.valueOf(body.get("statut").toUpperCase());
                v.setStatut(s);
                vRepo.save(v);
                return ResponseEntity.ok(Map.of("message", "Statut mis à jour : " + s));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Statut invalide."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        return vRepo.findById(id).map(v -> {
            vRepo.delete(v);
            return ResponseEntity.ok(Map.of("message", "Véhicule supprimé."));
        }).orElse(ResponseEntity.notFound().build());
    }
}