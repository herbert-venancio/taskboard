export class CheckboxDto {
    label: string;
    value: any;
    checked: boolean;

    constructor(label: string, value: any, checked: boolean = false) {
        this.label = label;
        this.value = value;
        this.checked = checked;
    }

    public static fromValues(values: string[]): CheckboxDto[] {
        return values.map(value => new CheckboxDto(value, value));
    }

    public static getSelectedValues(checkboxList: CheckboxDto[]): any[] {
        return checkboxList
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value);
    }
}
