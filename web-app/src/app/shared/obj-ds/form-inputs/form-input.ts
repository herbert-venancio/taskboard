import {Observable} from 'rxjs/internal/Observable';
import {ValidationErrors} from '@angular/forms';

export interface FormInput {

    readonly errors: Observable<ValidationErrors | null>;

}
