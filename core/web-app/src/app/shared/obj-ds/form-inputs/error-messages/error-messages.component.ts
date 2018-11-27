import {AfterContentInit, Component, ContentChildren, HostBinding, Input, OnDestroy, QueryList} from '@angular/core';
import {ErrorMessageComponent} from './error-message.component';
import {Subscription} from 'rxjs/internal/Subscription';
import * as _ from 'underscore';
import {FormInput} from '../form-input';
import {ValidationErrors} from '@angular/forms';

/**
 * Display error messages for a given `input` component (e.g. objTextField).
 * Only the first error message is displayed at time, respecting the declaration order.
 *
 * ### Content
 * The content of this component should be a list of `<obj-error-message>` elements.
 */
@Component({
  selector: 'obj-error-messages',
  template: `<ng-content select="obj-error-message"></ng-content>`,
  host: {
      'class': 'obj-error-message'
  }
})
export class ErrorMessagesComponent implements AfterContentInit, OnDestroy {

    /**
     * The `input` component instance which this component is bound to.
     */
    @Input('for')
    input: FormInput;

    @ContentChildren(ErrorMessageComponent)
    private messages: QueryList<ErrorMessageComponent>;
    private errorsSubscription: Subscription;
    private hasVisibleMessages = false;

    @HostBinding('class.has-messages')
    get hasMessages(): boolean {
        return this.hasVisibleMessages;
    }

    ngAfterContentInit(): void {
        this.errorsSubscription = this.input.errors.subscribe(errors => this.updateMessages(errors));
    }

    private updateMessages(errors: ValidationErrors | null): void {
        this.messages.forEach(m => m.isVisible = false);
        this.hasVisibleMessages = false;

        if (!errors) {
            return;
        }

        const errorCodes = _.keys(errors);
        const firstMatchedMessage = this.messages.find(m => m.match(errorCodes));

        if (firstMatchedMessage) {
            firstMatchedMessage.isVisible = true;
            this.hasVisibleMessages = true;
        }
    }

    ngOnDestroy(): void {
        this.errorsSubscription.unsubscribe();
    }
}
