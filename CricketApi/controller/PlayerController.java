package com.cricket.CricketApi.controller;

import com.cricket.CricketApi.entity.PlayerEntity;
import com.cricket.CricketApi.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins="*")
@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }
@CrossOrigin
    @PutMapping
    public PlayerEntity insertPlayer(@RequestBody PlayerEntity player) {
        return service.insertPlayer(player);
    }
@CrossOrigin
    @PostMapping("/{playerId}")
    public PlayerEntity updatePlayer(@PathVariable long playerId, @RequestBody PlayerEntity playerDetails) {
        return service.updatePlayer(playerId, playerDetails);
    }
@CrossOrigin
    @DeleteMapping("/{playerId}")
    public ResponseEntity<?> deletePlayer(@PathVariable long playerId) {
        service.deletePlayer(playerId);
        return ResponseEntity.ok().build();
    }
@CrossOrigin
    @GetMapping("/id/{id}")
    public ResponseEntity<PlayerEntity> getPlayerById(@PathVariable String id) {
        return service.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
@CrossOrigin
    @GetMapping("/playerId/{playerId}")
    public ResponseEntity<PlayerEntity> getPlayerByPlayerId(@PathVariable long playerId) {
        return service.getPlayerByPlayerId(playerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
@CrossOrigin
    @GetMapping
    public List<PlayerEntity> getAllPlayers() {
        return service.getAllPlayers();
    }
}