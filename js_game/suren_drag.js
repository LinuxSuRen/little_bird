if(typeof(suren) == "undefined"){
	suren = {};
}

if(typeof(suren.drag) == "undefined"){
	suren.drag = {};
}

suren.drag.event_register = function(ele){
	var jq_ele = $(ele);

	jq_ele.css("-moz-user-select", "none");

	jq_ele.mousedown(function(e){
		this.attributes.down = "true";
		this.attributes.lastX = e.clientX;
		this.attributes.lastY = e.clientY;

		jq_ele.css("cursor", "move");
		jq_ele.css("position", "absolute");
		jq_ele.css("z-index", "1");
	});

	jq_ele.mouseup(function(){
		this.attributes.down = "false";

		jq_ele.css("cursor", "pointer");
		jq_ele.css("z-index", "auto");
	});

	jq_ele.mouseover(function(){
		jq_ele.css("cursor", "pointer");
	});

	jq_ele.mouseout(function(){
		this.attributes.down = "false";

		jq_ele.css("cursor", "default");
		jq_ele.css("z-index", "auto");
	});

	jq_ele.mousemove(function(e){
		var allow_move = (ele.attributes.down == "true");
		var x = e.clientX - ele.attributes.lastX;
		var y = e.clientY - ele.attributes.lastY;

		ele.attributes.lastX = e.clientX;
		ele.attributes.lastY = e.clientY;

		if(!allow_move){
			return;
		}

		if(ele.offsetLeft + ele.offsetWidth - e.clientX <= 5){
			if(jq_ele.attr("resize") == "true"){
				ele.style.cursor = "crosshair";
				ele.style.width = ele.offsetWidth + x;
				ele.style.height = ele.offsetHeight + y;
			}
		}else{
			ele.style.cursor = "pointer";
			var marginLeft = parseInt(jq_ele.css("marginLeft"));
			var marginTop = parseInt(jq_ele.css("marginTop"));

			ele.style.left = ele.offsetLeft + x - marginLeft;
			ele.style.top = ele.offsetTop + y - marginTop;
		}
	});
}

$(function(){
	$("*[drag=true]").each(function(){
		suren.drag.event_register(this);
	});
});
