import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'objFilter'
})
export class ObjFilterPipe implements PipeTransform {

    transform(items: any[], filterParam: string | ObjFilterOptions): any {
        const filterOptions = ObjFilterOptions.of(filterParam);

        if (!ObjFilterPipe.hasToFilter(items, filterOptions))
            return items;

        return ObjFilterPipe.filter(items, filterOptions);
    }

    private static hasToFilter(items: any[], filterOptions: ObjFilterOptions): boolean {
        return !(!items || items.length === 0 || filterOptions.searchFor === '');
    }

    private static filter(items: any[], filterOptions: ObjFilterOptions): any[] {
        const valueToTest: string = this.getValueWithOptionsApplied(filterOptions.searchFor, filterOptions);

        return items.filter(item => {
            let valueToBeTested = this.getValueToBeTested(item, filterOptions);
            valueToBeTested = this.getValueWithOptionsApplied(valueToBeTested, filterOptions);
            return valueToBeTested.includes(valueToTest);
        });
    }

    private static getValueWithOptionsApplied(value: string, filter: ObjFilterOptions): string {
        let valueWithOptions = value;

        if (filter.normalize)
            valueWithOptions = this.normalizeString(valueWithOptions);

        if (!filter.caseSensitize)
            valueWithOptions = valueWithOptions.toLowerCase();

        return valueWithOptions;
    }

    private static getValueToBeTested(value: any, filterOptions: ObjFilterOptions): string {
        if (filterOptions.key)
            return this.getValueAsString(value[filterOptions.key]);
        else if (filterOptions.keys)
            return filterOptions.keys
                .map(key => this.getValueAsString(value[key]))
                .join('');
        else
            return this.getValueAsString(value);
    }

    private static getValueAsString(value: any) {
        return Array.isArray(value) ? value.join('') : value;
    }

    private static normalizeString(word: string): string {
        if (word === null || word === undefined)
            return '';

        const accentsIn  = 'ÀÁÂÃÄÅĄàáâãäåąßÒÓÔÕÕÖØÓòóôõöøóÈÉÊËĘèéêëęðÇĆçćÐÌÍÎÏìíîïÙÚÛÜùúûüÑŃñńŠŚšśŸÿýŽŻŹžżź';
        const accentsOut = 'AAAAAAAaaaaaaaBOOOOOOOOoooooooEEEEEeeeeeeCCccDIIIIiiiiUUUUuuuuNNnnSSssYyyZZZzzz';

        return word
            .split('')
            .map( letter => {
                const i = accentsIn.indexOf(letter);
                return (i !== -1) ? accentsOut[i] : letter;
            })
            .join('');
    }

}

export class ObjFilterOptions {
    key: string;
    keys: string[];
    searchFor: string = '';
    normalize: boolean = true;
    caseSensitize: boolean = false;

    public static of(filter: string | ObjFilterOptions): ObjFilterOptions {
        let filterObj = new ObjFilterOptions();

        if (typeof filter === 'string')
            filterObj.searchFor = filter;
        else
            filterObj = Object.assign(filterObj, filter);

        this.validate(filterObj);

        return filterObj;
    }

    private static validate(filterOptions: ObjFilterOptions): void {
        if (filterOptions.key !== undefined && filterOptions.keys !== undefined)
            throw 'Define both "key" and "keys" at the same time on ObjFilterOptions isn\'t allowed.';
    }

}
