import { Component, Input } from "@angular/core";
import { DataTableTopComponent } from "app/shared/obj-ds/data-table/top/data-table-top.component";

@Component({
    selector: 'tb-toolbar',
    templateUrl: './tb-toolbar.component.html',
    styleUrls: [ './tb-toolbar.component.scss' ]
})
export class TbToolbarComponent {

    @Input() title: string;
    @Input() subtitle: string;
    
    get hasSubtitle() {
        return this.subtitle && this.subtitle.length > 0;
    }
}