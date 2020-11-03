$(function () {
	// To expand lists in sidebar
	$('#sessions_list span:first').on('click', function () {
		var icon = $(this).children('i')
		icon.toggleClass('fa-angle-right');
		icon.toggleClass('fa-angle-down');
		if (icon.hasClass('fa-angle-right'))
			$('#sessions_list').children('li').css('display', 'none');
		if (icon.hasClass('fa-angle-down'))
			$('#sessions_list').children('li').css('display', 'block');
	});
})