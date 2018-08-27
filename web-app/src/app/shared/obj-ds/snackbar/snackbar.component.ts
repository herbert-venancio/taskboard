import {Component, Input} from '@angular/core';
import {InformativeSnackbar, SnackbarControl, SnackbarLevel} from './snackbar-control';

@Component({
    selector: 'obj-snackbar',
    templateUrl: './snackbar.component.html',
    styleUrls: ['./snackbar.component.scss']
})
export class SnackbarComponent {
    items: SnackbarItem[] = [];
    levels = SnackbarLevel;

    @Input()
    set control(value: SnackbarControl) {
        value.showInfo = (s: InformativeSnackbar) => this.showInfo(s);
    }

    private showInfo(snackbar: InformativeSnackbar): void {
        const item = new SnackbarItem(
            snackbar.title,
            snackbar.description,
            snackbar.level);

        item.closeTimeoutId = setTimeout(() => this.close(item), 5000);

        this.items.unshift(item);
    }

    close(item: SnackbarItem) {
        clearTimeout(item.closeTimeoutId);

        const index = this.items.indexOf(item);
        this.items.splice(index, 1);
    }

    isArray(item: any) {
        return Array.isArray(item);
    }
}

class SnackbarItem {
    closeTimeoutId: number;

    constructor(
        readonly title: string,
        readonly description: string | Array<string>,
        readonly level: SnackbarLevel) {}
}
