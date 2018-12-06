import {Component} from "@angular/core";
import {ComponentFixture, TestBed} from "@angular/core/testing";

@Component({ template: '' })
export class MockComponent {
    public vars: any = {};
}

export function createMockFixture(component: Component): Promise<ComponentFixture<MockComponent>> {
    return new Promise((resolve) => {
        TestBed.overrideComponent(MockComponent, { set: component });
        TestBed.compileComponents().then(() => {
            resolve(TestBed.createComponent(MockComponent));
        });
    });
}
