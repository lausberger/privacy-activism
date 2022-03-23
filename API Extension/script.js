var test = "success!";
var myCode = 'var inject = 4;';
//var script = document.createElement('script');

//script.textContent = myCode;
//(document.head||document.documentElement).appendChild(script);
//script.remove();

function insertScript(inline, data) {
	var script = document.createElement('script');
	script.setAttribute('id', '__lg_script');
	if(inline)
		script.appendChild(document.createTextNode(data));
	else
		script.setAttribute('src', data);

	// FF: there is another variables in the scope named parent, this causes a very hard to catch bug
	var _parent = document.head || document.body || document.documentElement;
	var firstChild = (_parent.childNodes && (_parent.childNodes.length > 0)) ? _parent.childNodes[0] : null;
	if(firstChild)
		_parent.insertBefore(script, firstChild);
	else
		_parent.appendChild(script);
}

insertScript(true, myCode);
insertScript(false, myCode);
