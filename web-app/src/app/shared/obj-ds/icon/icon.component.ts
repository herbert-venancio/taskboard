import {Component, ElementRef, Input} from '@angular/core';
import {IconRegistry} from './icon-registry';

/**
 * A SVG icon.
 *
 * ### Size Adjustment
 * Use one of the following style classes to adjust the size: `small`, `medium`, `large` or `extra large`.
 * Default size: medium.
 */
@Component({
    selector: 'obj-icon',
    template: '<div class="icon-container"></div>',
    styleUrls: ['./icon.component.scss']
})
export class IconComponent {

    constructor(
        private iconRegistry: IconRegistry,
        private elementRef: ElementRef) {
    }

    /** Name of the file in the SVG icons directory `/assets/images/icons/` without the extension. */
    @Input()
    set iconName(name: string) {
        if (!name) {
            return;
        }

        this.iconRegistry.getSvgString(name).subscribe(svgString => {
            const  hostElement = this.elementRef.nativeElement as HTMLElement;
            const containerElement = hostElement.querySelector('.icon-container');
            containerElement.innerHTML = svgString;

            const svg = containerElement.querySelector('svg');
            svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
            svg.setAttribute('focusable', 'false');
            svg.removeAttribute('height');
            svg.removeAttribute('width');
        });
    }
}
