export class StrategicalProjectDataSet {
    projectKey: string;
    projectDisplayName: string;
    monitors: MonitorData[];

    public static from(projectControllerData: StrategicalProjectDataSet): StrategicalProjectDataSet {
        const strategicalProjectDataSet = new StrategicalProjectDataSet();
        strategicalProjectDataSet.projectKey = strategicalProjectDataSet.projectKey;
        strategicalProjectDataSet.projectDisplayName = projectControllerData.projectDisplayName;
        return strategicalProjectDataSet;
    }
}

export class MonitorData {
    label?: string;
    icon?: string;
    items?: string;
    statusDetails?: string;
    status?: string;
    errors?: string;
}