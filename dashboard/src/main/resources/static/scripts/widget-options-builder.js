class WidgetOptionsBuilder {

    constructor(widget) {
        if (!widget)
            throw '"widget" is required.';

        this._widget = widget;
        this._options = [];
    }

    withCustomOption(option) {
        this._options.push(option);
        return this;
    }

    withFullscreen() {
        this._options.push(new WidgetFullscreenOption(this._widget).option);
        return this;
    }

    withFilters() {
        this._options.push(new WidgetOptionBuilder()
            .withIcon('taskboard-icons:dashboard-filter')
            .withTitle('Filters')
            .withOnTap(() => this._widget.$$('.filters-modal').open())
            .withHidden(true)
            .build());

        return this;
    }

    withSettings() {
        this._options.push(new WidgetOptionBuilder()
            .withIcon('taskboard-icons:settings')
            .withTitle('Settings')
            .withOnTap(() => {
                this._widget.settingIssueLevel = this._widget._getSavedLevel();
                this._widget.$$('.settings-modal').open();
            })
            .build());

        return this;
    }

    build() {
        return this._options;
    }

}
