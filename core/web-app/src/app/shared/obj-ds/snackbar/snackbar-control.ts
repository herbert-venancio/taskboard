
export class SnackbarControl {
    showInfo(snackbar: InformativeSnackbar): void {}
}

export interface InformativeSnackbar {
    title: string;
    description?: string;
    level?: SnackbarLevel;
}

export enum SnackbarLevel {
    Success,
    Normal,
    Error
}
