package com.easyloc.dao;

import com.easyloc.model.AvisEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvisEvaluationRepository extends JpaRepository<AvisEvaluation, Integer> {
    List<AvisEvaluation> findByVehiculeIdVehiculeOrderByDateCreationDesc(Integer vehiculeId);
    List<AvisEvaluation> findByClientIdUser(Integer clientId);

    @Query("SELECT AVG(a.note) FROM AvisEvaluation a WHERE a.vehicule.idVehicule = :vid")
    Double moyenneNoteParVehicule(@Param("vid") Integer vehiculeId);
}