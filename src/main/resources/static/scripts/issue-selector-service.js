class IssueSelectorService {

    select(issue) {
        const selectedIssues = issueSelectionData.getSelectedIssues();
        if (this._hasIssueSelectedOnAnotherStep(selectedIssues, issue)) {
            this.unselectAllIssues();
        }
        selectedIssues.push(issue);
        IssueSelectionUtils.setIsSelected(issue, true);
        taskboard.makeAllStepsUnsortable();
        this._setSelectionElementsStyle(issue);
    }

    unselect(issue) {
        const selectedIssues = issueSelectionData.getSelectedIssues();
        const index = selectedIssues.findIndex(i => i === issue);

        if (index > -1) {
            selectedIssues.splice(index, 1);
            IssueSelectionUtils.setIsSelected(issue, false);
        }

        if (selectedIssues.length === 0) {
            taskboard.makeAllStepsSortable();
        } else {
            this._setSelectionElementsStyle(issue);
        }
    }

    unselectAllIssues() {
        const selectedIssues = issueSelectionData.getSelectedIssues();
        if (_.isEmpty(selectedIssues))
            return;

        selectedIssues.forEach(i => {
            IssueSelectionUtils.setIsSelected(i, false);
            IssueSelectionUtils.setIsMoveToTopButtonVisible(i, false);
            IssueSelectionUtils.setIsTopBorderHidden(i, false);
            IssueSelectionUtils.setIsBottomBorderHidden(i, false);
            IssueSelectionUtils.setIsLeftBorderHidden(i, false);
            IssueSelectionUtils.setIsRightBorderHidden(i, false);
        });
        selectedIssues.splice(0);
        taskboard.makeAllStepsSortable();
    }

    hasAnyIssueSelected() {
        return !_.isEmpty(issueSelectionData.getSelectedIssues());
    }

    isSelectionKeyPressed(event) {
        return event.detail.sourceEvent.ctrlKey || event.detail.sourceEvent.metaKey;
    }

    setSelectionElementsStyle(issueStepId, issueStepColumnCount) {
        if (!this.hasAnyIssueSelected())
            return;

        const allIssues = taskboard.getIssuesByStep(issueStepId);
        const orderedIssues = taskboard.getOrderedIssues(allIssues);
        const selectionGroups = [];

        orderedIssues.forEach((i, issueIndex) => {
            const selectedIssue = this._getSelectedIssue(i);
            if (_.isEmpty(selectedIssue))
                return;

            this._addToSelectionGroups(selectedIssue, selectionGroups, issueIndex, issueStepColumnCount, orderedIssues);
            IssueSelectionUtils.setIsMoveToTopButtonVisible(selectedIssue, false);
            IssueSelectionUtils.setIsTopBorderHidden(selectedIssue, this._isIssueSelectedOnTopOf(issueIndex, issueStepColumnCount, orderedIssues));
            IssueSelectionUtils.setIsLeftBorderHidden(selectedIssue,  this._isIssueSelectedOnLeftOf(issueIndex, issueStepColumnCount, orderedIssues));
            IssueSelectionUtils.setIsRightBorderHidden(selectedIssue, this._isIssueSelectedOnRightOf(issueIndex, issueStepColumnCount, orderedIssues));
            IssueSelectionUtils.setIsBottomBorderHidden(selectedIssue, this._isIssueSelectedOnBottomOf(issueIndex, issueStepColumnCount, orderedIssues));
        })
        this._mergeContiguousSelectionGroups(selectionGroups, issueStepColumnCount, orderedIssues);
        this._setMoveToTopButton(selectionGroups);
    }

    _setSelectionElementsStyle(issue) {
        const issueStepId = taskboard.getIssueStep(issue.item).id;
        const issueStepColumnCount = document.querySelector('board-step#step-' + issueStepId).columns;
        this.setSelectionElementsStyle(issueStepId, issueStepColumnCount);
    }

    _addToSelectionGroups(issue, selectionGroups, issueIndex, columnsCount, stepIssues) {
        if (this._isIssueOnSelectionGroups(issue, selectionGroups))
            return;

        const contiguousIssue = this._getContiguousIssueOnSelectionGroup(issue, selectionGroups, issueIndex, columnsCount, stepIssues);
        if (contiguousIssue) {
            const group = this._getIssueSelectionGroup(contiguousIssue, selectionGroups);
            group.push(issue);
        } else {
            const newGroup = [issue];
            selectionGroups.push(newGroup);
        }
    }

    _getIssueSelectionGroup(issue, selectionGroups) {
        return selectionGroups.find(group => group.some(issueComponent => issueComponent.item === issue));
    }

    _isIssueOnSelectionGroups(issue, selectionGroups) {
        if (_.isEmpty(issue)) {
            return false;
        }
        const selectionGroup = this._getIssueSelectionGroup(issue, selectionGroups);
        return !_.isEmpty(selectionGroup);
    }

    _getContiguousIssueOnSelectionGroup(issue, selectionGroups, issueIndex, columnsCount, stepIssues) {
        let contiguousIssue;
        let contiguousIssueIndex;

        if (this._hasBottomIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups)) {
            contiguousIssueIndex = issueIndex + columnsCount;
            contiguousIssue = stepIssues[contiguousIssueIndex];
        } else if (this._hasLeftIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups)) {
            contiguousIssueIndex = issueIndex - 1;
            contiguousIssue = stepIssues[contiguousIssueIndex];
        } else if (this._hasRightIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups)) {
            contiguousIssueIndex = issueIndex + 1;
            contiguousIssue = stepIssues[contiguousIssueIndex];
        } else if (this._hasTopIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups)) {
            contiguousIssueIndex = issueIndex - columnsCount;
            contiguousIssue = stepIssues[contiguousIssueIndex];
        }
        return contiguousIssue;
    }

    _isIssueSelectedOnTopOf(index, columnsCount, stepIssues) {
        return !_.isEmpty(this._getSelectedIssue(stepIssues[index - columnsCount]));
    }

    _hasTopIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups) {
       const topIssueIndex = issueIndex - columnsCount;
       const topIssue = this._getSelectedIssue(stepIssues[topIssueIndex]);
       return this._isIssueOnSelectionGroups(topIssue, selectionGroups);
    }

    _isIssueSelectedOnLeftOf(index, columnsCount, stepIssues) {
        if (this._isFirstColumn(index, columnsCount)) {
            return false;
        }
        return !_.isEmpty(this._getSelectedIssue(stepIssues[index - 1]));
    }

    _hasLeftIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups) {
        if (this._isFirstColumn(issueIndex, columnsCount)) {
            return false;
        }
        const leftIssueIndex = issueIndex - 1;
        const leftIssue = this._getSelectedIssue(stepIssues[leftIssueIndex]);
        return this._isIssueOnSelectionGroups(leftIssue, selectionGroups);
    }

    _isIssueSelectedOnRightOf(index, columnsCount, stepIssues) {
        if (this._isLastColumnOrLastIssue(index, columnsCount, stepIssues)) {
            return false;
        }
        return !_.isEmpty(this._getSelectedIssue(stepIssues[index + 1]));
    }

    _hasRightIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups) {
        if (this._isLastColumnOrLastIssue(issueIndex, columnsCount, stepIssues)) {
            return false;
        }
        const rightIssueIndex = issueIndex + 1;
        const rightIssue = this._getSelectedIssue(stepIssues[rightIssueIndex]);
        return this._isIssueOnSelectionGroups(rightIssue, selectionGroups);
    }

    _isIssueSelectedOnBottomOf(index, columnsCount, stepIssues) {
        return !_.isEmpty(this._getSelectedIssue(stepIssues[index + columnsCount]));
    }

    _hasBottomIssueOnSelectionGroups(issueIndex, columnsCount, stepIssues, selectionGroups) {
        const bottomIssueIndex = issueIndex + columnsCount;
        const bottomIssue = this._getSelectedIssue(stepIssues[bottomIssueIndex]);
        return this._isIssueOnSelectionGroups(bottomIssue, selectionGroups);
    }

    _isFirstColumn(issueIndex, columnsCount) {
        return issueIndex % columnsCount === 0;
    }

    _isLastColumnOrLastIssue(issueIndex, columnsCount, stepIssues) {
        return ((issueIndex + 1) % columnsCount === 0) || (issueIndex === stepIssues.length -1);
    }

    _hasIssueSelectedOnAnotherStep(selectedIssues, issue) {
        if (_.isEmpty(selectedIssues))
            return false;

        const lastSelectionStep = taskboard.getIssueStep(_.last(selectedIssues).item).id;
        const actualStep = taskboard.getIssueStep(issue.item).id;
        return lastSelectionStep !== actualStep;
    }

    _getSelectedIssue(issue) {
        return issueSelectionData.getSelectedIssues().find(f => f.item === issue);
    }

    _mergeContiguousSelectionGroups(selectionGroups, columnsCount, stepIssues) {
        selectionGroups.forEach((group, groupIndex) => {
            for (let i = groupIndex + 1; i < selectionGroups.length; i++ ) {
                const nextSelectionGroup = selectionGroups[i];
                if (this._areContiguousGroups(group, nextSelectionGroup, stepIssues, columnsCount)) {
                    this._mergeGroups(selectionGroups, group, nextSelectionGroup);
                    this._mergeContiguousSelectionGroups(selectionGroups, columnsCount, stepIssues);
                }
            }
        })
    }

    _areContiguousGroups(group, nextSelectionGroup, stepIssues, columnsCount) {
        return group.some(groupIssue => {
            const groupIssueIndex = stepIssues.findIndex(i => i === groupIssue.item);
            return nextSelectionGroup.some(nextGroupIssue => {
                const nextGroupIssueIndex = stepIssues.findIndex(i => i === nextGroupIssue.item);
                return this._areIssuesContiguous(groupIssueIndex, nextGroupIssueIndex, columnsCount, stepIssues);
            });
        });
    }

    _mergeGroups(selectionGroups, targetGroup, sourceGroup) {
        const sourceGroupIndex = selectionGroups.indexOf(sourceGroup);

        selectionGroups.splice(sourceGroupIndex, 1);

        sourceGroup.forEach(issue => {
            targetGroup.push(issue);
        })
    }

    _areIssuesContiguous(firstIssueIndex, anotherIssueIndex, columnsCount, stepIssues) {
        const anotherIssue = this._getSelectedIssue(stepIssues[anotherIssueIndex]);

        const issueOnTop = this._getSelectedIssue(stepIssues[firstIssueIndex - columnsCount]);
        if (issueOnTop === anotherIssue) return true;

        if (!this._isLastColumnOrLastIssue(firstIssueIndex,columnsCount, stepIssues)) {
            const issueOnRight = this._getSelectedIssue(stepIssues[firstIssueIndex + 1]);
            if (issueOnRight === anotherIssue) return true;
        }

        const issueOnBottom = this._getSelectedIssue(stepIssues[firstIssueIndex + columnsCount]);
        if (issueOnBottom === anotherIssue) return true;

        if (!this._isFirstColumn(firstIssueIndex, columnsCount)) {
            const issueOnLeft = this._getSelectedIssue(stepIssues[firstIssueIndex - 1]);
            if (issueOnLeft === anotherIssue) return true;
        }

        return false;
    }

    _setMoveToTopButton(selectionGroups) {
        selectionGroups.forEach(group => {
            let issueOnTop = {offsetLeft: 0, offsetTop: 99999999999};

            group.forEach(issue => {
                if (issue.offsetTop <= issueOnTop.offsetTop && issue.offsetLeft >= issueOnTop.offsetLeft) {
                    issueOnTop = issue;
                }
            })
            IssueSelectionUtils.setIsMoveToTopButtonVisible(issueOnTop, true);
        })
    }
}
const issueSelectorService = new IssueSelectorService();

class IssueSelectionUtils {
    static setIsMoveToTopButtonVisible(issue, value) {
        issue.set('selection.isMoveToTopButtonVisible', value);
    }

    static setIsSelected(issue, value) {
        issue.set('selection.isSelected', value);
    }

    static setIsTopBorderHidden(issue, value) {
        issue.set('selection.isTopBorderHidden', value);
    }

    static setIsLeftBorderHidden(issue, value) {
        issue.set('selection.isLeftBorderHidden', value);
    }

    static setIsRightBorderHidden(issue, value) {
        issue.set('selection.isRightBorderHidden', value);
    }

    static setIsBottomBorderHidden(issue, value) {
        issue.set('selection.isBottomBorderHidden', value);
    }
}
