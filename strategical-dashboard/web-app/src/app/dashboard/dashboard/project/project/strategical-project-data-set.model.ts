export interface StrategicalProjectDataSet {
    projectKey: string;
    projectDisplayName: string;
    monitors: MonitorData[];
}

export interface MonitorData {
    label?: string;
    icon?: string;
    items?: string;
    statusDetails?: string;
    status?: string;
    errors?: string;
}