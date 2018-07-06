function NavigatorService(){
    var self = this;
    var PATH_CARD = '/#/card';

    self.openCard = function(issueKey){
        window.location.href = window.location.origin + PATH_CARD + "/" + issueKey;
    }

    var isOpenPath = function(path){
        var url = window.location.href;
        if(url.includes(path))
            return true;
        return false;
    }

    var getCardKey = function(){
        var matchCardRoute = new RegExp('#/card/(.*)').exec(window.location.hash);
        if (matchCardRoute)
            return matchCardRoute[1];
    }

    self.resetUrlWithouLoad = function(){
        var urlBase = window.location.origin;
        window.history.replaceState({}, null, urlBase);
    }

    var changeUrlWithouLoad = function(url){
        window.history.replaceState({}, null, url);
    }

    self.resolve = function(){
        var issuedetail = document.querySelector('issue-detail');
        if(isOpenPath(PATH_CARD)){
            var urlCard = window.location.href;
            var issueKey = getCardKey();
            var card = taskboard.getIssueByKey(issueKey);

            issuedetail.closeIfOpenedDialog();
            changeUrlWithouLoad(urlCard);

            if(card)
                return issuedetail.opendialog(card);

            this.resetUrlWithouLoad();
            taskboard.showError(issuedetail, 'Card '+ issueKey +' not found.');
        }else{
            issuedetail.closeIfOpenedDialog();
        }
    }
}

var navigatorService = new NavigatorService();

