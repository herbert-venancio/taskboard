dc.paperList = function (parent, chartGroup) {
    var SELECT_CSS_CLASS = 'dc-paper-list';
    var OPTION_CSS_CLASS = 'dc-paper-item';
    var ALL_CSS_CLASS = 'dc-paper-item--all';

    var _chart = dc.baseMixin({});

    var _selectAll;
    var _list;
    var _promptText = 'Select all';
    var _multiple = false;
    var _promptValue = null;
    var _order = function (a, b) {
        return _chart.keyAccessor()(a) > _chart.keyAccessor()(b) ?
             1 : _chart.keyAccessor()(b) > _chart.keyAccessor()(a) ?
            -1 : 0;
    };

    var _filterDisplayed = function (d) {
        return _chart.valueAccessor()(d) > 0;
    };

    _chart.data(function (group) {
        return group.all().filter(_filterDisplayed);
    });

    _chart.toggleSelectAll = function(event) {
        if(!_multiple)
            return;
        var checked = event.detail.value;
        if(checked) {
            var all = _chart.data().map(_chart.keyAccessor());
            _chart.onChange(all);
        } else {
            _chart.onChange(_promptValue);
        }
    };

    _chart._doRender = function () {
        var anchor = Polymer.dom(_chart.anchor());
        // remove old
        var hselect = anchor.querySelector('paper-listbox');
        if(hselect)
            anchor.removeChild(hselect);
        var selectAll = anchor.querySelector('paper-checkbox');
        if(selectAll)
            anchor.removeChild(selectAll);

        if(_multiple) {
            _selectAll = Polymer.dom(document.createElement('paper-checkbox'));
            _selectAll.classList.add(OPTION_CSS_CLASS);
            _selectAll.classList.add(ALL_CSS_CLASS);
            _selectAll.textContent = _promptText;
            anchor.appendChild(_selectAll.node);
        }

        // add new
        _list = Polymer.dom(document.createElement('paper-listbox'));
        _list.classList.add(SELECT_CSS_CLASS);
        _list.node.attrForSelected = 'value';
        _list.node.selectedAttribute = 'checked';

        anchor.appendChild(_list.node);

        _chart._doRedraw();

        return _chart;
    };

    _chart._doRedraw = function () {
        _list.node.removeEventListener('selected-changed', onChange);
        _list.node.removeEventListener('selected-values-changed', onChange);
        if(_selectAll) {
            _selectAll.node.removeEventListener('checked-changed', _chart.toggleSelectAll);
        }

        setAttributes();
        renderOptions();
        Polymer.dom.flush();

        if(_multiple) {
            _list.node.selectedValues = _chart.filters();
            _list.node.addEventListener('selected-values-changed', onChange);
            _selectAll.node.checked = _chart.isAllSelected();
            _selectAll.node.addEventListener('checked-changed', _chart.toggleSelectAll);
        } else {
            _list.node.selected = _chart.filter() || '';
            _list.node.addEventListener('selected-changed', onChange);
        }

        return _chart;
    };

    _chart.isAllSelected = function() {
        if (!_multiple)
            return !_chart.hasFilter();
        return _chart.filters().length == _chart.data().length;
    };

    function renderOptions () {
        var index = 0;
        var item;

        if(!_multiple) {
            item = getOrCreateItem(index++);
            item.classList.add(OPTION_CSS_CLASS);
            item.setAttribute('value', _promptValue || '');
            item.textContent = _promptText;
        }

        var data = _chart.data();
        for(var i = 0; i < data.length; ++i) {
            item = getOrCreateItem(index++);
            item.classList.add(OPTION_CSS_CLASS);
            item.setAttribute('value', _chart.keyAccessor()(data[i]));
            item.textContent = _chart.title()(data[i]);
        }

        var max = data.length + 1;
        while(_list.children.length > max) {
            _list.removeChild(_list.children[max]);
        }
    }

    function getOrCreateItem(index) {
        var type = _multiple ? 'paper-checkbox' : 'paper-radio-button';
        while(_list.children.length <= index) {
            _list.appendChild(document.createElement(type));
        }
        return Polymer.dom(_list.children[index]);
    }

    function onChange (event) {
        _list.node.debounce('value-changed', function() {
            if(_multiple) {
                var values = event.target.selectedValues.filter(function(v) { return v != _promptValue; });
                _chart.onChange(values.length > 0 ? values : _promptValue);
            } else {
                _chart.onChange(event.target.selected);
            }
        }, 0);
    }

    _chart.onChange = function (val) {
        if (val && _multiple) {
            _chart.replaceFilter([val]);
        } else if (val) {
            _chart.replaceFilter(val);
        } else {
            _chart.filterAll();
        }
        dc.events.trigger(function () {
            _chart.redrawGroup();
        });
    };

    function setAttributes () {
        _list.node.multi = _multiple;
    }

    /**
     * Get or set the function that controls the ordering of option tags in the
     * paper list. By default options are ordered by the group key in ascending
     * order.
     * @name order
     * @memberof dc.paperList
     * @instance
     * @param {Function} [order]
     * @example
     * // order by the group's value
     * chart.order(function (a,b) {
     *     return a.value > b.value ? 1 : b.value > a.value ? -1 : 0;
     * });
     **/
    _chart.order = function (order) {
        if (!arguments.length) {
            return _order;
        }
        _order = order;
        return _chart;
    };

    /**
     * Get or set the text displayed in the options used to prompt selection.
     * @name promptText
     * @memberof dc.paperList
     * @instance
     * @param {String} [promptText='Select all']
     * @example
     * chart.promptText('All states');
     **/
    _chart.promptText = function (_) {
        if (!arguments.length) {
            return _promptText;
        }
        _promptText = _;
        return _chart;
    };

    /**
     * Get or set the function that filters option tags prior to display. By default options
     * with a value of < 1 are not displayed.
     * @name filterDisplayed
     * @memberof dc.paperList
     * @instance
     * @param {function} [filterDisplayed]
     * @example
     * // display all options override the `filterDisplayed` function:
     * chart.filterDisplayed(function () {
     *     return true;
     * });
     **/
    _chart.filterDisplayed = function (filterDisplayed) {
        if (!arguments.length) {
            return _filterDisplayed;
        }
        _filterDisplayed = filterDisplayed;
        return _chart;
    };

    /**
     * Controls the type of paper list. Setting it to true converts the underlying
     * HTML tag into a multiple select.
     * @name multiple
     * @memberof dc.paperList
     * @instance
     * @param {boolean} [multiple=false]
     * @example
     * chart.multiple(true);
     **/
    _chart.multiple = function (multiple) {
        if (!arguments.length) {
            return _multiple;
        }
        _multiple = multiple;

        return _chart;
    };

    /**
     * Controls the default value to be used for
     * [dimension.filter](https://github.com/crossfilter/crossfilter/wiki/API-Reference#dimension_filter)
     * when only the prompt value is selected. If `null` (the default), no filtering will occur when
     * just the prompt is selected.
     * @name promptValue
     * @memberof dc.paperList
     * @instance
     * @param {?*} [promptValue=null]
     **/
    _chart.promptValue = function (promptValue) {
        if (!arguments.length) {
            return _promptValue;
        }
        _promptValue = promptValue;

        return _chart;
    };

    return _chart.anchor(parent, chartGroup);
};