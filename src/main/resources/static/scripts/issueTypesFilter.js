class IssueTypeFilterItem {

    constructor(labelText,name, value, checked, clickCallback) {
        const checkbox = document.createElement('input')
        checkbox.type = 'checkbox'
        checkbox.name = name
        checkbox.value = value
        checkbox.checked = checked
        checkbox.addEventListener('click',clickCallback)
        this.checkbox = checkbox

        this.label = document.createElement('label');
        this.label.appendChild(checkbox);
        this.label.appendChild(document.createTextNode(labelText));
    }

    get checked(){
        return this.checkbox.checked
    }

    get component(){
        return this.label
    }

    get issueType(){
        return this.checkbox.value
    }

    setChecked(isChecked){
        this.checkbox.checked = isChecked
    }

}

class IssueTypeFilter {

    constructor(issueTypes, filterSelector, filterCallback) {
        this.issueTypes = issueTypes
        this.filterSelector = filterSelector
        this.filterCallback = filterCallback
        
        this.filterItems = []
        this._buildIssuesTypes()
    }

     _buildIssuesTypes(){
        this._clean()

        this._createToggleAll()

        this.filterSelector.appendChild(document.createElement('hr'));
        
        this.issueTypes.slice().reverse().forEach((issueType) => {
            const item = this._createFilterItem(issueType, 'issueType', issueType, true);
            this.filterSelector.appendChild(item);
            this.filterSelector.appendChild(document.createElement('br'));
        });
    }

    _createToggleAll(){
        this.toggleAllCheckbox = new IssueTypeFilterItem('All Types','allTypesToggle', 'allTypesToggle', true,() => {
            const isChecked = this.toggleAllCheckbox.checked
            this.filterItems.forEach((item) => item.setChecked(isChecked));
            this._callback();
        });

        this.filterSelector.appendChild(this.toggleAllCheckbox.component);
    }

    _createFilterItem(labelText, checkboxName, checkboxValue, checkboxChecked) {
        
        const checkbox = new IssueTypeFilterItem(labelText,checkboxName, checkboxValue, checkboxChecked, () => {
            const allSelected = this.filterItems.length === Array.from(this.filterItems).filter((item) => item.checked).length;
            this.toggleAllCheckbox.setChecked(allSelected);
            this._callback()
        });
        
        this.filterItems.push(checkbox)
        return checkbox.component;
    }

    _callback(){
        const selectedTypes = Array.from(this.filterItems).filter((item) => item.checked).map((item) => item.issueType);
        this.filterCallback(selectedTypes,this.toggleAllCheckbox.checked)
    }
    

    _clean(){
        while (this.filterSelector.firstChild) {
            this.filterSelector.removeChild(this.filterSelector.firstChild);
        }
    }

   
}