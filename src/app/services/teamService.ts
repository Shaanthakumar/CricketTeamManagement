import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface Team {
  id: string;                     // MongoDB ObjectId
  teamName: string;
  playerIds: number[];           // Business IDs of players
  totalBatRating: number;
  totalBowlRating: number;
  totalWkRating: number;
  totalFieldRating: number;
}

@Injectable({ providedIn: 'root' })
export class TeamService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/teams';

  getAllTeams(): Observable<Team[]> {
    return this.http.get<Team[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  createBalancedTeams(numberOfTeams: number): Observable<Team[]> {
    return this.http.post(
      `${this.apiUrl}/balanced-teams/${numberOfTeams}`,
      {},
      { responseType: 'text' }
    ).pipe(
      map(response => {
        try {
          return JSON.parse(response) as Team[];
        } catch (e) {
          throw new Error(response);
        }
      }),
      catchError(this.handleError)
    );
  }

  deleteTeam(teamName: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${encodeURIComponent(teamName)}`).pipe(
      catchError(this.handleError)
    );
  }

  deleteAllTeams(): Observable<void> {
    return this.http.delete<void>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse | Error) {
    let errorMessage = 'An unknown error occurred';
    
    if (error instanceof Error) {
      errorMessage = error.message;
    } else if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else if (typeof error.error === 'string') {
      errorMessage = error.error;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    return throwError(() => ({ message: errorMessage }));
  }
}