import {Injectable} from '@angular/core';
import {AsyncSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class IconRegistry {
    private icons = new Map<string, AsyncSubject<string>>();

    constructor(private http: HttpClient) {}

    getSvgString(iconName: string): Observable<string> {
        let icon = this.icons.get(iconName);

        if (!icon) {
            icon = this.requestIcon(iconName);
            this.icons.set(iconName, icon);
        }

        return icon;
    }

    private requestIcon(iconName: string): AsyncSubject<string> {
        const icon = new AsyncSubject<string>();
        const url = `/app-static/assets/images/icons/${iconName}.svg`;

        this.http.get(url, {responseType: 'text'}).subscribe(data => {
            icon.next(data);
            icon.complete();
        });

        return icon;
    }
}
