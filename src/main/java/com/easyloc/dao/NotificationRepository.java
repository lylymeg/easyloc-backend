package com.easyloc.dao;

import com.easyloc.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Récupérer toutes les notifications d'un utilisateur (la plus récente d'abord)
    List<Notification> findByUserIdUserOrderByDateEnvoiDesc(Integer userId);

    // Récupérer uniquement les notifications non lues
    List<Notification> findByUserIdUserAndLuFalseOrderByDateEnvoiDesc(Integer userId);

    // Compter le nombre de notifications non lues (pour afficher une pastille rouge sur l'icône cloche)
    long countByUserIdUserAndLuFalse(Integer userId);
}

