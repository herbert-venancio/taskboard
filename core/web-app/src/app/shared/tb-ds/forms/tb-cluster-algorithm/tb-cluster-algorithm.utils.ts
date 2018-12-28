import * as moment from 'moment';

export function buildClusterToolbarSubtitle(startDate: moment.Moment, endDate: moment.Moment): string {
    const format = moment.localeData().longDateFormat('L');
    const start = startDate ? startDate.format(format) : '';
    const end   = endDate   ? endDate.format(format) : '';
    if (start && end)
        return `(between ${start} and ${end})`;
    if (start)
        return `(after ${start})`;
    if (end)
        return `(before ${end})`;
    return '';
}
