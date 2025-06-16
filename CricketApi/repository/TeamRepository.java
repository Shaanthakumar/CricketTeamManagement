package com.cricket.CricketApi.repository;

import com.cricket.CricketApi.entity.TeamEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TeamRepository extends MongoRepository<TeamEntity, String> {  // Changed ID type to String
    Optional<TeamEntity> findByTeamName(String teamName);
    boolean existsByTeamName(String teamName);
    void deleteByTeamName(String teamName);
}