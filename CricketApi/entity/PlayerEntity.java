package com.cricket.CricketApi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "players")
public class PlayerEntity {
    @Id
    private String id;  // Changed from long to String to match MongoDB ObjectId
    private long playerId;
    private String name;
    private String dob;
    private double batRate;
    private double bowlRate;
    private double wkRate;
    private double fieldRate;
    private int matchesPlayed;
}