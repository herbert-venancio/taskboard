import {Component, HostBinding, Input, OnInit} from '@angular/core';
import {TAB_ROUTER_SELECTOR, TabsRouterComponent} from "../tabs-router.component";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {filter} from 'rxjs/operators';

@Component({
    selector: TAB_ROUTER_SELECTOR,
    template: `<span class="tab-name" [title]="name">{{ name }}</span>`
})
export class TabRouterComponent implements OnInit {

    @Input() name: string;
    @Input() routerLink: string;

    @HostBinding('class.active') active: boolean = false;

    constructor(private tabs: TabsRouterComponent,
                private router: Router,
                private activatedRoute: ActivatedRoute) {}

    ngOnInit() {
        this.active = this.routerLink === this.activatedRoute.firstChild.routeConfig.path;

        this.router.events
            .pipe(filter(event => event instanceof NavigationEnd))
            .subscribe(() => {
                this.active = this.routerLink === this.activatedRoute.firstChild.routeConfig.path;
            });
    }

}
