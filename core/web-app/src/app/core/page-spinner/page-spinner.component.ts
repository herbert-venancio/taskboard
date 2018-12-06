
import {Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {PageSpinner} from 'app/core/page-spinner/page-spinner';
import {Subscription} from 'rxjs/internal/Subscription';
import {debounce, distinctUntilChanged} from 'rxjs/operators';
import {timer} from 'rxjs/internal/observable/timer';
import {EMPTY} from 'rxjs/internal/observable/empty';

@Component({
    selector: 'tb-page-spinner',
    template: `<div class="tb-spinner"></div>`,
    styleUrls: ['./page-spinner.component.scss']
})
export class PageSpinnerComponent implements OnInit, OnDestroy {
    private subscription: Subscription;
    private isOpen = false;

    constructor(private pageLoader: PageSpinner) {}

    ngOnInit(): void {
        this.subscription = this.pageLoader.isOpen
            .pipe(debounce(open => open ? timer(200) : EMPTY))
            .pipe(distinctUntilChanged())
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
