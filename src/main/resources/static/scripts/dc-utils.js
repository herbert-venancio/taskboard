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

        this._orderChartByKey(chart);
    };

    this._orderChartByKey = function(chart) {
        chart.ordering(function(d) { return -d.key });
    };

    this.getDateTicks = function(startDate, endDate, numOfTicks) {
        if (!startDate || !endDate)
            return;

        if (numOfTicks < 2)
            numOfTicks = 2;

        var cols = numOfTicks - 1;

        var oneDayInMilli = 86400000;
        var diffInDays = (endDate - startDate)/oneDayInMilli;
        var betweenTicks = diffInDays / cols;

        var ticks = [];
        var lastYearAdded = startDate.getFullYear();
        for (var i = 0; i < cols; i++) {
            var dateTick = addDaysToDate(startDate, Math.round(betweenTicks * i));
            if (lastYearAdded !== dateTick.getFullYear()) {
                lastYearAdded = dateTick.getFullYear();
                ticks.push(new Date(lastYearAdded, 0, 1));
            }
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

        self._updateStyleOfTicksThatContainsYears(chart);

        chart.visibleTickCount = tickCount;
    };

    this._updateStyleOfTicksThatContainsYears = function(chart) {
        var self = this;
        chart.on('pretransition', function(chart) {
            var chartHeight = chart.effectiveHeight();
            chart.selectAll('g.x .tick').each(function(d, i) {
                var textElOfTick = this.childNodes[1];
                if (self._textHasDateYearSplitWord(textElOfTick.innerHTML))
                    return;

                self._transformYearTick(this, chartHeight, i);
            });
        });
    };

    this._textHasDateYearSplitWord = function(text) {
        return text.indexOf(this._DATE_YEAR_SPLIT_WORD) === -1;
    };

    this._transformYearTick = function(tickEl, chartHeight, tickIndex) {
        var textElYear = tickEl.childNodes[2];

        if (textElYear)
            return;

        var lineEl = tickEl.childNodes[0];
        var textElDayMonthYear = tickEl.childNodes[1];

        tickEl.classList.add('tick--has-year');

        var dateArray = textElDayMonthYear.innerHTML.split(this._DATE_YEAR_SPLIT_WORD);
        var dayMonth = dateArray[0];
        var year = dateArray[1];

        var yearLineHeight = chartHeight + 4;
        lineEl.setAttribute('y1', -yearLineHeight);

        if (tickIndex === 0)
            textElDayMonthYear.innerHTML = dayMonth;
        else
            textElDayMonthYear.setAttribute('style', 'display: none');

        var textElYearElStr = '<text class="tick-year" x="0" dy="1.2em" transform="translate(0, -' + (yearLineHeight + 15) + ')" style="text-anchor: middle;">' + year + '</text>';
        tickEl.innerHTML += textElYearElStr;
    };

    this.downloadElementAsJpegImage = function(element, fileName) { 
        domtoimage.toJpeg(element, { quality: 0.95 }) 
            .then(function (dataUrl) { 
                var link = document.createElement('a'); 
                link.setAttribute("type", "hidden");
                link.download = fileName + '.jpeg'; 
                link.href = dataUrl; 

                document.body.appendChild(link);
                link.click();
                link.remove();
            }); 
    };
}
var dcUtils = new DcUtils();
