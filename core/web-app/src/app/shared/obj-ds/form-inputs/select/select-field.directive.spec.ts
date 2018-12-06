import {NgSelectComponent, NgSelectModule} from '@ng-select/ng-select';
import {FormInputsModule} from '../form-inputs.module';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {leftClick, mouseDown} from '../../../../../testing/event-utils';
import {createMockFixture, MockComponent} from '../../../../../testing/component-utils';
import {NotEmptyDirective} from "../../../form-utils/validators/not-empty.directive";

describe('SelectFieldDirective', () => {

    let fixture: ComponentFixture<MockComponent>;
    let mockComponent: MockComponent;
    let ngSelectEl: DebugElement;
    let ngSelectContainerEl: DebugElement;
    let ngSelect: NgSelectComponent;

    describe('Single value', () => {

        let outside: DebugElement;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ FormInputsModule, FormsModule, NgSelectModule ],
                declarations: [ MockComponent, NotEmptyDirective ]
            });
            createMockFixture({
                template: `
                    <form #testForm="ngForm" (ngSubmit)="vars.save(testForm)">
                        <div class="outside">Outside</div>
                        <ng-select name="testSelect"
                                   [(ngModel)]="vars.selected"
                                   [items]="[ 1, 2, 3 ]"
                                   placeholder="Select a value"
                        ></ng-select>
                    </form>
                `
            }).then(fixtureCreated => {
                setGlobalValues(fixtureCreated);
                mockComponent.vars.save = () => {};
                outside = fixture.debugElement.query(By.css('.outside'));
                fixture.detectChanges();
            });
        }));

        it('when init, the field must be valid valid and closed', () => {
            expectIsOpen(false);
            expectIsValid(true);
        });

        it('clicking outside must close the dropdown', () => {
            mouseDown(ngSelectContainerEl);
            fixture.detectChanges();
            expectIsOpen(true);

            mouseDown(outside);
            fixture.detectChanges();
            expectIsOpen(false);
        });

        it('clicking inside the container when dropdown is opened must close it', () => {
            mouseDown(ngSelectContainerEl);
            fixture.detectChanges();
            expectIsOpen(true);

            mouseDown(ngSelectContainerEl);
            fixture.detectChanges();
            expectIsOpen(false);
        });

    });

    describe('tbNotEmpty', () => {

        let submitBt: DebugElement;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [ FormInputsModule, FormsModule, NgSelectModule ],
                declarations: [ MockComponent, NotEmptyDirective ]
            });
            createMockFixture({
                template: `
                    <form #testForm="ngForm" (ngSubmit)="vars.save(testForm)">
                        <ng-select name="testSelect"
                                   [(ngModel)]="vars.selected"
                                   [items]="[ 1, 2, 3 ]"
                                   placeholder="Select a value"
                                   tbNotEmpty
                        ></ng-select>
                        <button type="submit">Save</button>
                    </form>
                `
            }).then(fixtureCreated => {
                setGlobalValues(fixtureCreated);
                mockComponent.vars.save = () => {};
                submitBt = fixture.debugElement.query(By.css('button[type="submit"]'));
                fixture.detectChanges();
            });
        }));

        it('init as valid', () => {
            expectIsValid(true);
        });

        it('submitting without a selected value must be invalid', () => {
            leftClick(submitBt);
            fixture.detectChanges();
            expectIsValid(false);
        });

    });

    const expectIsOpen = (isOpen: boolean): void => {
        expect(ngSelect.isOpen).toBe(isOpen);
        expect(ngSelectEl.classes['ng-select-opened']).toBe(isOpen);
    };

    const expectIsValid = (isValid: boolean): void => {
        expect(ngSelectEl.classes['ng-valid']).toBe(isValid);
        expect(ngSelectEl.classes['ng-invalid']).toBe(!isValid);
    };

    const setGlobalValues = (fixtureCreated: ComponentFixture<MockComponent>): void => {
        fixture = fixtureCreated;
        fixture.detectChanges();

        mockComponent = fixture.componentInstance;

        ngSelectEl = fixture.debugElement.query(By.css('ng-select'));
        ngSelectContainerEl = ngSelectEl.query(By.css('.ng-select-container'));
        ngSelect = ngSelectEl.componentInstance;
    }
});
