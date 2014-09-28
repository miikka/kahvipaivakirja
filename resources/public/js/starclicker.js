(function($) {
    function starclicker($root) {
	var $input = $root.children("input");
	$root.children('span').click(function(ev, target) {
	    var value = parseInt($(this).data('st-value'));
	    $root.children('span').each(function(idx, el) {
		$el = $(el);
		$el.removeClass("glyphicon-star glyphicon-star-empty");
		if (parseInt($el.data('st-value')) <= value) {
		    $el.addClass('glyphicon-star');
		} else {
		    $el.addClass('glyphicon-star-empty');
		}
	    });
	    $input.val(value);
	});;
    }

    $(function() {
	$(".js-starclicker").each(function(_, el) { starclicker($(el)); });
    });
})(jQuery);
