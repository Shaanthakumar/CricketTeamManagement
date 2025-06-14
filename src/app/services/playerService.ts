import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Player {
  playerId: number;        // Business ID
  name: string;
  dob: string;
  batRate: number;
  bowlRate: number;
  wkRate: number;
  fieldRate: number;
  matchesPlayed: number;
}

@Injectable({ providedIn: 'root' })
export class PlayerService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/players';

  getAllPlayers(): Observable<Player[]> {
    return this.http.get<Player[]>(this.apiUrl);
  }

  getPlayerById(id: string): Observable<Player> {
    return this.http.get<Player>(`${this.apiUrl}/id/${id}`);
  }

  getPlayerByPlayerId(playerId: number): Observable<Player> {
    return this.http.get<Player>(`${this.apiUrl}/playerId/${playerId}`);
  }

  createPlayer(player: Player): Observable<Player> {
    return this.http.put<Player>(this.apiUrl, player);
  }

  updatePlayer(playerId: number, player: Player): Observable<Player> {
    return this.http.post<Player>(`${this.apiUrl}/${playerId}`, player);
  }

  deletePlayer(playerId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${playerId}`);
  }
}