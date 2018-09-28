import {Injectable} from '@angular/core';
import {CanDeactivate} from '@angular/router';
import {ComponentLeaveConfirmation} from './component-leave-confirmation';

@Injectable({
    providedIn: 'root'
})
export class LeaveConfirmationGuard implements CanDeactivate<ComponentLeaveConfirmation> {

    canDeactivate(component: ComponentLeaveConfirmation): boolean {
        if(!component.canDeactivate())
            return confirm('If you leave before saving, your changes will be lost.');

        return true;
    }

}
