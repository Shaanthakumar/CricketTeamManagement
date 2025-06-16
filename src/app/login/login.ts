import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  username: string = '';
  password: string = '';
  showPassword: boolean = false;
  notificationMessage: string = '';
  isSuccess: boolean = false;

  constructor(private router: Router) {}

  onSubmit() {
    if (this.username === 'sk0404' && this.password === 'admin') {
      this.showNotification('✅ Login successful!', true);
      setTimeout(() => {
        this.router.navigate(['/home']);
      }, 1500); // Navigate after 1.5 seconds
    } else {
      this.showNotification('❌ Invalid credentials', false);
    }
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  private showNotification(message: string, isSuccess: boolean) {
    this.notificationMessage = message;
    this.isSuccess = isSuccess;
    
    // Clear the notification after 3 seconds
    setTimeout(() => {
      this.notificationMessage = '';
    }, 3000);
  }
}