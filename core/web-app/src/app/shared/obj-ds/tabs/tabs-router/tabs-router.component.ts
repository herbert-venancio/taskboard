import {Component} from '@angular/core';

export const TAB_ROUTER_SELECTOR: string = 'tb-tab-router';

@Component({
    selector: 'tb-tabs-router',
    templateUrl: './tabs-router.component.html',
    styleUrls: ['./tabs-router.component.scss']
})
export class TabsRouterComponent {

    public tabRouterSelector = TAB_ROUTER_SELECTOR;

    constructor() { }

}
