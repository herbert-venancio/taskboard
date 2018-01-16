function DcUtils() {

    this.percentFormat = d3.format('.2f');

    this.dateFormat = d3.time.format('%Y-%m-%d');

    this.monthAndYearFormat = d3.time.format('%b %y');

    this.getDefaultLegend = function () {
        return dc.legend().gap(8);
    };

    this.getChartColorsFromArray = function(colorsArray) {
        return d3.scale.ordinal().range(colorsArray);
    };

    this.getDefaultColors = function() {
        return this.getChartColorsFromArray(['#3366cc', '#dc3912', '#ff9900', '#109618', '#990099', '#0099c6', '#dd4477', '#66aa00', '#b82e2e', '#316395', '#994499', '#22aa99', '#aaaa11', '#6633cc', '#e67300', '#8b0707', '#651067', '#329262', '#5574a6', '#3b3eac']);
    };

}
var dcUtils = new DcUtils();
