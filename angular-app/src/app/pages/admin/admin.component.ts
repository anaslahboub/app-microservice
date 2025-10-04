import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { UserResponse } from '../../group-services/models';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  users: UserResponse[] = [];
  showAddUserModal = false;
  newUser: any = {
    userId: '',
    firstName: '',
    lastName: '',
    email: '',
    role: 'STUDENT'
  };

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: (error) => {
        console.error('Error loading users:', error);
      }
    });
  }

  assignRole(userId: string, role: string): void {
    this.adminService.assignRole(userId, role).subscribe({
      next: () => {
        console.log('Role assigned successfully');
      },
      error: (error) => {
        console.error('Error assigning role:', error);
      }
    });
  }

  deleteUser(userId: string): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          console.log('User deleted successfully');
          this.loadUsers(); // Refresh the user list
        },
        error: (error) => {
          console.error('Error deleting user:', error);
        }
      });
    }
  }

  showAddUserForm(): void {
    this.showAddUserModal = true;
  }

  closeAddUserForm(): void {
    this.showAddUserModal = false;
    this.resetNewUserForm();
  }

  resetNewUserForm(): void {
    this.newUser = {
      userId: '',
      firstName: '',
      lastName: '',
      email: '',
      role: 'STUDENT'
    };
  }

  addUser(): void {
    // In a real implementation, you would call the admin service to create the user
    // For now, we'll just close the modal and refresh the user list
    this.closeAddUserForm();
    this.loadUsers();
  }
}