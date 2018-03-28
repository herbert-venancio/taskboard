function DcUtils() {

    this._DATE_YEAR_SPLIT_WORD = '|';

    this.percentFormat = d3.format('.2f');

    this.dateFormatWithoutYear = d3.time.format('%b %e');

    this.dateFormatWithYear = d3.time.format('%b %e' + this._DATE_YEAR_SPLIT_WORD + '%Y');

    this.getDefaultLegend = function () {
        return dc.legend().gap(8);
    };

    this.defaultPieChartConfiguration = function(chart) {
        chart
            .externalLabels(10)
            .externalRadiusPadding(30)
            .turnOnControls(true)
            .legend(
                this.getDefaultLegend()
            );

        this.orderChartByKey(chart);
    };

    this.orderChartByKey = function(chart) {
        chart.ordering(function(d) { return -d.key });
    };

    this.getDateTicks = function(startDate, endDate, numOfTicks) {
        if (numOfTicks < 2)
            numOfTicks = 2;

        var cols = numOfTicks - 1;

        var oneDayInMilli = 86400000;
        var diffInDays = (endDate - startDate)/oneDayInMilli;
        var betweenTicks = diffInDays / cols;

        var ticks = [];
        for (var i = 0; i < cols; i++) {
            var dateTick = addDaysToDate(startDate, Math.round(betweenTicks * i));
            ticks.push(dateTick);
        }
        ticks.push(endDate);

        return ticks;
    };

    this.getChartColorsFromArray = function(colorsArray) {
        return d3.scale.ordinal().range(colorsArray);
    };

    this.getDefaultColors = function() {
        return this.getChartColorsFromArray([
            '#53B873',
            '#E72915',
            '#FFCF0F',
            '#EE3771',
            '#9DCB6A',
            '#FA6A01',
            '#AA5DBC',

            '#5DAFFF',
            '#FF8C80',
            '#FCE196',
            '#B5E5FB',
            '#CD95D7',
            '#FFB25D',
            '#F290B1'
        ]);
    };

    this.colors = {
        EXPECTED: '#5DAFFF',
        DONE: '#53B873',
        DONE_PROJECTION: '#FA6A01',
        BACKLOG: '#AA5DBC',
        BACKLOG_PROJECTION: '#E72915',

        BASELINE_BACKLOG: '#5DAFFF',
        BASELINE_DONE: '#167ABC',
        INTANGIBLE_BACKLOG: '#EE3771',
        INTANGIBLE_DONE: '#BE175E',
        NEWSCOPE_BACKLOG: '#FFCF0F',
        NEWSCOPE_DONE: '#E2AE33',
        REWORK_BACKLOG: '#FA6A01',
        REWORK_DONE: '#E64E29',
    };

    this.rangesEqual = function(range1, range2) {
        if (!range1 && !range2) {
            return true;
        }
        else if (!range1 || !range2) {
            return false;
        }
        else if (range1.length === 0 && range2.length === 0) {
            return true;
        }
        else if (range1[0].valueOf() === range2[0].valueOf() &&
            range1[1].valueOf() === range2[1].valueOf()) {
            return true;
        }
        return false;
    };

    this.setupChartDateTicks = function(chart, startDate, endDate, tickCount) {
        var self = this;
        var lastYearShown;

        chart.xAxis()
            .tickValues(
                self.getDateTicks(startDate, endDate, tickCount)
            )
            .tickFormat(function (tickDate, tickIndex) {
                if (tickIndex === 0 || lastYearShown !== tickDate.getFullYear()) {
                    lastYearShown = tickDate.getFullYear();
                    return self.dateFormatWithYear(tickDate);
                }
                return self.dateFormatWithoutYear(tickDate);
            });

        self._breakLineOnTicksThatContainsYears(chart);

        chart.visibleTickCount = tickCount;
    };

    this._breakLineOnTicksThatContainsYears = function(chart) {
        var self = this;
        chart.on('pretransition', function(chart) {
            chart.selectAll('g.x text').each(function(d, i) {
                if (this.innerHTML.indexOf(self._DATE_YEAR_SPLIT_WORD) === -1)
                    return;

                var dateArray = this.innerHTML.split(self._DATE_YEAR_SPLIT_WORD);
                var dayMonth = dateArray[0];
                var year = dateArray[1];
                this.innerHTML = dayMonth + '<tspan class="tick-year" x="0" dy="1.2em">' + year + '</tspan>';
            });
        });
    };

}
var dcUtils = new DcUtils();
