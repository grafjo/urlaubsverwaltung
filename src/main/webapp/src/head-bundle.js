window.$ = $;
window.jQuery = jQuery;
//require('jquery-ui');
require('jquery-ui/ui/version');
require('jquery-ui/ui/widgets/datepicker');
require('jquery-migrate');
require('timepicker');
require('tablesorter');
window._ = require('underscore');
require('list.js');
require('datejs');
require('bootstrap');

window.app = {};
window.app.custom = require('./js/custom.js');
window.app.action = require('./js/actions.js');
require('./js/polyfills');
require('./js/back-button.js');
require('./js/feedback.js');
require('./js/sortable.js');
require('./js/textarea.js');
require('./js/popover.js');
require('./js/gravatar.js');
//here we have a order dependence we should solve it through require
require('./js/calendar.js');
require('./js/calendarInit.js');
require('./js/navbarInit.js');