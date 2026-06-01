package com.easyloc.controller;

import com.easyloc.dao.ClientRepository;
import com.easyloc.dao.UtilisateurRepository;
import com.easyloc.model.Client;
import com.easyloc.model.Utilisateur;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final UtilisateurRepository uRepo;
    private final ClientRepository cRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UtilisateurRepository uRepo, ClientRepository cRepo) {
        this.uRepo = uRepo;
        this.cRepo = cRepo;
    }

    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        String pwd   = body.getOrDefault("password", "");

        if (email.isEmpty() || pwd.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Email et mot de passe requis."));
        }

       return uRepo.findByEmail(email).map(u -> {
   
    if (!u.getPassword().startsWith("$2a$")) {
        
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        uRepo.save(u);
    }

    
    if (!passwordEncoder.matches(pwd, u.getPassword())) {
        return ResponseEntity.status(401).body(Map.of("erreur", "Mot de passe incorrect."));
    }

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("id",      u.getIdUser());
            res.put("nom",     u.getNom());
            res.put("prenom",  u.getPrenom());
            res.put("email",   u.getEmail());
            res.put("role",    u.getRole().name());

            if (u instanceof Client client) {
                res.put("nbLocationsTerminees", client.getNbLocationsTerminees());
                res.put("clientFidele",         client.isClientFidele());
                res.put("tauxRemise",           client.getTauxRemise());
                res.put("numPermis",            client.getNumPermis());
            }

            return ResponseEntity.ok(res);
        }).orElse(ResponseEntity.status(404)
                .body(Map.of("erreur", "Aucun compte avec cet e-mail.")));
    }

   
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String email = body.getOrDefault("email", "").toString().trim();

        if (uRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cet e-mail est déjà utilisé."));
        }

        Client client = new Client();
        client.setNom(body.getOrDefault("nom", "").toString());
        client.setPrenom(body.getOrDefault("prenom", "").toString());
        client.setEmail(email);
        
client.setPassword(passwordEncoder.encode(body.getOrDefault("password", "").toString()));
        client.setRole(Utilisateur.Role.CLIENT);
        client.setNbLocationsTerminees(0);

        if (body.containsKey("numPermis")) {
            client.setNumPermis(body.get("numPermis").toString());
        }

        Client saved = cRepo.save(client);

        return ResponseEntity.ok(Map.of(
            "message", "Compte créé avec succès.",
            "id",      saved.getIdUser(),
            "prenom",  saved.getPrenom(),
            "nom",     saved.getNom(),
            "email",   saved.getEmail(),
            "role",    "CLIENT"
        ));
    }
}