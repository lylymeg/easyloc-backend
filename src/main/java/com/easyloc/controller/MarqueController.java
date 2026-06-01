package com.easyloc.controller;

import com.easyloc.dao.MarqueRepository;
import com.easyloc.model.Marque;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/marques")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MarqueController {

    
    private final MarqueRepository mRepo;

    
    public MarqueController(MarqueRepository mRepo) { 
        this.mRepo = mRepo; 
    }

    
    @GetMapping
    public ResponseEntity<List<Marque>> getAll() { 
        return ResponseEntity.ok(mRepo.findAll()); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Marque> getById(@PathVariable Integer id) {
        return mRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

  
    @PostMapping
    public ResponseEntity<Marque> create(@RequestBody Marque m) { 
        return ResponseEntity.ok(mRepo.save(m)); 
    }

  
    @PutMapping("/{id}")
    public ResponseEntity<Marque> update(@PathVariable Integer id, @RequestBody Marque body) {
        return mRepo.findById(id).map(m -> {
            m.setNom(body.getNom());
            m.setLogo(body.getLogo());
            return ResponseEntity.ok(mRepo.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        return mRepo.findById(id).map(m -> {
            mRepo.delete(m);
            return ResponseEntity.ok(Map.of("message", "Marque supprimée avec succès."));
        }).orElse(ResponseEntity.notFound().build());
    }
}