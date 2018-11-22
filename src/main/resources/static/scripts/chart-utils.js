class ChartUtils {
    static getDefaultHighchartsOptions () {
        const options = {
            colors: ['#53B873', '#E72915', '#FFCF0F', '#EE3771', '#9DCB6A', '#FA6A01', '#AA5DBC', '#5DAFFF', '#FF8C80', '#FCE196', '#B5E5FB', '#CD95D7', '#FFB25D', '#F290B1'],
            chart: {
                backgroundColor: 'transparent',
                zoomType: 'xy'
            },
            exporting: {
                enabled: true
            },
            legend: {
                align: 'left',
                itemHiddenStyle: {
                    color: '#666'
                },
                itemHoverStyle: {
                    color: '#8E8E8E'
                },
                itemStyle: {
                    color: '#8E8E8E'
                },
                layout: 'vertical',
                reversed: true,
                verticalAlign: 'top'
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            title: {
                style: {
                    color: '#8E8E8E'
                }
            },
            tooltip: {
                footerFormat: '<span style="font-style: italic; font-weight: bold; font-size: 1.1em;">Total: {point.total:.2f}</span>',
                shared: true,
                reversed: true
            },
            xAxis: {
                labels: {
                    style: {
                        color: '#8E8E8E'
                    }
                },
                lineColor: '#8E8E8E',
                startOfWeek: 0,
                tickColor: '#8E8E8E',
                type: 'datetime'
            },
            yAxis: {
                gridLineColor: '#CCC',
                labels: {
                    style: {
                        color: '#8E8E8E'
                    }
                },
                reversedStacks: false,
                title: {
                    style: {
                        color: '#8E8E8E'
                    },
                    text: undefined
                }
            },
            navigation: {
                buttonOptions: {
                    enabled: false
                }
            }
        };
        return options;
    }

    static resizeAllHighchartsCharts () {
        /*
         * This function performs a resize on all Highchart charts.
         * 
         * It is necessary because when the chart is first loaded, the container
         * (div) has no size set, then the chart's size falls back to the lib's
         * default size which gives an erroneous size.
         */
        Highcharts.charts.forEach((chart) => {
            if (chart) {
                const chartDiv = chart.container;
                // section is the first ancestral that has the correct height
                const section = chartDiv.closest('section');
                const header = section.children[0];
                const borderSizeWidgetBody = 16;
                const height = section.offsetHeight - header.offsetHeight - borderSizeWidgetBody * 2;
                const width = section.offsetWidth - borderSizeWidgetBody * 2;
                chart.setSize(width, height);
            }
        });
    }

    static scheduleResize (afterMillis) {
        if (!ChartUtils.timeout) {
            ChartUtils.timeout = setTimeout(() => {
                ChartUtils.resizeAllHighchartsCharts();
                delete ChartUtils.timeout;
            }, afterMillis);
        }
    }


    static truncateDate (dateInMillis) {
        const date = new Date(dateInMillis);
        date.setHours(0, 0, 0, 0);
        return date.getTime();
    }

    static ceilDate (dateInMillis) {
        const date = new Date(dateInMillis);
        if (date.getHours() + date.getMinutes() + date.getSeconds() + date.getMilliseconds() === 0) {
            return date.getTime();
        }
        date.setDate(date.getDate() + 1);
        return ChartUtils.truncateDate(date.getTime());
    }
}

class ChartBuilderBase {
    constructor (divID){
        this.options = ChartUtils.getDefaultHighchartsOptions();
        this.options.chart.renderTo = divID;
        this.options.credits = false;
        this.options.chart.events = {
                selection: function (event) {
                    if (event.resetSelection) {
                        this.resetZoomButton = this.resetZoomButton.destroy(); // hide reset button
                        event.preventDefault(); // prevent zoom out
                        this.yAxis[0].setExtremes(null, null); // reset Y
                        dcDateRangeChartsService.applySelection(this.title.textStr); // reset X to timeline  selection
                    }
                }
        };
    }

    withTitle (title) {
        this.options.title.text = title;
        return this;
    }

    withChartType(chartType) {
        this.options.chart.type = chartType;
        return this;
    }

    withTooltipNumberOfDecimals (numberOfDecimals) {
        this.options.tooltip.valueDecimals = numberOfDecimals;
        return this;
    }

    withSeriesData (seriesData) {
        Highcharts.merge(true, this.options, {series: seriesData});
        return this;
    }

    build () {
        const chart = Highcharts.chart(this.options);
        return chart;
    }
}

class WeeklyChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        
    	super(divID);
        const oneWeekInMillis = 7 * 24 * 3600 * 1000;
        this.options.xAxis.tickInterval = oneWeekInMillis;
        this.options.xAxis.labels = {
            formatter: function () {
                return Highcharts.dateFormat('%e %b %y', this.value);
            }
        };
        
        this.options.tooltip.xDateFormat = 'Week from %A, %b %e, %Y';
    }
}

class ProgressChartBuilder extends ChartBuilderBase {
    constructor(divID, startDate, endDate){

        super(divID);		
        this.options.yAxis.max = 100;
        this.options.yAxis.labels.format = '{value}%';
        this.options.yAxis.title.text = 'Progress %';
        this.options.tooltip.headerFormat = null;
        this.options.tooltip.footerFormat = null;
        this.options.tooltip.pointFormat = '<b>{point.x:%d/%m/%Y}: {point.y:.1f}%</b>';
        this.options.plotOptions = {
                spline : {
                    pointStart: startDate,
                    pointEnd: endDate
                }
        }
    }
}

class CFDChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        super(divID)
        Highcharts.merge(true, this.options, this._plotOptions);
        Highcharts.merge(true, this.options, this._tooltipOptions);
        Highcharts.merge(true, this.options, this._xAxisOptions);
        Highcharts.merge(true, this.options, this._yAxisOptions);
    }

    get _plotOptions () {
        return {
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            }
        }
    }

    get _tooltipOptions () {
        return {
            xDateFormat: '%b %e, %Y'
        };
    }

    get _xAxisOptions () {
        return {
            xAxis: {
                tickInterval: 24 * 3600 * 1000,
                events: this._xAxisEvents,
                labels: this._xAxisLabels
            }
        };
    }

    get _xAxisEvents () {
        return {
            afterSetExtremes: (event) => {
                const xMin = ChartUtils.truncateDate(event.min);
                const xMax = ChartUtils.ceilDate(event.max);
                if (xMin === null || xMax === null) {
                    return;
                }
                const baseStackSeries = event.target.series[0];
                const xMinPoint = baseStackSeries.points.find((p) => p.x === xMin)
                const xMaxPoint = baseStackSeries.points.find((p) => p.x === xMax)
                if (xMinPoint === undefined || xMaxPoint === undefined){
                	return;                	
                }
                const yMin = xMinPoint.y;
                const yMax = xMaxPoint.stackTotal;
                const yAxis = event.target.chart.axes[1];
                yAxis.setExtremes(yMin, yMax);
            }
        };
    }

    get _xAxisLabels () {
        return {
            formatter: function () {
                return Highcharts.dateFormat('%e %b %y', this.value);
            }
        };
    }

    get _yAxisOptions () {
        return {
            yAxis: {
                endOnTick: false,
                startOnTick: false
            }
        };
    }
}
