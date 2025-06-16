package com.cricket.CricketApi.controller;

import com.cricket.CricketApi.entity.TeamEntity;
import com.cricket.CricketApi.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> insertTeam(@RequestBody TeamEntity team) {
        try {
            return ResponseEntity.ok(service.addTeam(team));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/balanced-teams/{numberOfTeams}")
    public ResponseEntity<?> createBalancedTeams(@PathVariable int numberOfTeams) {
        try {
            List<TeamEntity> teams = service.createBalancedTeams(numberOfTeams);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{teamName}")
    public ResponseEntity<?> deleteTeam(@PathVariable String teamName) {
        try {
            service.deleteTeam(teamName);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllTeam() {
        service.deleteAllTeam();
        return ResponseEntity.ok().build();  // Changed to return empty response
    }

    @GetMapping
    public List<TeamEntity> getAllTeams() {
        return service.getAllTeam();
    }

    @GetMapping("/{teamName}")
    public ResponseEntity<?> getTeamByName(@PathVariable String teamName) {
        try {
            return ResponseEntity.ok(service.getTeamByName(teamName).orElseThrow());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}