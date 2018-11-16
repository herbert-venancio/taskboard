import {
    Component,
    Input,
    OnChanges,
    SimpleChanges,
    Output,
    EventEmitter,
    ViewChild,
    ViewChildren,
    QueryList,
    ElementRef
} from '@angular/core';
import { NgForm, FormGroup } from '@angular/forms';

import { ClusterItemDto } from './cluster-item-dto.model';
import { ClusterItemDtoGroup, ClusterItemChangeDto } from './cluster-item-dto-group.model';
import { ExpansionPanelComponent } from 'app/shared/obj-ds/expansion-panel/expansion-panel.component';
import * as _ from 'underscore';

@Component({
    selector: 'tb-cluster',
    exportAs: 'tb-cluster',
    templateUrl: './tb-cluster.component.html',
    styleUrls: ['./tb-cluster.component.scss']
})
export class TbClusterComponent implements OnChanges {
    @Input() clusterItems: ClusterItemDto[] = [];
    @Input() changesCandidates: ClusterItemDto[] = [];

    @Output() saveEvent = new EventEmitter();
    @Output() errorEvent = new EventEmitter();

    groupedItems: ClusterItemDtoGroup[] = [];

    @ViewChild('tbClusterForm') tbClusterForm: NgForm;
    @ViewChildren(ExpansionPanelComponent) expansionPanels: QueryList<ExpansionPanelComponent>;
    @ViewChildren('sizingInput') inputs: QueryList<ElementRef>;

    constructor() {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.clusterItems || changes.changesCandidates)
            this.groupClusterItemsByIssueType();
    }

    save(): void {
        (this.tbClusterForm as any).submitted = true;
        this.tbClusterForm.ngSubmit.emit();
    }

    markAsPristine(): void {
        this.tbClusterForm.control.markAsPristine();
    }

    isPristine(): boolean {
        return this.tbClusterForm.pristine;
    }

    isIndeterminate(group: ClusterItemDtoGroup): boolean {
        const keys = Object.keys(group.acceptChanges);
        const firstKey = keys[0];
        return !keys.every(key => group.acceptChanges[key] === group.acceptChanges[firstKey]);
    }

    isAllChecked(group: ClusterItemDtoGroup): boolean {
        const keys = Object.keys(group.acceptChanges);
        return keys.every(key => group.acceptChanges[key]);
    }

    updateChecked(group: ClusterItemDtoGroup, ev: Event) {
        const checked = (<HTMLInputElement>ev.target).checked;
        const formGroup = <FormGroup>this.tbClusterForm.controls[group.issueType];
        const checkboxes = [`accept-effort-changes`, `accept-cycle-changes`];
        checkboxes.forEach(key => {
            const control = formGroup.controls[key];
            control.setValue(checked);
            control.markAsDirty();
        });
    }

    sendSaveEvent(): void {
        if (this.tbClusterForm.invalid) {
            this.expandFirstErrorPanel();
            this.focusOnFirstError();
            this.sendErrorEvent();
            return;
        }

        const clusterItemsOutput: ClusterItemDto[] = [];
        this.groupedItems.forEach(group => {
            _.zip(group.items, group.changes).forEach(pair => {
                const item = pair[0];
                const change = pair[1];
                const changeCycle = group.acceptChanges.cycle && change;
                const changeEffort = group.acceptChanges.effort && change;
                const itemUpdated = new ClusterItemDto(
                    changeCycle ? change.cycle : item.cycle,
                    changeEffort ? change.effort : item.effort,
                    item.fromBaseCluster,
                    item.issueType,
                    item.sizing
                );
                clusterItemsOutput.push(itemUpdated);
            });
        });
        this.saveEvent.emit(clusterItemsOutput);
        this.expandAllPanels();
    }

    sendErrorEvent() {
        this.errorEvent.emit('Please review the form');
    }

    private expandAllPanels() {
        this.expansionPanels.forEach(panel => panel.open());
    }

    private expandFirstErrorPanel() {
        this.expansionPanels.forEach(panel => {
            const panelControl = this.tbClusterForm.controls[panel.title];
            if (panelControl && panelControl.invalid)
                panel.open();
        });
    }

    private focusOnFirstError() {
        const firstInvalidInput = this.inputs.find(i => !i.nativeElement.validity.valid);
        if (firstInvalidInput)
            firstInvalidInput.nativeElement.focus();
    }

    private groupClusterItemsByIssueType(): void {
        const clusters = _.groupBy(this.clusterItems, item => item.issueType);
        const changes = _.groupBy(this.changesCandidates.map(item => new ClusterItemChangeDto(item)), item => item.issueType);

        const clusterKeys = new Set(Object.keys(clusters));
        const changeKeys = new Set(Object.keys(changes));
        const allKeys = new Set([...Array.from(clusterKeys), ...Array.from(changeKeys)]);

        // create or update all groups
        allKeys.forEach(issueType => {
            const group = this.getIssueTypeGroup(issueType);
            group.items = clusterKeys.has(issueType) ? clusters[issueType] : [];
            group.changes = changeKeys.has(issueType) ? changes[issueType] : [];
            group.acceptChanges.effort = false;
            group.acceptChanges.cycle = false;
        });

        // remote non-existent groups
        for (let i = this.groupedItems.length - 1; i >= 0; --i) {
            const group = this.groupedItems[i];
            if (!allKeys.has(group.issueType))
                this.groupedItems.splice(i, 1);
        }

        this.sortGroups();
    }

    private getIssueTypeGroup(issueType: string): ClusterItemDtoGroup {
        let issueTypeFound = this.groupedItems.find(i => i.issueType === issueType);

        if (!issueTypeFound) {
            issueTypeFound = new ClusterItemDtoGroup(issueType);
            this.groupedItems.push(issueTypeFound);
        }
        return issueTypeFound;
    }

    private sortGroups() {
        this.groupedItems.sort((a, b) => {
            if (a.issueType < b.issueType)
                return -1;
            if (a.issueType > b.issueType)
                return 1;
            return 0;
        });
    }
}
