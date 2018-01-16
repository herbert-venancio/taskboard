function DcUtils() {

    this.percentFormat = d3.format('.2f');

    this.dateFormat = d3.time.format('%Y-%m-%d');

    this.monthAndYearFormat = d3.time.format('%b %y');

    this.getDefaultLegend = function () {
        return dc.legend().gap(8);
    };

    this.rotateDateFormattedXAxis = function(chart) {
        chart.renderlet(function (chart) {
            chart.selectAll('g.x text')
                .attr('transform', 'translate(-10,20) rotate(315)');
        });
    };

    this.rotateMonthAndYearFormattedXAxis = function(chart) {
        chart.renderlet(function (chart) {
            chart.selectAll('g.x text')
                .attr('transform', 'translate(-10,10) rotate(315)');
        });
    };

    this.getChartColorsFromArray = function(colorsArray) {
        return d3.scale.ordinal().range(colorsArray);
    };

    this.getDefaultColors = function() {
        return this.getChartColorsFromArray(['#3366cc', '#dc3912', '#ff9900', '#109618', '#990099', '#0099c6', '#dd4477', '#66aa00', '#b82e2e', '#316395', '#994499', '#22aa99', '#aaaa11', '#6633cc', '#e67300', '#8b0707', '#651067', '#329262', '#5574a6', '#3b3eac']);
    };

    this.colors = {
        DONE: '#2ca02c',
        DONE_PROJECTION: '#ff7f0e',
        BACKLOG: '#909',
        BACKLOG_PROJECTION: '#dc3912'
    };

}
var dcUtils = new DcUtils();