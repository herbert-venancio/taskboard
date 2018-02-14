dc.paperMenu = function (parent, chartGroup) {
    var SELECT_CSS_CLASS = 'dc-paper-menu';
    var OPTION_CSS_CLASS = 'dc-paper-item';

    var _chart = dc.baseMixin({});

    var _select;
    var _menu;
    var _promptText = 'Select all';
    var _multiple = false;
    var _promptValue = null;
    var _numberVisible = null;
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

    _chart._doRender = function () {
        var anchor = Polymer.dom(_chart.anchor());
        // remove old
        var hselect = anchor.querySelector('paper-dropdown-menu');
        if(hselect)
            anchor.removeChild(hselect);

        // add new
        _select = Polymer.dom(document.createElement('paper-dropdown-menu'));
        _select.node.noLabelFloat = true;
        _select.classList.add(SELECT_CSS_CLASS);

        _menu = Polymer.dom(document.createElement('paper-menu'));
        _menu.setAttribute('slot', 'dropdown-content');
        _menu.classList.add('dropdown-content');
        _menu.node.attrForSelected = 'value';

        var item = Polymer.dom(document.createElement('paper-item'));
        item.classList.add(OPTION_CSS_CLASS);
        item.setAttribute('value', _promptValue || '');
        item.textContent = _promptText;

        _menu.appendChild(item.node);
        _select.appendChild(_menu.node);
        anchor.appendChild(_select.node);

        _chart._doRedraw();

        if(_multiple) {
            _menu.node.selectedValues = _chart.filters();
        } else {
            _menu.node.selected = _chart.filter() || '';
        }

        _menu.node.addEventListener('selected-changed', onChange);
        _menu.node.addEventListener('selected-values-changed', onChange);

        return _chart;
    };

    _chart._doRedraw = function () {
        setAttributes();
        renderOptions();
        Polymer.dom.flush();
        return _chart;
    };

    function renderOptions () {
        var item = _menu.firstChild;
        var data = _chart.data();
        for(var i = 0; i < data.length; ++i) {
            item = getOrCreateNext(item);
            item = Polymer.dom(item);
            item.classList.add(OPTION_CSS_CLASS);
            item.setAttribute('value', _chart.keyAccessor()(data[i]));
            item.textContent = _chart.title()(data[i]);
        }

        var max = data.length + 1;
        while(_menu.children.length > max) {
            _menu.removeChild(_menu.children[max]);
        }
    }

    function getOrCreateNext(paperItem) {
        return Polymer.dom(paperItem).nextSibling || _menu.appendChild(document.createElement('paper-item'));
    }

    function onChange (event) {
        _menu.node.debounce('value-changed', function() {
            if(_multiple) {
                var values = event.target.selectedValues;
                _chart.onChange(values.length > 0 ? values : null);
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
        if(_multiple) {
            _menu.node.multi = true;
        } else {
            _menu.node.multi = false;
        }
        if (_numberVisible !== null) {
            // not implemented yet
        }
    }

    /**
     * Get or set the function that controls the ordering of option tags in the
     * select menu. By default options are ordered by the group key in ascending
     * order.
     * @name order
     * @memberof dc.selectMenu
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
     * @memberof dc.selectMenu
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
     * @memberof dc.selectMenu
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
     * Controls the type of select menu. Setting it to true converts the underlying
     * HTML tag into a multiple select.
     * @name multiple
     * @memberof dc.selectMenu
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
     * @memberof dc.selectMenu
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

    /**
     * Controls the number of items to show in the select menu, when `.multiple()` is true. This
     * controls the [`size` attribute](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/select#Attributes) of
     * the `select` element. If `null` (the default), uses the browser's default height.
     * @name numberItems
     * @memberof dc.selectMenu
     * @instance
     * @param {?number} [numberVisible=null]
     * @example
     * chart.numberVisible(10);
     **/
    _chart.numberVisible = function (numberVisible) {
        if (!arguments.length) {
            return _numberVisible;
        }
        _numberVisible = numberVisible;

        return _chart;
    };

    _chart.size = dc.logger.deprecate(_chart.numberVisible, 'selectMenu.size is ambiguous - use numberVisible instead');

    return _chart.anchor(parent, chartGroup);
};