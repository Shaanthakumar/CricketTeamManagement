package com.cricket.CricketApi.service;

import com.cricket.CricketApi.entity.PlayerEntity;
import com.cricket.CricketApi.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    public PlayerEntity insertPlayer(PlayerEntity player) {
        if (repository.existsByPlayerId(player.getPlayerId())) {
            throw new RuntimeException("Player with ID " + player.getPlayerId() + " already exists.");
        }
        return repository.save(player);
    }

    public PlayerEntity updatePlayer(long playerId, PlayerEntity playerDetails) {
        PlayerEntity existingPlayer = repository.findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with id " + playerId));

        existingPlayer.setName(playerDetails.getName());
        existingPlayer.setDob(playerDetails.getDob());
        existingPlayer.setBatRate(playerDetails.getBatRate());
        existingPlayer.setBowlRate(playerDetails.getBowlRate());
        existingPlayer.setWkRate(playerDetails.getWkRate());
        existingPlayer.setFieldRate(playerDetails.getFieldRate());
        existingPlayer.setMatchesPlayed(playerDetails.getMatchesPlayed());

        return repository.save(existingPlayer);
    }

    public void deletePlayer(long playerId) {
        if (!repository.existsByPlayerId(playerId)) {
            throw new RuntimeException("Player not found with id " + playerId);
        }
        repository.deleteByPlayerId(playerId);
    }

    public List<PlayerEntity> getAllPlayers() {
        return repository.findAll();
    }

    public Optional<PlayerEntity> getPlayerById(String id) {  // Changed parameter type to String
        return repository.findById(id);
    }

    public Optional<PlayerEntity> getPlayerByPlayerId(long playerId) {  // New method for business ID
        return repository.findByPlayerId(playerId);
    }
}