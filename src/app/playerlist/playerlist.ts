import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlayerService, Player } from '../services/playerService';
import { ModalComponent } from './modal';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-player-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './playerlist.html',
  styleUrls: ['./playerlist.css']
})
export class PlayerListComponent implements OnInit {
  private playerService = inject(PlayerService);
  private cdr = inject(ChangeDetectorRef);

  players: Player[] = [];
  originalPlayers: Player[] = [];
  filteredPlayers: Player[] = [];


  editedPlayer: Player = this.initEmptyPlayer();
  playerToDelete: Player | null = null;
  showAddModal = false;
  showEditModal = false;
  showDeleteModal = false;
  newPlayer: Player = this.initEmptyPlayer();
  formErrors: string[] = [];

  // Sorting, search, loading
  currentSort: string | null = null;
  sortOrder: 'asc' | 'desc' = 'desc';
  searchTerm = '';
  isLoading = true;
  isSubmitting = false;
  loadError = false;
  isDataReady = false

  async ngOnInit() {
    await this.loadPlayers();
    this.updateFilteredPlayers();
  }


  async loadPlayers() {
  this.isLoading = true;

  await this.playerService.getAllPlayers().subscribe({
    next: (data) => {
      this.players = data || [];
      this.originalPlayers = [...this.players];
      this.updateFilteredPlayers();
      this.isLoading = false;
      this.isDataReady = true;
      this.cdr.detectChanges(); // Trigger change detection after data load
    },
    error: (err) => {
      console.error('Error loading players:', err);
      this.isLoading = false;
      this.loadError = true;
    }
  });
}

  private initEmptyPlayer(): Player {
    return {
      playerId: 0,
      name: '',
      dob: '',
      batRate: 0,
      bowlRate: 0,
      wkRate: 0,
      fieldRate: 0,
      matchesPlayed: 0
    };
  }

  

  refreshPlayers(): void {
    this.loadPlayers();
  }

  private updateFilteredPlayers(): void {
    let playersToFilter = [...this.players];
    
    if (this.currentSort) {
      playersToFilter = this.applySortingToArray(playersToFilter, this.currentSort);
    }
    
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      playersToFilter = playersToFilter.filter(player =>
        player.name.toLowerCase().includes(term) ||
        player.playerId.toString().includes(term)
      );
    }
    
    this.filteredPlayers = playersToFilter;
  }

  trackByPlayerId(index: number, player: Player): number {
    return player.playerId;
  }

  onSearchChange(): void {
    this.updateFilteredPlayers();
  }

  sortPlayers(criteria: string): void {
    if (this.currentSort === criteria) {
      this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc';
    } else {
      this.currentSort = criteria;
      this.sortOrder = criteria === 'name' ? 'asc' : 'desc';
    }
    this.updateFilteredPlayers();
  }

  private applySortingToArray(playersArray: Player[], criteria: string): Player[] {
    return playersArray.sort((a, b) => {
      let valueA: any;
      let valueB: any;

      switch (criteria) {
        case 'playerId':
          valueA = a.playerId || 0;
          valueB = b.playerId || 0;
          break;
        case 'batRate':
          valueA = a.batRate || 0;
          valueB = b.batRate || 0;
          break;
        case 'bowlRate':
          valueA = a.bowlRate || 0;
          valueB = b.bowlRate || 0;
          break;
        case 'wkRate':
          valueA = a.wkRate || 0;
          valueB = b.wkRate || 0;
          break;
        case 'fieldRate':
          valueA = a.fieldRate || 0;
          valueB = b.fieldRate || 0;
          break;
        case 'matchesPlayed':
          valueA = a.matchesPlayed || 0;
          valueB = b.matchesPlayed || 0;
          break;
        case 'name':
          valueA = a.name?.toLowerCase() || '';
          valueB = b.name?.toLowerCase() || '';
          break;
        case 'dob':
          valueA = a.dob ? new Date(a.dob).getTime() : 0;
          valueB = b.dob ? new Date(b.dob).getTime() : 0;
          break;
        default:
          return 0;
      }

      if (criteria === 'name') {
        return this.sortOrder === 'asc' 
          ? valueA.localeCompare(valueB)
          : valueB.localeCompare(valueA);
      } else {
        return this.sortOrder === 'desc' 
          ? valueB - valueA 
          : valueA - valueB;
      }
    });
  }

  resetSort(): void {
    this.currentSort = null;
    this.sortOrder = 'desc';
    this.updateFilteredPlayers();
  }

  getSortDisplayName(sortKey: string): string {
    const displayNames: {[key: string]: string} = {
      'playerId': 'Player ID',
      'batRate': 'Batting Rate',
      'bowlRate': 'Bowling Rate',
      'wkRate': 'Wicket Keeping Rate',
      'fieldRate': 'Fielding Rate',
      'matchesPlayed': 'Matches Played',
      'name': 'Name',
      'dob': 'Date of Birth'
    };
    return displayNames[sortKey] || sortKey;
  }

  validatePlayer(player: Player): boolean {
    this.formErrors = [];
    
    if (!player.playerId || isNaN(player.playerId) || player.playerId <= 0) {
      this.formErrors.push('Player ID is required and must be a positive number');
    }
    if (!player.name?.trim()) {
      this.formErrors.push('Name is required');
    }
    if (!player.dob) {
      this.formErrors.push('Date of Birth is required');
    } else {
      const dobDate = new Date(player.dob);
      const today = new Date();
      if (dobDate > today) {
        this.formErrors.push('Date of Birth cannot be in the future');
      }
    }
    
    const rates = [
      { name: 'Batting Rate', value: player.batRate },
      { name: 'Bowling Rate', value: player.bowlRate },
      { name: 'Wicket Keeping Rate', value: player.wkRate },
      { name: 'Fielding Rate', value: player.fieldRate }
    ];
    
    rates.forEach(rate => {
      if (rate.value !== null && rate.value !== undefined && (rate.value < 0 || rate.value > 100)) {
        this.formErrors.push(`${rate.name} must be between 0 and 100`);
      }
    });
    
    if (player.matchesPlayed < 0) {
      this.formErrors.push('Matches Played cannot be negative');
    }
    
    return this.formErrors.length === 0;
  }

  editPlayer(player: Player): void {
    this.editedPlayer = { ...player };
    this.formErrors = [];
    this.showEditModal = true;
  }

  onSubmitEdit(): void {
    if (this.validatePlayer(this.editedPlayer)) {
      this.isSubmitting = true;
      
      const originalPlayer = this.players.find(p => p.playerId === this.editedPlayer.playerId);
      const playerIndex = this.players.findIndex(p => p.playerId === this.editedPlayer.playerId);
      
      if (playerIndex !== -1) {
        this.players[playerIndex] = { ...this.editedPlayer };
        this.originalPlayers[playerIndex] = { ...this.editedPlayer };
        this.updateFilteredPlayers();
      }
      this.showEditModal = false;
        this.isSubmitting = false;
      this.playerService.updatePlayer(this.editedPlayer.playerId, this.editedPlayer).subscribe({
        next: () => {
          
        },
        
        error: (err) => {
          console.error('Update failed:', err);
          if (originalPlayer && playerIndex !== -1) {
            this.players[playerIndex] = originalPlayer;
            this.originalPlayers[playerIndex] = originalPlayer;
            this.updateFilteredPlayers();
          }
          this.formErrors.push('Failed to update player. Please try again.');
          this.isSubmitting = false;
        }
      });
    }
  }

  onCancelEdit(): void {
    this.showEditModal = false;
    this.formErrors = [];
    this.editedPlayer = this.initEmptyPlayer();
  }

  openAddModal(): void {
    this.newPlayer = this.initEmptyPlayer();
    this.formErrors = [];
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.formErrors = [];
    this.newPlayer = this.initEmptyPlayer();
  }

  onSubmitAdd(): void {
    if (this.validatePlayer(this.newPlayer)) {
      const existingPlayer = this.players.find(p => p.playerId === this.newPlayer.playerId);
      if (existingPlayer) {
        this.formErrors.push('Player ID already exists. Please use a different ID.');
        return;
      }
      
      const newPlayerCopy = { ...this.newPlayer, id: `temp_${Date.now()}` };
      
      this.players.push(newPlayerCopy);
      this.originalPlayers.push(newPlayerCopy);
      this.updateFilteredPlayers();
      
      this.playerService.createPlayer(this.newPlayer).subscribe({
        next: (response) => {
          const tempIndex = this.players.findIndex(p => p.playerId === newPlayerCopy.playerId);
          if (tempIndex !== -1 && response) {
            this.players[tempIndex] = response;
            this.originalPlayers[tempIndex] = response;
            this.updateFilteredPlayers();
          }
          
          
        },
        error: (err) => {
          console.error('Add player failed:', err);
          this.players = this.players.filter(p => p.playerId !== newPlayerCopy.playerId);
          this.originalPlayers = this.originalPlayers.filter(p => p.playerId !== newPlayerCopy.playerId);
          this.updateFilteredPlayers();
          
          this.formErrors.push('Failed to add player. Please try again.');
          this.isSubmitting = false;
        }
      });
    }
  }

  deletePlayer(player: Player): void {
    this.playerToDelete = player;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    if (this.playerToDelete) {
      this.isSubmitting = true;
      
      const playerToRemove = this.playerToDelete;
      const playerIndex = this.players.findIndex(p => p.playerId === playerToRemove.playerId);
      const originalPlayerIndex = this.originalPlayers.findIndex(p => p.playerId === playerToRemove.playerId);
      
      if (playerIndex !== -1) {
        this.players.splice(playerIndex, 1);
      }
      if (originalPlayerIndex !== -1) {
        this.originalPlayers.splice(originalPlayerIndex, 1);
      }
      this.updateFilteredPlayers();
      this.showDeleteModal = false;
      this.playerService.deletePlayer(playerToRemove.playerId).subscribe({
        next: () => {
          
          this.playerToDelete = null;
          this.isSubmitting = false;
        },
        error: (err) => {
          console.error('Delete failed:', err);
          if (playerIndex !== -1) {
            this.players.splice(playerIndex, 0, playerToRemove);
          }
          if (originalPlayerIndex !== -1) {
            this.originalPlayers.splice(originalPlayerIndex, 0, playerToRemove);
          }
          this.updateFilteredPlayers();
          this.isSubmitting = false;
        }
      });
    }
  }

  onCancelDelete(): void {
    this.showDeleteModal = false;
    this.playerToDelete = null;
  }

  calculateAge(dob: string): number | null {
    if (!dob) return null;
    const birthDate = new Date(dob);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return age;
  }

  getPlayerOverallRating(player: Player): number {
    const rates = [
      player.batRate || 0,
      player.bowlRate || 0,
      player.wkRate || 0,
      player.fieldRate || 0
    ];
    return Math.round(rates.reduce((sum, rate) => sum + rate, 0) / rates.length);
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'Not specified';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}