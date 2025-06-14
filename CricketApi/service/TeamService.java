package com.cricket.CricketApi.service;

import com.cricket.CricketApi.entity.PlayerEntity;
import com.cricket.CricketApi.entity.TeamEntity;
import com.cricket.CricketApi.repository.PlayerRepository;
import com.cricket.CricketApi.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {
    private final TeamRepository repository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository repository, PlayerRepository playerRepository) {
        this.repository = repository;
        this.playerRepository = playerRepository;
    }

    public TeamEntity addTeam(TeamEntity team) {
        if (repository.existsByTeamName(team.getTeamName())) {
            throw new RuntimeException("Team with name " + team.getTeamName() + " already exists.");
        }
        return repository.save(team);
    }

    public Optional<TeamEntity> getTeamByName(String teamName) {
        return repository.findByTeamName(teamName);
    }

    public List<TeamEntity> getAllTeam() {
        return repository.findAll();
    }

    public void deleteTeam(String teamName) {
        if (!repository.existsByTeamName(teamName)) {
            throw new RuntimeException("Team not found with name: " + teamName);
        }
        repository.deleteByTeamName(teamName);
    }

    public void deleteAllTeam() {
        repository.deleteAll();
    }

    public List<TeamEntity> createBalancedTeams(int requestedTeams) {
        List<PlayerEntity> allPlayers = playerRepository.findAll();
        if (allPlayers.isEmpty()) {
            throw new RuntimeException("No players available to create teams");
        }

        int totalPlayers = allPlayers.size();
        int actualTeams = Math.min(requestedTeams, totalPlayers / 11);

        if (actualTeams < 1) {
            throw new RuntimeException("Not enough players to create even 1 team (need at least 11 players)");
        }

        if (actualTeams != requestedTeams) {
            System.out.println("Adjusted team count from " + requestedTeams + " to " + actualTeams +
                    " due to insufficient players");
        }

        List<TeamEntity> teams = new ArrayList<>();
        for (int i = 0; i < actualTeams; i++) {
            TeamEntity team = new TeamEntity();
            team.setTeamName("Team " + (i + 1));
            team.setPlayerIds(new ArrayList<>());
            team.setTotalBatRating(0);
            team.setTotalBowlRating(0);
            team.setTotalWkRating(0);
            team.setTotalFieldRating(0);
            teams.add(team);
        }

        // Filter players by roles
        List<PlayerEntity> wkPlayers = allPlayers.stream()
                .filter(p -> p.getWkRate() > 0)
                .sorted(Comparator.comparingDouble(PlayerEntity::getWkRate).reversed())
                .collect(Collectors.toList());

        List<PlayerEntity> batPlayers = allPlayers.stream()
                .filter(p -> p.getBatRate() > 0)
                .sorted(Comparator.comparingDouble(PlayerEntity::getBatRate).reversed())
                .collect(Collectors.toList());

        List<PlayerEntity> bowlPlayers = allPlayers.stream()
                .filter(p -> p.getBowlRate() > 0)
                .sorted(Comparator.comparingDouble(PlayerEntity::getBowlRate).reversed())
                .collect(Collectors.toList());

        List<PlayerEntity> fieldPlayers = allPlayers.stream()
                .filter(p -> p.getFieldRate() > 0)
                .sorted(Comparator.comparingDouble(PlayerEntity::getFieldRate).reversed())
                .collect(Collectors.toList());

        // 1. Assign 1 WK to each team directly (best WKs)
        assignPlayersRoundRobin(wkPlayers, teams, 1);

        // 2. Assign 4 Batsmen, adding one at a time to team with lowest totalBatRating
        assignPlayersByLowestSkill(batPlayers, teams, 4, PlayerSkill.BATSMAN);

        // 3. Assign 4 Bowlers similarly using totalBowlRating
        assignPlayersByLowestSkill(bowlPlayers, teams, 4, PlayerSkill.BOWLER);

        // 4. Assign 2 Fielders similarly using totalFieldRating
        assignPlayersByLowestSkill(fieldPlayers, teams, 2, PlayerSkill.FIELDER);

        // 5. Fill remaining spots (up to 11 per team) with best available players by combined rating
        List<PlayerEntity> remainingPlayers = allPlayers.stream()
                .filter(p -> !isPlayerAssigned(p.getPlayerId(), teams))
                .sorted((p1, p2) -> Double.compare(
                        p2.getBatRate() + p2.getBowlRate() + p2.getFieldRate() + p2.getWkRate(),
                        p1.getBatRate() + p1.getBowlRate() + p1.getFieldRate() + p1.getWkRate()))
                .collect(Collectors.toList());

        for (TeamEntity team : teams) {
            while (team.getPlayerIds().size() < 11 && !remainingPlayers.isEmpty()) {
                PlayerEntity player = remainingPlayers.remove(0);
                team.getPlayerIds().add(player.getPlayerId());
                updateTeamRatings(team, player);
            }
        }

        return repository.saveAll(teams);
    }

    private void assignPlayersRoundRobin(List<PlayerEntity> players, List<TeamEntity> teams, int perTeam) {
        int assigned = 0;
        int totalToAssign = teams.size() * perTeam;
        int teamIndex = 0;
        while (assigned < totalToAssign && !players.isEmpty()) {
            PlayerEntity player = players.remove(0);
            if (isPlayerAssigned(player.getPlayerId(), teams)) {
                continue;
            }
            TeamEntity team = teams.get(teamIndex);
            if (team.getPlayerIds().size() < 11) {
                team.getPlayerIds().add(player.getPlayerId());
                updateTeamRatings(team, player);
                assigned++;
                teamIndex = (teamIndex + 1) % teams.size();
            }
        }
    }

    private void assignPlayersByLowestSkill(List<PlayerEntity> players, List<TeamEntity> teams,
                                            int playersPerTeam, PlayerSkill skill) {
        int totalToAssign = teams.size() * playersPerTeam;
        int assigned = 0;

        while (assigned < totalToAssign && !players.isEmpty()) {
            PlayerEntity player = players.remove(0);

            if (isPlayerAssigned(player.getPlayerId(), teams)) {
                continue;
            }

            TeamEntity targetTeam = teams.stream()
                    .filter(t -> t.getPlayerIds().size() < 11)
                    .min((t1, t2) -> {
                        switch (skill) {
                            case BATSMAN:
                                return Double.compare(t1.getTotalBatRating(), t2.getTotalBatRating());
                            case BOWLER:
                                return Double.compare(t1.getTotalBowlRating(), t2.getTotalBowlRating());
                            case FIELDER:
                                return Double.compare(t1.getTotalFieldRating(), t2.getTotalFieldRating());
                            default:
                                return 0;
                        }
                    }).orElse(null);

            if (targetTeam == null) break;

            targetTeam.getPlayerIds().add(player.getPlayerId());
            updateTeamRatings(targetTeam, player);
            assigned++;
        }
    }

    private boolean isPlayerAssigned(Long playerId, List<TeamEntity> teams) {
        return teams.stream()
                .anyMatch(team -> team.getPlayerIds().contains(playerId));
    }

    private void updateTeamRatings(TeamEntity team, PlayerEntity player) {
        team.setTotalBatRating(team.getTotalBatRating() + player.getBatRate());
        team.setTotalBowlRating(team.getTotalBowlRating() + player.getBowlRate());
        team.setTotalWkRating(team.getTotalWkRating() + player.getWkRate());
        team.setTotalFieldRating(team.getTotalFieldRating() + player.getFieldRate());
    }

    private enum PlayerSkill {
        BATSMAN, BOWLER, FIELDER
    }
}