import { Component, Inject, OnInit, Renderer2 } from '@angular/core';
import { StrategicalProjectDataSet } from './project/project/strategical-project-data-set.model';
import { ProjectService } from './project/project/project.service';
import { HttpClient } from '@angular/common/http';
import { DOCUMENT } from '@angular/common';

@Component({
    selector: 'sdb-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
    projects: StrategicalProjectDataSet[] = [];

    constructor(
        private http: HttpClient,
        private projectService: ProjectService,
        private renderer: Renderer2,
        @Inject(DOCUMENT) private document: Document) {
    }

    ngOnInit() {
        this.projectService.getProjects().subscribe(projects => {
            this.projects = projects;
            this.renderer.removeClass(this.document.body, 'app-loading');
        });
    }
}
