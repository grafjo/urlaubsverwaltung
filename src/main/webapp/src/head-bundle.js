window.$ = $;
window.jQuery = jQuery;
require('jquery-ui');
require('jquery-migrate');
require('timepicker');
require('tablesorter');
require('moment');
window._ = require('underscore');
require('list.js');
require('datejs');
require('bootstrap');



window.app = {};
window.app.custom = require('./js/custom.js');
window.app.action = require('./js/actions.js');
require('./js/back-button.js');
require('./js/feedback.js');
require('./js/sortable.js');
require('./js/textarea.js');
require('./js/popover.js');
require('./js/gravatar.js');