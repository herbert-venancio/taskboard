class WidgetOptionBuilder {

    constructor() {
        this._option = {
            disabled: false,
            hidden: false,
            icon: undefined,
            title: undefined,
            cssClasses: undefined,
            tap: undefined
        };
    }

    withDisabled(isDisabled) {
        this._option.disabled = isDisabled;
        return this;
    }

    withHidden(isHidden) {
        this._option.hidden = isHidden;
        return this;
    }

    withIcon(icon) {
        this._option.icon = icon;
        return this;
    }

    withTitle(title) {
        this._option.title = title;
        return this;
    }

    withCSSClasses(cssClasses) {
        this._option.cssClasses = cssClasses;
        return this;
    }

    withOnTap(onTapFunc) {
        this._option.tap = onTapFunc;
        return this;
    }

    build() {
        if (!this._option.icon)
            throw '"icon" is required.';

        else if (!this._option.title)
            throw '"title" is required.';

        else if (!this._option.tap)
            throw '"onTap" is required.';

        return this._option;
    }

}
