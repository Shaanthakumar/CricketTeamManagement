package com.cricket.CricketApi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "teams")
public class TeamEntity {
    @Id
    private String id;  // MongoDB ObjectId

    private String teamName;
    private List<Long> playerIds;  // Changed to Long to store player's business ID
    private double totalBatRating;
    private double totalBowlRating;
    private double totalWkRating;
    private double totalFieldRating;
}