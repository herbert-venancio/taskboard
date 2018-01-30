function DcUtils() {

    this.percentFormat = d3.format('.2f');

    this.dateFormat = d3.time.format('%Y-%m-%d');

    this.monthAndYearFormat = d3.time.format('%b %y');

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

    this.rotateDateFormattedXAxis = function(chart) {
        chart.on('pretransition', function(chart) {
            chart.selectAll('g.x text')
                .attr('transform', 'translate(-10,20) rotate(315)');
        });
    };

    this.rotateMonthAndYearFormattedXAxis = function(chart) {
        chart.on('pretransition', function (chart) {
            chart.selectAll('g.x text')
                .attr('transform', 'translate(-10,10) rotate(315)');
        });
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

}
var dcUtils = new DcUtils();
