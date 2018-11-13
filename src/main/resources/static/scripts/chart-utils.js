
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

    static createIssueTypeFilter (div, issueTypes, filterCallback) {

        function createInputCheckbox (name, value, checked) {
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = name;
            checkbox.value = value;
            checkbox.checked = checked;
            return checkbox;
        }

        function createFilterItem (labelText, checkboxName, checkboxValue, checkboxChecked) {
            const label = document.createElement('label');
            const checkbox = createInputCheckbox(checkboxName, checkboxValue, checkboxChecked);
            checkbox.addEventListener('click', function () {
                const checkboxes = document.querySelectorAll(`#${div.id} input[name=issueType]`);
                const allSelected = checkboxes.length === Array.from(checkboxes).filter((cb) => cb.checked).length;
                const toggleAllCheckbox = document.querySelector(`#${div.id} input[name=allTypesToggle]`);
                toggleAllCheckbox.checked = allSelected;
                filterCallback();
            });
            label.appendChild(checkbox);
            label.appendChild(document.createTextNode(labelText));
            return label;
        }

        while (div.firstChild) {
            div.removeChild(div.firstChild);
        }

        const toggleAllCheckbox = createInputCheckbox('allTypesToggle', 'allTypesToggle', true);
        toggleAllCheckbox.addEventListener('click', function () {
            const checkboxes = document.querySelectorAll(`#${div.id} input[name=issueType]`);
            checkboxes.forEach((cb) => cb.checked = this.checked);
            filterCallback();
        });
        const label = document.createElement('label');
        label.appendChild(toggleAllCheckbox);
        label.appendChild(document.createTextNode('All Types'));
        div.appendChild(label);
        div.appendChild(document.createElement('hr'));
        issueTypes.slice().reverse().forEach((issueType) => {
            const item = createFilterItem(issueType, 'issueType', issueType, true);
            div.appendChild(item);
            div.appendChild(document.createElement('br'));
        });
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
}

class ChartBuilderBase {
    constructor (divID){
        this.options = ChartUtils.getDefaultHighchartsOptions();
        this.options.chart.renderTo = divID;
        this.options.credits = false;
        this.options.chart.events = {
                selection: function (event) {
                    if (event.resetSelection) {
                        this.resetZoomButton = this.resetZoomButton.destroy(); // hide // button
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
        this.options.series = seriesData;
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
