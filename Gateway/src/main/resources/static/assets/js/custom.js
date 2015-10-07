
// Update footer copyright with latest year
jQuery(document).ready(function() {

    var currentYear = new Date().getFullYear();

    jQuery('#copyrightDate').text(currentYear + ' \u00A9 All Rights Reserved.')
});

