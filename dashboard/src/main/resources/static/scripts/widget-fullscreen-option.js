class WidgetFullscreenOption {

    constructor(widget) {
        this._widget = widget;

        this._isActive = false;
        this._initialStyle = {};
        this._initialScrollTop = 0;

        this._title = 'Fullscreen';
        this._maximizeIcon = 'taskboard-icons:maximize';
        this._minimizeIcon = 'taskboard-icons:minimize';

        this._option = new WidgetOptionBuilder()
            .withIcon(this._maximizeIcon)
            .withTitle(this._title)
            .withOnTap(() => this._resizeFullScreen())
            .build();

        this._setDefaultStyle();

        this._registerUpdateOnWindowResize();
        this._registerRemoveFullscreenOnCategoryChanged();
    }

    get option() {
        return this._option;
    }

    _setDefaultStyle() {
        this._marginSize = 16;
        this._backgroundColor = '#444';

        this._scrollDuration = 400;
        this._transitionTimingFunction = 'ease-out';
        this._transitionDurationInMillis = 700;
    }

    _registerUpdateOnWindowResize() {
        executeOnWindowResizingEnds(() => {
                if (this._isActive)
                    this._applyFullscreen();
            });
    }

    _registerRemoveFullscreenOnCategoryChanged() {
        const kpiCategoriesMenu = document.querySelector('.kpis__categories');
        kpiCategoriesMenu.addEventListener('click', () => {
                if (this._isActive)
                    this._removeFullscreen(false);
            });
    }

    _resizeFullScreen() {
        if (this._isActive)
            this._removeFullscreen(true);
        else
            this._applyFullscreen();
    }

    _applyFullscreen() {
        this._isActive = true;
        this._widget.set('isReady', false);

        const widgetsContainerEl = this._getWidgetsContainerEl();
        this._initialScrollTop = widgetsContainerEl.scrollTop;
        widgetsContainerEl.style.overflow = 'hidden';
        elementScroll(widgetsContainerEl, 0, this._scrollDuration);

        this._initialStyle = clone(this._getWidgetWrapContainerEl().style);

        const position = this._getFullscreenPositionObj();

        this._executeAnimation(`${position.top}px`, `${position.right}px`, `${position.bottom}px`, `${position.left}px`,
            () => {
                ChartUtils.resizeHighchartsChart(this._widget.chart);
                this._widget.set('isReady', true);
            });

        this._changeIcon(this._minimizeIcon);
    }

    _removeFullscreen(scrollToPreviousPosition) {
        this._isActive = false;
        this._widget.set('isReady', false);

        const widgetsContainerEl = this._getWidgetsContainerEl();
        widgetsContainerEl.style.overflow = '';
        if (scrollToPreviousPosition)
            elementScroll(widgetsContainerEl, this._initialScrollTop, this._scrollDuration);

        this._executeAnimation(this._initialStyle.top, this._initialStyle.right, this._initialStyle.bottom, this._initialStyle.left,
            () => {
                this._setWidgetWrapContainerStyle(this._initialStyle);
                ChartUtils.resizeHighchartsChart(this._widget.chart);
                this._widget.set('isReady', true);
            });

        this._changeIcon(this._maximizeIcon);
    }

    _getFullscreenPositionObj() {
        return {
            top: this._getFirstVisibleWidgetEl().offsetTop - this._widget.offsetTop,
            right: (this._widget.offsetLeft + this._widget.offsetWidth) - (window.innerWidth - this._marginSize),
            bottom: (this._widget.offsetTop + this._widget.offsetHeight) - (window.innerHeight - this._marginSize),
            left: this._marginSize - this._widget.offsetLeft
        };
    }

    _executeAnimation(cssTopValue, cssRightValue, cssBottomValue, cssLeftValue, callbackFunc) {
        const animationDurationInSec = this._transitionDurationInMillis / 1000;
        const transitionProperty = `${animationDurationInSec}s ${this._transitionTimingFunction}`;
        this._setWidgetWrapContainerStyle(`
            background-color: ${this._backgroundColor};
            top: ${cssTopValue};
            right: ${cssRightValue};
            bottom: ${cssBottomValue};
            left: ${cssLeftValue};
            z-index: 1;
            opacity: 1;
            transition: top ${transitionProperty}, right ${transitionProperty}, bottom ${transitionProperty}, left ${transitionProperty};
            `);

        setTimeout(() => {
            callbackFunc();
        }, this._transitionDurationInMillis);
    }

    _changeIcon(icon) {
        const fullscreenOptIndex = this._widget.options.findIndex(opt => opt.title === this._title);
        this._widget.set('options.'+ fullscreenOptIndex + '.icon', icon);
    }

    _getWidgetWrapContainerEl() {
        return this._widget.$$('widget-wrap').$$('.widget');
    }

    _setWidgetWrapContainerStyle(style) {
        this._getWidgetWrapContainerEl().style = style;
    }

    _getWidgetsContainerEl() {
        return document.querySelector('.kpis__widgets');
    }

    _getFirstVisibleWidgetEl() {
        return document.querySelector('.kpis__widget.active');
    }

}
