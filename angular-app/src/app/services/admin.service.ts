import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GroupMemberDto, UserResponse } from '../group-services/models';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = '/api/admin';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  assignRole(userId: string, role: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/${userId}/role?role=${role}`, {});
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${userId}`);
  }

  createUser(user: GroupMemberDto): Observable<GroupMemberDto> {
    return this.http.post<GroupMemberDto>(`${this.apiUrl}/users`, user);
  }
}