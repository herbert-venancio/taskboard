/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

function Taskboard() {
    var self = this;

    var _cardFieldFilters;
    var _issuesBySteps;
    var _laneConfiguration;

    this.urlJira = null;
    this.urlLinkGraph = null

    this.fieldSelector = {
        ISSUE_TYPE: 'Issue Type',
        PROJECT: 'Project',
        TEAM: 'Team'
    };

    this.init = function() {
        moment.locale(this.getLocaleFromBrowser());
        window.addEventListener('resize', function() {
            self.refitSteps();
        });
    }

    this.getLoggedUser = function() {
        return window.user;
    }
    
    this.getAvailableTeams = function() {
        return TEAMS;
    }

    this.getTeamById = function(id) {
        return TEAMS_BY_ID[id];
    }

    this.getTaskboardLogoUrl = function() {
        var logoUrlExistis = window.logo !== null && window.logo !== undefined && window.logo !== '';
        var logoUrlDefault = '/static/images/touch/icon-128x128.png';
        return logoUrlExistis ? window.logo : logoUrlDefault;
    }

    this.setCardFieldFilters = function(source, cardFieldFilters) {
        _cardFieldFilters = cardFieldFilters;
        if(source)
            source.fire('iron-signal', {name:'refresh-release-filter'});
    };

    this.getCardFieldFilters = function() {
        return _cardFieldFilters;
    };

    this.getTeams = function() {
        var cardTeamFilter = _.find(this.getCardFieldFilters(), function(fieldFilter) {
            return fieldFilter.fieldSelector.name === self.fieldSelector.TEAM
        });
        return cardTeamFilter.filterFieldsValues;
    };

    this.setIssues = function(issues) {
        this.issues = issues;
        forEachInArray(issues, function (issue, index) {
            self.issues[index] = self.resolveIssueFields(issue);
        });
        setIssuesBySteps(issues);
        this.refitSteps();
    };

    function setIssuesBySteps(issues) {
        _issuesBySteps = {};
        forEachInArray(self.getAllSteps(), function(step) {
            var issuesByStep = [];
            forEachInArray(step.issuesConfiguration, function(issueConfigurationFilter) {
                forEachInArray(issues, function(issue) {
                    if (issueConfigurationFilter.issueType === issue.type && issueConfigurationFilter.status === issue.status)
                        issuesByStep.push(issue)
                });
            });
            _issuesBySteps[step.id] = issuesByStep;
        });
    }

    this.getIssueStep = function(issue) {
        var steps = self.getAllSteps()
        for (var s in steps) {
            var step = steps[s]
            var filters = step.issuesConfiguration

            for (var f in filters) {
                var filter = filters[f]

                if (filter.issueType === issue.type && filter.status === issue.status)
                    return step;
            }
        }
        return null;
    };

    this.getIssuesByStep = function(stepId) {
        if (_issuesBySteps)
            return _issuesBySteps[stepId];
    };

    this.getIssueByKey = function(issueKey) {
        var issue = this.findIssueByKey(issueKey);

        if(issue)
            return issue;

        $.ajax({
            url: '/ws/issues/byKey/' + issueKey + "?onlyVisible="+false,
            async: false,
            success: function (issueFromServer) {
                issue = issueFromServer
            }
        });
        return issue;
    };

    this.findIssueByKey = function(issueKey){
        return findInArray(this.issues, i => i.issueKey === issueKey);
    }

    this.setLaneConfiguration = function(laneConfiguration) {
        _laneConfiguration = laneConfiguration;
    };

    this.getAllSteps = function() {
        var steps = new Array()
        for(var laneIndex in _laneConfiguration) {
            var lane = _laneConfiguration[laneIndex]
            for(var stageIndex in lane.stages) {
                steps = steps.concat(lane.stages[stageIndex].steps)
            }
        }
        return steps;
    };

    this.getTotalLaneWeight = function() {
        var total = 0;

        if (userPreferences.getLevels().length === 0) {
            _laneConfiguration.forEach(function(lane) {
                total += lane.weight;
            });
            return total;
        }

        userPreferences.getLevels().forEach(function(x) {
            if(x.showLevel)
                total += x.weightLevel;
        });

        return total;
    };

    this.getLaneContainerHeight = function() {
        var container = document.querySelector('#lane-container');
        return container ? container.offsetHeight : 0;
    };

    this.getLane = function(id) {
        return document.querySelector('#lane-'+id);
    };

    this.refitSteps = _.debounce(function() {
        var steps = document.querySelectorAll('board-step');
        for(var i = 0; i < steps.length; i++)
            steps[i].refit();
        self.refitTables();
    }, 100);

    this.refitTables = function() {
        $('table').floatThead('reflow');
    };

    this.getHierarchyMatch = function(rootIssueKey) {
        if(!rootIssueKey)
            return null;

        var queue = [];
        var issue = this.getIssueByKey(rootIssueKey);
        queue.push({issue:issue, parents:true, children:true});

        function enqueueParent(item) {
            if(!item.parents || !item.issue.parent)
                return;

            var parentIssue = self.findIssueByKey(item.issue.parent);
            if (parentIssue)
                queue.push({issue:parentIssue, parents:true});
        }

        function enqueueChildren(item) {
            if(!item.children)
                return;

            forEachInArray(item.issue.subtasks, function(subtaskSample) {
                var subtask = self.findIssueByKey(subtaskSample.issueKey);
                if (subtask)
                    queue.push({issue:subtask, children:true});
            });
        }

        var hierarchyIssueKeys = [];
        while(queue.length > 0) {
            var next = queue.shift();
            issue = next.issue;
            if(_.contains(hierarchyIssueKeys, issue.issueKey))
                continue;

            hierarchyIssueKeys.push(issue.issueKey);

            enqueueParent(next);
            enqueueChildren(next);
        }
        return hierarchyIssueKeys;
    };

    this.getDependenciesMatch = function(hierarchyIssueKeys) {
        if(!hierarchyIssueKeys)
            return null;

        var dependencies = [];
        var hierarchyIssues = hierarchyIssueKeys.map(function(key) {
            return self.getIssueByKey(key);
        });
        for(var i = 0; i < this.issues.length; ++i) {
            var issue = this.issues[i];
            if(_.contains(hierarchyIssueKeys, issue.issueKey))
                continue;

            for(var h = 0; h < hierarchyIssues.length; ++h) {
                var hierarchyIssue = hierarchyIssues[h];
                if(_.contains(hierarchyIssue.dependencies, issue.issueKey))
                    dependencies.push(issue.issueKey);
            }
        }
        return dependencies;
    };

    this.getIssueTypeName = function(issueTypeId) {
        var types = JSON.parse(localStorage.getItem("issueTypes"));
        for (var i in types)
            if (types[i].id == issueTypeId)
                return types[i].name;
        return "";
    };

    this.getStatuses = function() {
        if (this.statuses === undefined)
            this.statuses = JSON.parse(localStorage.getItem("statuses"));
        return this.statuses;
    };

    this.getFieldName = function(fs) {
        return FIELDNAMES[fs.fieldId] || fs.fieldId;
    };

    this.getStatus = function(statusId) {
        var statusIdNumber = parseInt(statusId);
        return Object.values(this.getStatuses()).find( function(s) { return s.id === statusIdNumber } );
    };

    this.getStatusName = function(statusId) {
        return this.getStatus(statusId).name;
    };

    var reconnectCount = 0;

    this.connectToWebsocket = function(taskboardHome) {
        var socket = new SockJS('/taskboard-websocket');
        var stompClient = Stomp.over(socket);
        stompClient.debug = function(message) {
            if(!message || message.indexOf('PING') > -1 || message.indexOf('PONG') > -1)
                return;
            if (message.indexOf("ERROR") > -1)
                console.log(message);
        };
        stompClient.connect({},
            function() {
                reconnectCount = 0;
                stompClient.subscribe('/topic/issues/updates', function (response) {
                    handleIssueUpdates(taskboardHome, response)
                });

                stompClient.subscribe('/user/topic/sizing-import/status', function(status) {
                    handleSizingImportStatus(taskboardHome, status);
                });

                stompClient.subscribe('/topic/cache-state/updates', function (response) {
                    taskboardHome.fire("iron-signal", {name:"issue-cache-state-updated", data:{
                        newstate: JSON.parse(response.body)
                    }});
                });

                stompClient.subscribe('/topic/projects/updates', function (response) {
                    taskboardHome.fire('iron-signal', {name:'projects-changed', data:{
                        projects: JSON.parse(response.body)
                    }});
                });
            },
            function() {
                if (reconnectCount >= 10) {
                    fireWebsocketError(taskboardHome, 'Taskboard websocket can\'t establish the connection.');
                    return;
                }

                setTimeout(function() {
                    $.get('/ws/taskboard-websocket/ping')
                    .done(function() {
                        reconnectCount++;
                        self.connectToWebsocket(taskboardHome);
                    })
                    .fail(function(jqXHR) {
                        if (jqXHR && jqXHR.status === 401) {
                            var buttonGoToLogin = [{
                                name: 'Go to Login',
                                callback: function() { window.location = '/login'; }
                            }];
                            fireWebsocketError(taskboardHome, 'Your session has expired.', buttonGoToLogin);
                            return;
                        }
                        if (jqXHR && jqXHR.status === 500) {
                            fireWebsocketError(taskboardHome, 'Unexpected websocket error. Please, report this error to the administrator.');
                            return;
                        }
                        reconnectCount++;
                        self.connectToWebsocket(taskboardHome);
                    });
                }, 10000);
            });
    }

    function fireWebsocketError(source, errorMessage, button) {
        if (!button) {
            button = [{
                name: 'Refresh',
                callback: function() {
                    location.reload(true);
                }
            }];
        }
        self.showError(source, errorMessage, button, true);
        source.fire("iron-signal", {name:"issue-cache-state-updated", data:{
            newstate: 'websocketError'
        }});
    }

    function handleIssueUpdates(taskboardHome, response) {
        var relevantEvents = JSON.parse(response.body)
        var ids = relevantEvents.map(function(i) { return i.issueId })

        $.post({
            url:"/ws/issues/byids", 
            contentType: 'application/json', 
            data: JSON.stringify(ids)
        })
        .done(function(issues) {
            var issueById = {};
            issues.forEach(function(i){
                issueById[i.id] = i;
            })
            var issueUpdateEvents = []
            relevantEvents.forEach(function(event){
                var issueData = issueById[event.issueId];
                if (!issueData)
                    event.updateType = 'DELETED'

                if (event.updateType === 'DELETED') {
                    var previous = getPreviousIssueInstanceById(event.issueId);
                    if (!previous)
                        return;
                    event.target = previous.issue;
                }
                else {
                    event.target = issueData;
                }
                issueUpdateEvents.push(event);
            })

            updateIssuesByEvents(taskboardHome, issueUpdateEvents)
        })
    }

    function updateIssuesByEvents(taskboardHome, updateEvents) {
        var updatedIssueKeys = []
        var updateByStep = {};
        updateEvents.forEach(function(anEvent) {
            var previousInstance = getPreviousIssueInstanceById(anEvent.target.id);
            if (anEvent.updateType === 'DELETED' && previousInstance !== null) {
                self.issues.splice(previousInstance.index, 1);
                updatedIssueKeys.push(anEvent.target.issueKey);
                self.fireIssueUpdated('server', taskboardHome, anEvent.target, anEvent.updateType);
                return;
            }

            var converted = self.resolveIssueFields(anEvent.target);
            if (previousInstance === null)
                self.issues.push(converted);
            else {
                if (previousInstance.issue.stateHash === anEvent.target.stateHash)
                    return;
                self.issues[previousInstance.index] = converted;
            }

            updatedIssueKeys.push(anEvent.target.issueKey);
            var issueStep = self.getIssueStep(converted);
            if (issueStep !== null) {
                var stepId = issueStep.id;
                if (Object.keys(updateByStep).indexOf(stepId) === -1)
                    updateByStep[stepId] = [];

                updateByStep[stepId].push(anEvent.target);
            }

            self.fireIssueUpdated('server', taskboardHome, anEvent.target, anEvent.updateType);
        });

        if (updatedIssueKeys.length === 0)
            return;

        Object.keys(updateByStep).forEach(function(stepIdKey) {
            taskboardHome.fire("iron-signal", {name:"step-update", data:{
                issues: updateByStep[stepIdKey],
                stepId: +stepIdKey
            }})
        })

        if (!hasSomeVisibleIssueByKeys(updatedIssueKeys))
            return;

        taskboardHome.fire("iron-signal", {name:"show-issue-updated-message", data:{
            message: "Issues updated",
            updatedIssueKeys: updatedIssueKeys
        }})
    }

    function handleSizingImportStatus(taskboardHome, response) {
        var status = JSON.parse(response.body);
        taskboardHome.fire("iron-signal", {name:"sizing-import-status", data:{
            status: status
        }});
    }

    function getPreviousIssueInstanceById(id) {
        var previousInstance = null;
        var piIndex = findIndexInArray(self.issues, function(i) { return i.id === id });
        if (piIndex > -1)
            previousInstance = {issue: self.issues[piIndex], index: piIndex};
        return previousInstance;
    }

    this.fireIssueUpdated = function(source, triggerSource, issue, updateType) {
        var converted = updateType === 'DELETED' ? issue : self.convertAndRegisterIssue(issue);
        converted.__eventInfo = {source: source, type: updateType}
        triggerSource.fire("iron-signal", {name:"issues-updated", data:{
            source: source,
            eventType: updateType,
            issue: converted
        }})
    }

    this.convertAndRegisterIssues = function(issues) {
        var converted = []
        issues.forEach(function(issue) {
            converted.push(self.convertAndRegisterIssue(issue));
        })
        return converted;
    }

    this.convertAndRegisterIssue = function(issue) {
        var converted = this.resolveIssueFields(issue);
        
        var previousInstance = getPreviousIssueInstanceById(issue.id);
        if (previousInstance === null)
            self.issues.push(converted)
        else
            self.issues[previousInstance.index] = converted;
        setIssuesBySteps(self.issues);
        return converted;
    }
    
    this.resolveIssueFields = function(issue) {
        if (issue.additionalEstimatedHoursField)
            issue.additionalEstimatedHoursField.name = this.getFieldName(issue.additionalEstimatedHoursField);

        issue.subtasksTshirtSizes.forEach(function(ts) {
            ts.name = this.getFieldName(ts);
        }.bind(this))
        
        return issue;
    }

    function hasSomeVisibleIssueByKeys(issuesKeys) {
        return self.getVisibleIssues().some(issue => issuesKeys.indexOf(issue.issueKey) !== -1);
    }

    this.getVisibleIssues = function() {
        return this.issues.filter(i => searchFilter.match(i));
    };

    this.getTimeZoneIdFromBrowser = function() {
        return Intl.DateTimeFormat().resolvedOptions().timeZone || jstz.determine().name();
    };

    this.getLocaleFromBrowser = function() {
        return window.navigator.userLanguage || window.navigator.language;
    };

    this.getResolutionFieldName = function() {
        return "resolution";
    };

    this.getArchivedText = function(isArchived) {
        return isArchived ? 'archived' : 'active';
    };

    this.getOrderedIssues = function(issues) {
        return this._getExpediteIssues(issues)
            .concat(this._getRegularIssues(issues));
    };

    this._getExpediteIssues = function(issues) {
        return filterInArray(issues, function(issue) {
            return self.isIssueExpedite(issue.classOfServiceValue);
        }).sort(sortByProperty('created'));
    };

    this._getRegularIssues = function(issues) {
        return filterInArray(issues, function(issue) {
            return !self.isIssueExpedite(issue.classOfServiceValue);
        }).sort(sortByProperty('priorityOrder'));
    };

    this.isIssueExpedite = function(classOfServiceValue) {
        return classOfServiceValue === 'Expedite';
    };

    this.getVisibleProjectKeys = function() {
        return _.chain(this.getCardFieldFilters())
            .filter(function(cardFieldFilter) {
                return cardFieldFilter.fieldSelector.name === self.fieldSelector.PROJECT;
            })
            .map(function(cardFieldFilter) {
                return _.chain(cardFieldFilter.filterFieldsValues)
                    .filter(function(filterFieldValue) {
                        return filterFieldValue.selected;
                    })
                    .map(function(filterFieldValue) {
                        return filterFieldValue.value;
                    })
                    .value();
            })
            .flatten()
            .value();
    };

    this.showError = function(source, message, actions, hideClose) {
        source.fire("iron-signal", {name:"show-error-message", data: { message: message, actions: actions, hideClose: hideClose}});
    };
    
    this.showIssueError = function(source, issueKey, message) {
        source.fire("iron-signal", {name:"show-issue-error-message", data: {
            issueKey: issueKey,
            errorMessage: message
        }});
    }

    this.init();
}

var taskboard = new Taskboard();

function IssueLocalState(issueKey) {
    this.isUpdating = false;
    this.errorMessage = null;
    this.getIssueKey = function() {
        return issueKey;
    }
    this.hasError = function() {
        return !_.isEmpty(this.errorMessage);
    }
}

function IssueLocalStateBuilder(issueKey) {
    var issueLocalState = new IssueLocalState(issueKey);
    return {
        isUpdating: function(isUpdating) {
            issueLocalState.isUpdating = isUpdating;
            return this;
        },
        errorMessage: function(errorMessage) {
            issueLocalState.errorMessage = errorMessage;
            return this;
        },
        build: function() {
            return issueLocalState;
        }
    }
}

function StepLocalState(idOfStep) {
    var stepId = idOfStep;
    this.isUpdating = false;
    this.getStepId = function() {
        return stepId;
    }
}

function StepLocalStateBuilder(idOfStep) {
    var stepLocalState = new StepLocalState(idOfStep);
    return {
        isUpdating: function(isUpdating) {
            stepLocalState.isUpdating = isUpdating;
            return this;
        },
        build: function() {
            return stepLocalState;
        }
    }
}

var buttonTypes = {
    BUTTON: 'button',
    LINK: 'link'
};

function ButtonBuilder(text) {
    var button = {
        text: text,
        type: buttonTypes.BUTTON,
        disabled: false,
        hidden: false,
        onClick: function() {
            throw '"onClick" callback needs to be implemented.';
        }
    };
    return {
        id: function(id) {
            button.id = id;
            return this;
        },
        classes: function(classes) {
            button.classes = classes;
            return this;
        },
        type: function(type) {
            button.type = type;
            return this;
        },
        disabled: function(disabled) {
            button.disabled = disabled;
            return this;
        },
        hidden: function(hidden) {
            button.hidden = hidden;
            return this;
        },
        onClick: function(callback) {
            button.onClick = callback;
            return this;
        },
        build: function() {
            return button;
        }
    }
}

function flash(el, color) {
    var original = el.css('backgroundColor');
    el.animate({backgroundColor:color},300).animate({backgroundColor:original},800)
}
