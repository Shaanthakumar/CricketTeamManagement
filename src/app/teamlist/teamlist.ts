import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TeamService, Team } from '../services/teamService';

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teamlist.html',
  styleUrls: ['./teamlist.css'],
})
export class TeamsComponent implements OnInit {
  teams: Team[] = [];
  numberOfTeams: number = 1;
  deleteTeamName: string = '';
  isGeneratingTeams: boolean = false;
  showPlayers: boolean[] = [];
  isLoading = true;
  loadError = false;

  message = {
    text: '',
    type: '', // 'success', 'error', or 'info'
    dismissible: true
  };

  private teamService = inject(TeamService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.loadTeams();
  }

  clearMessage(): void {
    this.message = { text: '', type: '', dismissible: true };
    this.cdr.detectChanges();
  }

  showMessage(text: string, type: string, dismissible: boolean = true): void {
    this.message = { text, type, dismissible };
    this.cdr.detectChanges();
    
    // Auto-hide success messages after 5 seconds
    if (type === 'success') {
      setTimeout(() => this.clearMessage(), 5000);
    }
  }

  loadTeams(): void {
    this.isLoading = true;
    this.loadError = false;
    this.clearMessage();
    
    this.teamService.getAllTeams().subscribe({
      next: (teams) => {
        this.teams = teams;
        this.showPlayers = new Array(teams.length).fill(false);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading teams:', err);
        this.isLoading = false;
        this.loadError = true;
        this.showMessage('Failed to load teams. Please try again.', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  generateTeams(): void {
  if (!this.numberOfTeams || this.numberOfTeams < 1) {
    this.showMessage('Please enter a valid number of teams (minimum 1)', 'error');
    return;
  }


  this.teamService.deleteAllTeams().subscribe({
    next: () => {
      this.teams = [];
      this.showPlayers = [];
      this.teamService.createBalancedTeams(this.numberOfTeams).subscribe({
        next: (teams) => {
          this.teams = teams;
          this.showPlayers = new Array(teams.length).fill(false);
          this.isGeneratingTeams = false;
          
          if (teams.length < this.numberOfTeams) {
            this.showMessage(
              `Number of teams reduced from ${this.numberOfTeams} to ${teams.length} due to insufficient players`,
              'info'
            );
          } else {
            this.showMessage(`Successfully generated ${teams.length} balanced team(s)`, 'success');
          }
        },
        error: (err) => {
          this.isGeneratingTeams = false;
          this.showMessage(err.message || 'Failed to generate teams', 'error');
        }
      });
    },
    error: (err) => {
      this.isGeneratingTeams = false;
      this.showMessage('Failed to delete existing teams: ' + (err.error?.message || err.message), 'error');
    }
  });
}

  // In teamlist.ts, replace the deleteTeam method with this:
deleteTeam(teamName: string): void {
  if (!teamName) {
    this.showMessage('No team specified for deletion', 'error');
    return;
  }

  this.teamService.deleteTeam(teamName).subscribe({
  next: () => {
    this.showMessage(`Team "${teamName}" deleted successfully`, 'success');
    this.loadTeams(); // Refresh the team list
  },
  error: (err) => {
    this.showMessage(`Failed to delete team "${teamName}": ${err.message}`, 'error');
  }
});

}

  deleteAllTeams(): void {
    if (!this.teams.length) {
      this.showMessage('No teams to delete', 'info');
      return;
    }



    this.teamService.deleteAllTeams().subscribe({
      next: () => {
        this.teams = [];
        this.showPlayers = [];
        this.showMessage(`All ${this.teams.length} teams deleted successfully`, 'success');
        this.loadTeams();
      },
      error: (err) => {
        this.showMessage('Failed to delete all teams: ' + (err.error?.message || err.message), 'error');
      }
    });
  }

  togglePlayers(index: number): void {
    this.showPlayers[index] = !this.showPlayers[index];
  }

  getTotalRating(team: Team): number {
    return team.totalBatRating + team.totalBowlRating + 
           team.totalWkRating + team.totalFieldRating;
  }

  formatRating(rating: number): string {
    return rating.toFixed(1);
  }
}