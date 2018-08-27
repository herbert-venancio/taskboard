import {DebugElement} from '@angular/core';

export function mouseDown(element: DebugElement, event: EventInit = { bubbles: true }): void {
    element.nativeElement.dispatchEvent(new MouseEvent('mousedown', event));
}

export enum MouseButtonCode {
    left = 0,
    right = 2
}
export function click(element: DebugElement, event: MouseEventInit): void {
    element.nativeElement.dispatchEvent(new MouseEvent('click', event));
}
export function leftClick(element: DebugElement): void {
    click(element, { button: MouseButtonCode.left });
}
