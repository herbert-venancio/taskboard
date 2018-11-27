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
import { NgForm } from '@angular/forms';

import { ClusterItemDto } from './cluster-item-dto.model';
import { ClusterItemDtoGroup } from './cluster-item-dto-group.model';
import { ExpansionPanelComponent } from 'app/shared/obj-ds/expansion-panel/expansion-panel.component';

@Component({
    selector: 'tb-cluster',
    exportAs: 'tb-cluster',
    templateUrl: './tb-cluster.component.html',
    styleUrls: ['./tb-cluster.component.scss']
})
export class TbClusterComponent implements OnChanges {
    @Input() clusterItems: ClusterItemDto[] = [];

    @Output() saveEvent = new EventEmitter();
    @Output() errorEvent = new EventEmitter();

    groupedItems: ClusterItemDtoGroup[] = [];

    @ViewChild('tbClusterForm') tbClusterForm: NgForm;
    @ViewChildren(ExpansionPanelComponent) expansionPanels: QueryList<ExpansionPanelComponent>;
    @ViewChildren('sizingInput') inputs: QueryList<ElementRef>;

    constructor() {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.clusterItems)
            this.groupClusterItemsByIssueType(changes.clusterItems.currentValue);
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

    sendSaveEvent(): void {
        if (this.tbClusterForm.invalid) {
            this.expandFirstErrorPanel();
            this.focusOnFirstError();
            this.sendErrorEvent();
            return;
        }

        const clusterItemsOutput: ClusterItemDto[] = [];
        this.groupedItems.forEach(group => {
            group.items.forEach(s => {
                const itemUpdated = new ClusterItemDto(
                    s.cycle,
                    s.effort,
                    s.fromBaseCluster,
                    s.issueType,
                    s.projectKey,
                    s.sizing
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

    private groupClusterItemsByIssueType(clusterItems: ClusterItemDto[]): void {
        clusterItems.forEach(item => {
                const issueTypeGroup = this.getIssueTypeGroup(item);
                this.setItemOnGroup(issueTypeGroup, item);
            }
        );
    }

    private getIssueTypeGroup(item: ClusterItemDto): ClusterItemDtoGroup {
        let issueTypeFound = this.groupedItems.find(i => i.issueType === item.issueType);

        if (!issueTypeFound) {
            issueTypeFound = new ClusterItemDtoGroup(item.issueType);
            this.groupedItems.push(issueTypeFound);
        }
        return issueTypeFound;
    }

    private setItemOnGroup(issueTypeGroup: ClusterItemDtoGroup, item: ClusterItemDto): void {
        const sizingFound = issueTypeGroup.items.find(s => s.sizing === item.sizing);
        if (!sizingFound) {
            issueTypeGroup.items.push(item);
            return;
        }
        sizingFound.effort = item.effort;
        sizingFound.cycle = item.cycle;
        sizingFound.fromBaseCluster = item.fromBaseCluster;
    }
}
