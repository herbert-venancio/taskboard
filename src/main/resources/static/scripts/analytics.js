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
var _paq = _paq || [];
_paq.push(['enableLinkTracking']);

function Analytics() {

    var CD_USER = 1;

    this.init = function (trackingSite, siteId) {
        var self = this;
        (function () {
            var u = "//" + trackingSite + "/piwik/";
            _paq.push(['setTrackerUrl', u + 'piwik.php']);
            _paq.push(['setSiteId', siteId]);
            var d = document, g = d.createElement('script'), s = d.getElementsByTagName('script')[0];
            g.type = 'text/javascript';
            g.async = true;
            g.defer = true;
            g.src = u + 'piwik.js';
            s.parentNode.insertBefore(g, s);
        })();
        this.setUser(window.user.user);
    };

    this.setUser = function (user) {
        _paq.push(['setCustomDimension', CD_USER, user]);
    };

    this.trackView = function () {
        _paq.push(['trackPageView']);
    };

}

var analytics = new Analytics();
