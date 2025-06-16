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

        int playersPerTeam = 11;
        int totalPlayers = allPlayers.size();
        int requiredPlayers = requestedTeams * playersPerTeam;

        if (totalPlayers < requiredPlayers) {
            int missingPlayers = requiredPlayers - totalPlayers;
            throw new RuntimeException(missingPlayers + " more player(s) required to generate " + requestedTeams + " team(s). (11 players per team)");
        }

// Proceed to calculate actual teams (safe to assume now)
        int maxPossibleTeams = totalPlayers / playersPerTeam;
        int actualTeams = Math.min(requestedTeams, maxPossibleTeams);

        if (actualTeams != requestedTeams) {
            System.out.println("Adjusted team count from " + requestedTeams + " to " + actualTeams +
                    " because only enough players for " + maxPossibleTeams + " complete teams");
        }


        // Initialize teams
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

        // Categorize players by their primary skill (based on highest rating)
        List<PlayerEntity> wkPlayers = new ArrayList<>();
        List<PlayerEntity> batPlayers = new ArrayList<>();
        List<PlayerEntity> bowlPlayers = new ArrayList<>();
        List<PlayerEntity> fieldPlayers = new ArrayList<>();

        for (PlayerEntity player : allPlayers) {
            double maxSkill = Math.max(Math.max(player.getBatRate(),
                            Math.max(player.getBowlRate(),
                                    Math.max(player.getWkRate(), player.getFieldRate()))),
                    0);

            if (player.getWkRate() == maxSkill && player.getWkRate() > 0) {
                wkPlayers.add(player);
            } else if (player.getBatRate() == maxSkill && player.getBatRate() > 0) {
                batPlayers.add(player);
            } else if (player.getBowlRate() == maxSkill && player.getBowlRate() > 0) {
                bowlPlayers.add(player);
            } else if (player.getFieldRate() > 0) {
                fieldPlayers.add(player);
            }
        }

        // Rest of the method remains the same...
        // [Previous implementation continues...]

        // Sort each category by their primary skill
        wkPlayers.sort(Comparator.comparingDouble(PlayerEntity::getWkRate).reversed());
        batPlayers.sort(Comparator.comparingDouble(PlayerEntity::getBatRate).reversed());
        bowlPlayers.sort(Comparator.comparingDouble(PlayerEntity::getBowlRate).reversed());
        fieldPlayers.sort(Comparator.comparingDouble(PlayerEntity::getFieldRate).reversed());

        // Assign players following cricket team composition rules
        // 1. Assign 1 wicket keeper to each team
        assignPlayersRoundRobin(wkPlayers, teams, 1);

        // 2. Assign 4 batsmen to each team
        assignPlayersByLowestSkill(batPlayers, teams, 4, PlayerSkill.BATSMAN);

        // 3. Assign 4 bowlers to each team
        assignPlayersByLowestSkill(bowlPlayers, teams, 4, PlayerSkill.BOWLER);

        // 4. Assign 2 fielders to each team
        assignPlayersByLowestSkill(fieldPlayers, teams, 2, PlayerSkill.FIELDER);

        // Verify all teams have exactly 11 players
        for (TeamEntity team : teams) {
            if (team.getPlayerIds().size() < 11) {
                // Fill remaining spots with best available players
                List<PlayerEntity> remainingPlayers = allPlayers.stream()
                        .filter(p -> !isPlayerAssigned(p.getPlayerId(), teams))
                        .sorted((p1, p2) -> Double.compare(
                                p2.getBatRate() + p2.getBowlRate() + p2.getFieldRate() + p2.getWkRate(),
                                p1.getBatRate() + p1.getBowlRate() + p1.getFieldRate() + p1.getWkRate()))
                        .collect(Collectors.toList());

                while (team.getPlayerIds().size() < 11 && !remainingPlayers.isEmpty()) {
                    PlayerEntity player = remainingPlayers.remove(0);
                    team.getPlayerIds().add(player.getPlayerId());
                    updateTeamRatings(team, player);
                }

                // If still not enough players, remove this incomplete team
                if (team.getPlayerIds().size() < 11) {
                    teams.remove(team);
                    System.out.println("Removed incomplete team with only " + team.getPlayerIds().size() + " players");
                }
            }
        }

        // If we ended up with no complete teams
        if (teams.isEmpty()) {
            throw new RuntimeException("Could not create any complete teams with 11 players each");
        }

        return repository.saveAll(teams);
    }

    private void assignPlayersRoundRobin(List<PlayerEntity> players, List<TeamEntity> teams, int perTeam) {
        int assigned = 0;
        int totalToAssign = teams.size() * perTeam;
        int teamIndex = 0;

        while (assigned < totalToAssign && !players.isEmpty()) {
            PlayerEntity player = players.remove(0);
            TeamEntity team = teams.get(teamIndex);

            if (team.getPlayerIds().size() < 11) {
                team.getPlayerIds().add(player.getPlayerId());
                updateTeamRatings(team, player);
                assigned++;
                teamIndex = (teamIndex + 1) % teams.size();
            } else {
                // This team is already full, move to next
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

            // Find team with lowest rating in this skill that isn't full
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
                    })
                    .orElse(null);

            if (targetTeam == null) break; // All teams are full

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