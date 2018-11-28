import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'sortBy'
})

export class SortByPipe implements PipeTransform {

    transform(array: Array<any>, sortProperties: any) {

        return array.sort( function(a, b) {
            
            if (a[sortProperties.column] < b[sortProperties.column]) {
                return -1 * sortProperties.direction;
            }
            else if (a[sortProperties.column] > b[sortProperties.column]) {
                return 1 * sortProperties.direction;
            }
            else {
                return 0;
            }
        
        });
    };
}