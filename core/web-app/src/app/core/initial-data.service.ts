import {Injectable} from '@angular/core';
import {AsyncSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class InitialDataService {
    private data = new AsyncSubject<InitialDataDto>();
    private loaded = false;

    constructor(private httpClient: HttpClient) {}

    public getData(): Observable<InitialDataDto> {
        return this.data;
    }

    public load(onLoadCallback: () => void): void {
        if (this.loaded) {
            onLoadCallback();
            return;
        }

        this.loaded = true;

        this.httpClient.get<InitialDataDto>('/ws/app/initial-data').subscribe(data => {
            this.data.next(data);
            this.data.complete();
            onLoadCallback();
        });
    }
}

export class InitialDataDto {
    loggedInUser: LoggedInUserDto;
}

export class LoggedInUserDto {
    username: string;
    name: string;
    avatarUrl: string;
    permissions: string[];
}
