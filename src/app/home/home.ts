import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlayerListComponent } from '../playerlist/playerlist';
import { TeamsComponent } from '../teamlist/teamlist';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, PlayerListComponent, TeamsComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  currentView: string = 'players'; // Set default view to players

  ngOnInit(): void {
    this.currentView = 'players'; 
  }
  showPlayers() {
    this.currentView = 'players';
  }
  
  showTeams() {
    this.currentView = 'teams';
  }
}