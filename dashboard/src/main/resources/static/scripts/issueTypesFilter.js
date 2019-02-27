class IssueTypeFilter {

    constructor(issueTypes, containerElement, filterCallback) {
        this._issueTypes = issueTypes;
        this._containerElement = containerElement;
        this._filterCallback = filterCallback;
        this._toggleAllCheckbox = null;
        this._filterItems = [];
        this._buildIssuesTypesCheckboxes();
    }

    _buildIssuesTypesCheckboxes() {
        this._clean();

        this._createToggleAllCheckbox();

        this._containerElement.appendChild(this._toggleAllCheckbox.component);
        this._containerElement.appendChild(document.createElement('hr'));

        this._issueTypes.slice().reverse().forEach((issueType) => {
            const item = this._createFilterItem(issueType, 'issueType', issueType, true);
            this._containerElement.appendChild(item);
            this._containerElement.appendChild(document.createElement('br'));
        });
    }

    _createToggleAllCheckbox() {
        this._toggleAllCheckbox = new IssueTypeFilterItem('All Types', 'allTypesToggle', 'allTypesToggle', true, () => {
            const isChecked = this._toggleAllCheckbox.checked;
            this._filterItems.forEach((item) => item.setChecked(isChecked));
            this._callback();
        });
    }

    _createFilterItem(labelText, checkboxName, checkboxValue, checkboxChecked) {

        const checkbox = new IssueTypeFilterItem(labelText, checkboxName, checkboxValue, checkboxChecked, () => {
            const allSelected = this._filterItems.length === this._filterItems.filter((item) => item.checked).length;
            this._toggleAllCheckbox.setChecked(allSelected);
            this._callback();
        });

        this._filterItems.push(checkbox);
        return checkbox.component;
    }

    _callback() {
        const selectedTypes = this._filterItems.filter((item) => item.checked).map((item) => item.issueType);
        this._filterCallback(selectedTypes, this._toggleAllCheckbox.checked);
    }


    _clean() {
        while (this._containerElement.firstChild) {
            this._containerElement.removeChild(this._containerElement.firstChild);
        }
    }
}

class IssueTypeFilterItem {

    constructor(labelText, name, value, checked, clickCallback) {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.name = name;
        checkbox.value = value;
        checkbox.checked = checked;
        checkbox.addEventListener('click', clickCallback);
        this.checkbox = checkbox;

        this.label = document.createElement('label');
        this.label.appendChild(checkbox);
        this.label.appendChild(document.createTextNode(labelText));
    }

    get checked() {
        return this.checkbox.checked;
    }

    get component() {
        return this.label;
    }

    get issueType() {
        return this.checkbox.value;
    }

    setChecked(isChecked) {
        this.checkbox.checked = isChecked;
    }
}