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
                    color: '#8E8E8E',
                    fontWeight: 'normal'
                },
                layout: 'vertical',
                reversed: true,
                verticalAlign: 'top',
                itemMarginBottom: 4
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            title: {
                style: {
                    color: '#8E8E8E'
                },
                text: undefined
            },
            tooltip: {
                borderWidth: 0,
                shared: true,
                style: {
                    fontFamily: '\'ptsans-regular\', Verdana, Arial, Helvetica, sans-serif'
                },
                reversed: true,
                useHTML: true,
                valueSuffix: ''
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
                tickInterval: null,
                tickPixelInterval: 100
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

    static extractUniqueValuesOfPropertyFromArray (data, propertyName) {
        return _.chain(data)
            .map((item) => item[propertyName])
            .unique()
            .value();
    }

    static groupChartDataByGroupKey (crossfilterGroup, groupPropertyNameKey, valuePropertyNameKey) {
        const reduceAdd = (accumulator, current) => {
            const groupingKey = current[groupPropertyNameKey];
            if (accumulator[groupingKey] === undefined) {
                accumulator[groupingKey] = 0;
            }
            accumulator[groupingKey] += current[valuePropertyNameKey];
            return accumulator;
        };
        const reduceSub = (accumulator, current) => {
            const groupingKey = current[groupPropertyNameKey];
            accumulator[groupingKey] -= current[valuePropertyNameKey];
            return accumulator;
        };
        const reduceInit = () => {
            return {};
        };
        crossfilterGroup.reduce(reduceAdd, reduceSub, reduceInit);
    }

    static registerOptions (widget) {
        const options = [
            {
                icon:'taskboard-icons:dashboard-filter',
                title: 'Filters',
                tap: () => widget.$$('.filters-modal').open(),
                hidden: true
            },
            {
                icon: 'taskboard-icons:settings',
                title: 'Settings',
                tap: () => {
                    widget.settingIssueLevel = widget._getSavedLevel();
                    widget.$$('.settings-modal').open();
                },
                cssClasses: ''
            }
        ];
        widget.options = options;
    }
}

class ChartBuilderBase {
    constructor (divID) {
        this.options = ChartUtils.getDefaultHighchartsOptions();
        this.options.chart.renderTo = divID;
        this.options.credits = false;
        this.hasPlotLine = false;
        this.options.chart.events = {
            selection: function (event) {
                const hideResetButton = () => {
                    this.resetZoomButton = this.resetZoomButton.destroy();
                };
                const preventZoomOut = (zoomOutEvent) => {
                    zoomOutEvent.preventDefault();
                };
                const zoomOutYAxis = () => {
                    this.yAxis[0].setExtremes(null, null);
                };
                const zoomOutXAxisToTimelineExtent = () => {
                    dcDateRangeChartsService.applySelection(this);
                };
                if (event.resetSelection) {
                    hideResetButton();
                    preventZoomOut(event);
                    zoomOutYAxis();
                    zoomOutXAxisToTimelineExtent();
                }
            }
        };
        this._tooltipHeader = {
            title: {
                text: '{point.key}',
                bold: false,
            },
            subTitle: {
                text: '',
                bold: false
            }
        };
        this._tooltipFooter = {
            label: {
                text: 'Total:',
                bold: false,
            },
            value: {
                text: '{point.total:.2f}'
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

    withTooltipHeaderTitle (title) {
        Highcharts.merge(true, this._tooltipHeader, {
            title: {
                text: title
            }
        });
        return this;
    }

    withTooltipHeaderTitleBold () {
        Highcharts.merge(true, this._tooltipHeader, {
            title: {
                bold: true
            }
        });
        return this;
    }

    withTooltipHeaderSubTitle (subTitle) {
        Highcharts.merge(true, this._tooltipHeader, {
            subTitle: {
                text: subTitle
            }
        });
        return this;
    }

    withTooltipHeaderSubTitleBold () {
        Highcharts.merge(true, this._tooltipHeader, {
            subTitle: {
                bold: true
            }
        });
        return this;
    }

    withTooltipFooterLabel(label) {
        Highcharts.merge(true, this._tooltipFooter, {
            label: {
                text: label
            }
        });
        return this;
    }

    withTooltipFooterLabelBold() {
        Highcharts.merge(true, this._tooltipFooter, {
            label: {
                bold: true
            }
        });
        return this;
    }

    withTooltipFooterValue(value) {
        Highcharts.merge(true, this._tooltipFooter, {
            value: {
                text: value
            }
        });
        return this;
    }

    withSeriesData (seriesData) {
        Highcharts.merge(true, this.options, {series: seriesData});
        return this;
    }

    withPlotLineAt (date) {
        this.plotLineDate = date;
        this.hasPlotLine = true;
        return this;
    }

    withColors (colors) {
        this.options.colors = colors;
        return this;
    }

    build () {
        this._buildTooltipHeaderFormat();
        this._buildTooltipFooterFormat();
        const chart = Highcharts.chart(this.options);
        if (this.hasPlotLine) {
            chart.xAxis[0].addPlotLine({
                value: this.plotLineDate,
                color: 'grey',
                width: 2,
                id: 'projectionStart'});
        }
        return chart;
    }

    _buildTooltipHeaderFormat () {
        const tooltip = this.options.tooltip;
        if (tooltip.headerFormat === null) {
            return;
        }
        if (tooltip.headerFormat === undefined) {
            tooltip.headerFormat = `<div class="highcharts-tooltip-header">
                <div class="highcharts-tooltip-header__title${this._tooltipHeader.title.bold ? ' highcharts-tooltip--text-bold' : ''}">${this._tooltipHeader.title.text}</div>
                <div class="highcharts-tooltip-header__subtitle${this._tooltipHeader.title.bold ? ' highcharts-tooltip--text-bold' : ''}">${this._tooltipHeader.subTitle.text}</div>
            </div>`;
        }
    }

    _buildTooltipFooterFormat () {
        const tooltip = this.options.tooltip;
        if (tooltip.footerFormat === null) {
            return;
        }
        if (tooltip.footerFormat === undefined) {
            tooltip.footerFormat = `<div class="highcharts-tooltip-item">
            <div class="highcharts-tooltip-item__label${this._tooltipFooter.label.bold ? ' highcharts-tooltip--text-bold' : ''}">${this._tooltipFooter.label.text}</div>
            <div class="highcharts-tooltip-item__value highcharts-tooltip--text-bold">${this._tooltipFooter.value.text} ${tooltip.valueSuffix}</div>
            </div>`;
        }
    }
}

class PieChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        super(divID);
        this.options.chart.plotBorderWidth = null;
        this.options.chart.plotShadow = false;
        this.options.tooltip.reversed = false;
        this.options.plotOptions.pie = {
            allowPointSelect: true,
            cursor: 'pointer',
            showInLegend: true,
            dataLabels: {
                enabled: true,
                formatter: function(){
                    if (this.y > 0){
                        return `${this.point.percentage.toFixed(2)} %`;
                    }
                }
            }
        };
    }
}

class WeeklyChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        super(divID);
        Highcharts.merge(true, this.options, {
            tooltip: {
                xDateFormat: 'Week from %A, %b %e, %Y'
            }
        });
        Highcharts.merge(true, this.options, {
            xAxis: {
                type: 'datetime',
                labels: {
                    format: '{value:%e %b %y}'
                }
            }
        });
    }
}

class ProgressChartBuilder extends ChartBuilderBase {
    constructor(divID, startDate, endDate){

        super(divID);
        this.options.yAxis.max = 100;
        this.options.yAxis.labels.format = '{value}%';
        this.options.yAxis.title.text = 'Progress %';
        this.options.xAxis.type = 'datetime';
        this.options.tooltip.headerFormat = null;
        this.options.tooltip.footerFormat = null;
        this.options.tooltip.pointFormat = '<b>{point.x:%d/%m/%Y}: {point.y:.1f}%</b>';
        this.options.plotOptions = {
            spline : {
                pointStart: startDate,
                pointEnd: endDate
            }
        };
    }
}

class CFDChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        super(divID);
        this.options.xAxis.type = 'datetime';
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
        };
    }

    get _tooltipOptions () {
        return {
            tooltip: {
                xDateFormat: '%b %e, %Y'
            }
        };
    }

    get _xAxisOptions () {
        return {
            xAxis: {
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
                const xMinPoint = baseStackSeries.points.find((point) => point.x === xMin);
                const xMaxPoint = baseStackSeries.points.find((point) => point.x === xMax);
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
            format: '{value:%e %b %y}'
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

class TouchTimeByIssuesChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        super(divID);
        Highcharts.merge(true, this.options, this._chartOptions);
        Highcharts.merge(true, this.options, this._tooltipOptions);
        Highcharts.merge(true, this.options, this._xAxisOptions);
        this._filterCallback = undefined;
    }

    withCategories (categories) {
        this.options.xAxis.categories = categories;
        return this;
    }

    get _chartOptions () {
        return {
            chart: {
                events: {
                    selection: undefined
                },
                zoomType: 'xy'
            }
        };
    }

    get _tooltipOptions () {
        return {
            tooltip: {
                valueSuffix: ' (h)'
            }
        };
    }

    get _xAxisOptions () {
        return {
            xAxis: {
                type: 'category'
            }
        };
    }

    get _yAxisOptions () {
        return {
            yAxis: {
                type: 'category',
                title: {
                    text: 'Effort (h)'
                }
            }
        };
    }

    withTimelineFilterCallback (callback) {
        this._filterCallback = callback;
        return this;
    }

    build () {
        const highchartChart = super.build();
        if (this._filterCallback === undefined) {
            throw new Error('FilterCallback must be defined');
        }
        return new HighchartChartWrapper(highchartChart, this._filterCallback);
    }
}

class TouchTimeByWeekChartBuilder extends WeeklyChartBuilder {
    constructor (divID) {
        super(divID);
        Highcharts.merge(true, this.options, {
            tooltip: {
                valueSuffix: ' (h)'
            }
        });
        Highcharts.merge(true, this.options, {
            yAxis: {
                title: {
                    text: 'Effort (h)'
                }
            }
        });
    }
}

class HighchartChartWrapper {
    constructor (highchartChart, filterCallback) {
        this.highchartChart = highchartChart;
        this.chartKey = highchartChart.options.chart.renderTo;
        this._filterCallback = filterCallback;
    }

    notifyTimelineChanged () {
        this._filterCallback();
    }

    destroy () {
        this.highchartChart.destroy();
    }
}

class BudgetChartBuilder extends ChartBuilderBase {
    constructor(divID, startDate, endDate, projectionDate){

        super(divID);
        this.options.title.text = 'Projection date: ' + ((projectionDate !== null ) ? projectionDate : '...');
        this.options.title.style = {'color' : '#8E8E8E','cursor': 'default', 'fontSize': '14px'};
        this.options.title.align = 'left';
        this.options.title.verticalAlign = 'bottom';
        this.options.xAxis.labels.format = '{value:%e %b %y}';
        this.options.yAxis.labels.format = '{value}h';
        this.options.yAxis.title.text = 'Hours';
        this.options.tooltip.headerFormat = null;
        this.options.tooltip.footerFormat = null;
        this.options.tooltip.pointFormat = '<b>{point.x:%d/%m/%Y}: {point.y:.1f}h</b>';
        this.options.tooltip.shared = false;
        this.options.plotOptions = {
            spline : {
                pointStart: startDate,
                pointEnd: endDate
            }
        };
    }
}

class CycleTimeChartBuilder extends ChartBuilderBase {
    constructor (divID) {
        super(divID);
        const HOUR_IN_MILLIS = 60 * 60 * 1000;
        Highcharts.merge(true, this.options, {
            xAxis: {
                labels: {
                    format: '{value:%e %b %y}'
                },
                type: 'datetime',
            }
        });
        Highcharts.merge(true, this.options, {
            yAxis: {
                title: {
                    text: 'Days'
                }
            }
        });
        Highcharts.merge(true, this.options, {
            plotOptions: {
                series: {
                    stacking: null,
                    turboThreshold: 10000
                },
                scatter: {
                    jitter: {
                        x: 4 * HOUR_IN_MILLIS,
                        y: 0.4
                    }
                }
            }
        });
        Highcharts.merge(true, this.options, {
            tooltip: {
                borderWidth: 0,
                pointFormatter: function () {
                    const cycleHtml = [];
                    Object.entries(this.extraData.subCycles).forEach(([status, cycle]) => {
                        if (cycle > 0) {
                            cycleHtml.push('<div class="highcharts-tooltip-item">');
                            cycleHtml.push(`<div class="highcharts-tooltip-item__label highcharts-tooltip-subcycle-status highcharts-tooltip-subcycle-status--bgcolor-red">${status}</div>`);
                            cycleHtml.push(`<div class="highcharts-tooltip-item__value">${cycle} days</div>`);
                            cycleHtml.push('</div>');
                        }
                    });
                    cycleHtml.push('<div class="highcharts-tooltip-item">');
                    cycleHtml.push('<div class="highcharts-tooltip-item__label">Conclusion Date:</div>');
                    cycleHtml.push(`<div class="highcharts-tooltip-item__value highcharts-tooltip--text-bold">${this.extraData.exitDate.toDateString()}</div>`);
                    cycleHtml.push('</div>');
                    return cycleHtml.join('\n');
                },
                footerFormat: `<div class="highcharts-tooltip-item">
                    <div class="highcharts-tooltip-item__label">Total Cycle Time:</div>
                    <div class="highcharts-tooltip-item__value highcharts-tooltip--text-bold">{point.y} days</div>
                </dev>`,
                reversed: false,
                useHTML: true,
                xDateFormat: '%b %e, %Y'
            }
        });
    }
}

class ScopeProgressChartBuilder extends ChartBuilderBase {
    constructor(divID, startDate, xAxisPlotLines){

        super(divID);
        this.options.yAxis.title.text = 'Effort (hours)';
        this.options.yAxis.labels.format = '{value}';
        this.options.xAxis.labels.format = '{value:%e %b %y}';
        this.options.xAxis.type = 'datetime';
        this.options.tooltip.headerFormat = '';
        this.options.tooltip.footerFormat = '';
        this.options.tooltip.pointFormat = '<b>{series.name}: {point.y:.2f} h ({point.x:%m/%d/%Y})</b><br>';
        this.options.tooltip.shared = false;
        this.options.xAxis.plotLines = xAxisPlotLines;
        this.options.plotOptions.series.pointStart = startDate;
        this.options.plotOptions.series.pointInterval = 24 * 3600 * 1000;
    }
}
