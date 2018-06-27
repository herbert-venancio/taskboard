import {DOCUMENT} from '@angular/common';
import {Inject, Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class LegacyAppRouter {

    constructor(@Inject(DOCUMENT) private document: Document) {}

    goToHome() {
        this.document.location.href = '/';
    }

    goToProjectConfiguration(projectKey: string) {
        this.document.location.href = `/#/project/${projectKey}/config`;
    }
}
