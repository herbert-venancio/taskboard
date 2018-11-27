export class FormGroupDto {
    dto: any;
    constructor(readonly groupId: number, dto: any) {
        this.dto = dto;
    }
    static getDtos(items: FormGroupDto[]): any[] {
        return items.map(i => i.dto);
    }
}
