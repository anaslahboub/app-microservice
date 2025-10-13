import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostListComponent } from '../../components/post-list/post-list.component';

@Component({
  selector: 'app-posts',
  standalone: true,
  imports: [
    CommonModule,
    PostListComponent
  ],
  template: `
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <app-post-list></app-post-list>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      height: 100%;
    }
  `]
})
export class PostsComponent {
  constructor() { }
}