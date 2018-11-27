import {HostBinding, OnDestroy} from '@angular/core';
import {BehaviorSubject} from 'rxjs/internal/BehaviorSubject';
import {NgControl, NgForm, ValidationErrors} from '@angular/forms';
import {FormInput} from './form-input';
import {Subscription} from 'rxjs/internal/Subscription';

export abstract class AbstractFormInput implements FormInput, OnDestroy {
    private ngSubmitSubscription: Subscription;
    private errorsSubject = new BehaviorSubject<ValidationErrors | null>(null);

    readonly errors = this.errorsSubject.asObservable();

    protected constructor(protected control: NgControl, protected form: NgForm, componentName: string) {
        this.checkComponentConstraints(componentName);
        this.ngSubmitSubscription = form.ngSubmit.subscribe(() => this.updateErrors());
    }

    private checkComponentConstraints(componentName: string) {
        if (!this.control) {
            throw new Error(componentName + ' should be used in an input configured with the "ngModel" directive');
        }

        if (!this.form) {
            throw new Error(componentName + ' should be used inside a form');
        }
    }

    private updateErrors() {
        const errors = this.control.invalid && this.form.submitted ? this.control.errors : null;
        this.errorsSubject.next(errors);
    }

    @HostBinding('class.invalid')
    get hasErrors(): boolean {
        return this.errorsSubject.getValue() != null;
    }

    ngOnDestroy(): void {
        this.errorsSubject.complete();
        this.ngSubmitSubscription.unsubscribe();
    }
}
