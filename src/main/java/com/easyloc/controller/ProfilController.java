package com.easyloc.controller;

import com.easyloc.dao.ClientRepository;
import com.easyloc.dao.UtilisateurRepository;
import com.easyloc.model.Client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/profil")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProfilController {

    private final UtilisateurRepository uRepo;
    private final ClientRepository cRepo;

    public ProfilController(UtilisateurRepository uRepo, ClientRepository cRepo) {
        this.uRepo = uRepo;
        this.cRepo = cRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfil(@PathVariable Integer id) {
        return uRepo.findById(id).map(u -> {
            if (u instanceof Client c) {
                c.setPassword(null);
                return ResponseEntity.ok(c);
            }
            u.setPassword(null);
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfil(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return uRepo.findById(id).map(u -> {
            if (body.containsKey("nom")) u.setNom(body.get("nom").toString());
            if (body.containsKey("prenom")) u.setPrenom(body.get("prenom").toString());

            if (u instanceof Client c) {
                if (body.containsKey("numPermis")) c.setNumPermis(body.get("numPermis").toString());
                if (body.containsKey("dateNaissance")) c.setDateNaissance(LocalDate.parse(body.get("dateNaissance").toString()));
                if (body.containsKey("licenceDate")) c.setLicenceDate(LocalDate.parse(body.get("licenceDate").toString()));
                if (body.containsKey("licenceExpiry")) c.setLicenceExpiry(LocalDate.parse(body.get("licenceExpiry").toString()));
                cRepo.save(c);
                c.setPassword(null);
                return ResponseEntity.ok(Map.of("message","Profil mis à jour.","profil",c));
            }
            uRepo.save(u);
            u.setPassword(null);
            return ResponseEntity.ok(Map.of("message","Profil mis à jour.","profil",u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/mot-de-passe")
    public ResponseEntity<?> changerMdp(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return uRepo.findById(id).map(u -> {
            String ancien = body.getOrDefault("ancienMdp", "");
            String nouveau = body.getOrDefault("nouveauMdp", "");

            if (!ancien.equals(u.getPassword()))
                return ResponseEntity.badRequest().body(Map.of("erreur", "Ancien mot de passe incorrect."));
            if (nouveau.length() < 6)
                return ResponseEntity.badRequest().body(Map.of("erreur", "Le nouveau mot de passe doit faire au moins 6 caractères."));

            u.setPassword(nouveau);
            uRepo.save(u);
            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès."));
        }).orElse(ResponseEntity.notFound().build());
    }
}