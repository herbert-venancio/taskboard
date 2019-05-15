
import {Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {PageSpinner} from 'app/core/page-spinner/page-spinner';
import {Subscription} from 'rxjs/internal/Subscription';

@Component({
    selector: 'sdb-page-spinner',
    template: `<div class="sdb-spinner"></div>`,
    styleUrls: ['./page-spinner.component.scss']
})
export class PageSpinnerComponent implements OnInit, OnDestroy {
    private subscription: Subscription;
    private isOpen = false;

    constructor(private pageLoader: PageSpinner) {}

    ngOnInit(): void {
        this.subscription = this.pageLoader.isOpen
            .subscribe(open => this.isOpen = open);
    }

    ngOnDestroy(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    @HostBinding('class.open')
    get showSpinner() {
        return this.isOpen;
    }
}
