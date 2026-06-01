package com.easyloc.controller;

import com.easyloc.dao.NotificationRepository;
import com.easyloc.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class NotificationController {

    private final NotificationRepository repo;

    public NotificationController(NotificationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getByUser(@RequestParam Integer userId) {
    
        return ResponseEntity.ok(repo.findByUserIdUserOrderByDateEnvoiDesc(userId));
    }

    @GetMapping("/non-lues")
    public ResponseEntity<?> getNonLues(@RequestParam Integer userId) {
        List<Notification> nonLues = repo.findByUserIdUserAndLuFalseOrderByDateEnvoiDesc(userId);
        return ResponseEntity.ok(Map.of(
            "notifs", nonLues, 
            "count", nonLues.size()
        ));
    }

    @PutMapping("/{id}/lire")
    public ResponseEntity<?> marquerLue(@PathVariable Integer id) {
        return repo.findById(id).map(n -> {
            n.setLu(true);
            repo.save(n);
            return ResponseEntity.ok(Map.of("message", "Lue."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return repo.findById(id).map(n -> {
            repo.delete(n);
            return ResponseEntity.ok(Map.of("message", "Supprimée."));
        }).orElse(ResponseEntity.notFound().build());
    }
}