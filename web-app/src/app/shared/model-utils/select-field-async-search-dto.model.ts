import {concat, Observable, of, Subject} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';

export class SelectFieldAsyncSearchDto {

    items$: Observable<{} | any[]>;
    itemsInput$ = new Subject<string>();
    isLoading: boolean = false;

    constructor(searchFunc: (term: string) => Observable<any[]>, filterFunc?: (value: any) => boolean) {
        this.setupAsyncSearch(searchFunc, filterFunc);
    }

    setupAsyncSearch(searchFunc: Function, filterFunc?: (value?: any) => {}) {
        this.items$ = concat(
            of([]),
            this.itemsInput$.pipe(
                debounceTime(200),
                distinctUntilChanged(),
                tap(() => this.isLoading = true),
                switchMap(term => searchFunc(term).pipe(
                    catchError(() => of([])),
                    map((values: any[]) => {
                        return filterFunc === undefined
                            ? values
                            : values.filter((value: any) => filterFunc(value));
                    }),
                    tap(() => this.isLoading = false)
                ))

            )
        );
    }
}
