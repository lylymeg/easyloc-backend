package com.easyloc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easyloc.dao.ReservationRepository;
import com.easyloc.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class EasyLocApplication {
    public static void main(String[] args) {
    
  
    System.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/easyloc");
    System.setProperty("spring.datasource.username", "root");
    System.setProperty("spring.datasource.password", "");
    System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
    
    
    System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.MySQL8Dialect");

    SpringApplication.run(EasyLocApplication.class, args);

    }
}


@Component
class RetardScheduler {

    @Autowired
    private ReservationRepository rRepo;

   
    @Scheduled(fixedRate = 3600000)
    public void detecterRetards() {
        List<Reservation> aVerifier = rRepo.findByStatutReservation(Reservation.Statut.CONFIRMEE);
        int count = 0;
        for (Reservation r : aVerifier) {
    if (r.getDateFin() != null && r.getDateFin().isBefore(LocalDateTime.now())) {
        
        long jours = java.time.temporal.ChronoUnit.DAYS.between(r.getDateFin(), LocalDateTime.now());
        
        
        r.setStatutReservation(Reservation.Statut.RETARD);
        r.setJoursRetard((int) jours);
        r.setMontantPenalite(r.calculerPenalite());
        
        rRepo.save(r);
        count++;
    }
}
        if (count > 0) {
            System.out.println("[RetardScheduler] " + count + " réservation(s) basculée(s) en RETARD.");
        }
    }
}
