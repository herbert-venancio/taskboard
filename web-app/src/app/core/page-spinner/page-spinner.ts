import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs/internal/BehaviorSubject';

@Injectable({
    providedIn: 'root'
})
export class PageSpinner {
    private isOpenSubject = new BehaviorSubject(false);
    readonly isOpen = this.isOpenSubject.asObservable();

    show() {
        this.isOpenSubject.next(true);
    }

    hide() {
        this.isOpenSubject.next(false);
    }
}
