import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap} from "@angular/router";
import {ProjectConfigService} from "./project-config.service";

@Component({
    selector: 'tb-project-config',
    templateUrl: './project-config.component.html',
    styleUrls: ['./project-config.component.scss'],
    host: {
        class: 'tb-fixed-page'
    }
})
export class ProjectConfigComponent implements OnInit {

    private projectKey: string;
    public projectName: string;

    constructor(
        private route: ActivatedRoute,
        private projectConfigService: ProjectConfigService
    ) { }

    ngOnInit() {
        this.route.paramMap.subscribe((params: ParamMap) => {
            this.projectKey = params.get('key');
            this.projectConfigService.getName(this.projectKey).subscribe((name: string)  => {
                this.projectName = name;
            });
        });
    }

}
