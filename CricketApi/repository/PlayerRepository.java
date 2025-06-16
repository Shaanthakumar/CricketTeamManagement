package com.cricket.CricketApi.repository;

import com.cricket.CricketApi.entity.PlayerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlayerRepository extends MongoRepository<PlayerEntity, String> {  // Changed ID type to String
    Optional<PlayerEntity> findByPlayerId(long playerId);
    void deleteByPlayerId(long playerId);
    boolean existsByPlayerId(long playerId);
}