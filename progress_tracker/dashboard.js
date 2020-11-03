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

	var iframe = document.getElementsByTagName('iframe')[0];

	// For iframe editable
	iframe.contentDocument.body.contentEditable = "true";
	// For integrating mathjax
	var math_script = iframe.contentDocument.createElement('script');
	math_script.type = 'text/javascript';
	math_script.src = 'https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-MML-AM_CHTML';
	math_script.async = true;
	iframe.contentDocument.head.appendChild(math_script);
	math_script.onload = function () {
		// For configuring Latex
		iframe.contentWindow.MathJax.Hub.Config({
			tex2jax: {
				inlineMath: [['$', '$']], // for allowing use of $
				processEscapes: true, // for interpreting \$ as literal $
				preview: '[math]', // text to display while processing
				skipTags: ["script", "noscript", "style", "pre", "code"], //for ignoring these tags
			},
			showProcessingMessages: false,
			messageStyle: 'none',
			jax: ['input/TeX', 'output/SVG']
			// Use MathJax.Hub.Typeset() to re-run typesetting
		});
	};
	function toggleTypeset() {
		var HTML = iframe.contentWindow.MathJax.HTML, jax = iframe.contentWindow.MathJax.Hub.getAllJax();
		for (var i = 0, m = jax.length; i < m; i++) {
			var script = jax[i].SourceElement(), tex = jax[i].originalText;
			if (script.type.match(/display/)) { tex = "\\[" + tex + "\\]" } else { tex = "$" + tex + "$" }
			jax[i].Remove();
			var preview = script.previousSibling;
			script.parentNode.removeChild(preview);
			preview = document.createTextNode(tex);
			script.parentNode.insertBefore(preview, script);
			script.parentNode.removeChild(script);
		}
	}
	var mathjax_show = false;
	$('#preview_latex').on('click', function () {
		if (mathjax_show) {
			mathjax_show = false;
			$('#preview_latex').css({ 'background': 'springgreen', 'color': 'black' });
			$('#preview_latex').html('Preview LaTeX');
			toggleTypeset();
		}
		else {
			mathjax_show = true;
			$('#preview_latex').css({ 'background': '#F44336', 'color': 'floralwhite' });
			$('#preview_latex').html('View Code');
			iframe.contentWindow.MathJax.Hub.Typeset();
		}
	});
	
	var font_css = iframe.contentDocument.createElement('link');
	font_css.rel = 'stylesheet';
	font_css.href = 'https://fonts.googleapis.com/css?family=Quicksand:400,700';
	iframe.contentDocument.head.appendChild(font_css);
	iframe.contentDocument.body.style.fontFamily = 'quicksand';
	
	// for iframe resize
	iframe.contentDocument.body.style.overflowY = 'hidden';
	iframe.contentDocument.body.style.whiteSpace = 'pre-wrap';
	iframe.contentDocument.body.style.wordWrap = 'break-word';
	iframe.contentDocument.execCommand('styleWithCSS', false, false);
	iframe.contentDocument.body.spellcheck = false;
	function resize() {
		var top = document.getElementById('site').scrollTop;
		var left = document.getElementById('site').scrollLeft;
		iframe.height = 'auto';
		iframe.height = iframe.contentDocument.body.scrollHeight + 'px';
		document.getElementById('site').scrollTo(left, top);
	}
	function delayedResize() {
		setTimeout(resize, 0);
	}
	iframe.contentDocument.body.addEventListener('cut', delayedResize);
	iframe.contentDocument.body.addEventListener('keydown', delayedResize);
	iframe.contentDocument.body.addEventListener('drop', function (e) {
		var data = e.dataTransfer.getData("text/plain");
		iframe.contentDocument.body.focus();
		console.log(iframe.contentDocument.execCommand('insertText', false, data)); 
		e.preventDefault();
		delayedResize();
	});

	iframe.contentDocument.body.addEventListener('paste', function (e) {
		e.preventDefault();

		var pastedText = undefined;
		if (window.clipboardData && window.clipboardData.getData) { // IE
			pastedText = window.clipboardData.getData('Text');
		} else if (e.clipboardData && e.clipboardData.getData) {
			pastedText = e.clipboardData.getData('text/plain');
		}
		// pastedText = pastedText.replace(/[^\x00-\x7F]/gm, "");
		prevent_paste = false;
		iframe.contentDocument.execCommand('insertText', false, pastedText);
		delayedResize();
		return true;
	});


	// For editor buttons
	var commands = ['bold', 'italic', 'strikethrough', 'underline', 'insertOrderedList', 'insertUnorderedList', 'indent', 'outdent', 'superscript', 'subscript', 'justifyFull', 'justifyLeft', 'justifyRight', 'justifyCenter'];
	function click_events(command) {
		return function () {
			iframe.contentDocument.execCommand(command, false, null);
			iframe.contentWindow.document.body.focus();
			check_formatting();
		}
	}
	for (var i = 0; i < commands.length; i++) {
		$('.' + commands[i]).on('click', click_events(commands[i]));
	}

	// For changing colors of editor bar
	function check_formatting() {
		var commands = ['bold', 'italic', 'strikethrough', 'underline', 'insertOrderedList', 'insertUnorderedList', 'indent', 'outdent', 'superscript', 'subscript', 'justifyFull', 'justifyLeft', 'justifyRight', 'justifyCenter'];
		for (var i = 0; i < commands.length; i++) {
			var elements = document.getElementsByClassName(commands[i]);
			if (iframe.contentDocument.queryCommandState(commands[i])) {
				for (var j = 0; j < elements.length; j++) {
					elements[j].style.color = 'deepskyblue';
				}
			}
			else for (var j = 0; j < elements.length; j++) {
				elements[j].style.color = 'black';
			}
		}
	}
	// To change colors of editor bar
	var things_to_check_for_editor = ['keydown', 'cut', 'paste', 'drop', 'click'];
	for (var i = 0; i < things_to_check_for_editor.length; i++) {
		iframe.contentDocument.body.addEventListener(things_to_check_for_editor[i], function () { setTimeout(check_formatting, 0) });
	}
})