if(typeof(suren) == "undefined"){
	suren = {};
}

if(typeof(suren.game) == "undefined"){
	suren.game = {};
}

if(typeof(suren.time_human) == "undefined"){
	suren.time_human = function(time){
		if(arguments.length < 1){
			return "";
		}

		var result = "";
		if(time > 3600000){
			var h = Math.floor(time / 3600000);
			h = Math.round(h * 100) / 100;
			result += h;
			result += "h";
			result += ", ";

			return result + suren.time_human(time - h * 3600000);
		}else if(time > 60000){
			var m = Math.floor(time / 60000);
			m = Math.round(m * 100) / 100;
			result += m;
			result += "m";
			result += ", ";

			return result + suren.time_human(time - m * 60000);
		}else{
			var s = Math.round(time / 1000 * 100) / 100;
			return s + "s";
		}
	}
}

suren.game.init = function(game_zone){
	if(!game_zone){
		return;
	}

	var game = suren.game;
	game.cell = 10;

	game.width = 20;
	game.control_panel_width = game.cell * 12;
	game.height = 30;

	game.zone = game_zone;
	game.background_color = "pink";
	game.cell_color = "black";

	game.status = {
		init : "init",
		begin : "begin",
		pause : "pause",
		end : "end"
	};

	game.level = 0;
	game.level_unit = 10;
	game.speed = 1000;
	game.score = 0;
	game.cell_num = 0;
	game.time = 0;
	game.last_time = new Date();
	game.status.status = game.status.init;
	game.preview = {};

	game.render();
}

suren.game.level_update = function(){
	var score = suren.game.score;
	
	suren.game.level = Math.floor(score / 10);
}

suren.game.render = function(){
	var game = suren.game;
	var zone = game.zone;
	var jq_zone = $(zone);

	if(!zone){
		console.warn("not found game zone.");
	}

	jq_zone.css("background-color", game.background_color);
	jq_zone.css("width", game.width * game.cell)
	jq_zone.css("height", game.height * game.cell);
	jq_zone.css("position", "absolute");
	jq_zone.css("left", "10");
	jq_zone.css("top", "10");
	jq_zone.css("margin", "0");
	jq_zone.css("padding", "0");

	jq_zone.html("");

	game.leftEdge = parseInt(jq_zone.css("margin-left"));
	game.rightEdge = parseInt(jq_zone.css("width")) - game.cell;
	game.rightEdge += game.leftEdge;
	game.bottomEdge = parseInt(jq_zone.css("height")) - game.cell;

	game.zone.points = {};

	game.create_control_panel();

	game.event_register();
}

suren.game.create_control_panel = function(){
	var jq_zone = $(suren.game.zone);

	var control_panel = document.createElement("span");
	var jq_control_panel = $(control_panel);
	var height = parseInt(jq_zone.css("height"));
	var control_left = suren.game.zone.offsetWidth + suren.game.zone.offsetLeft;

	control_left += 10;

	jq_control_panel.css("position", "absolute");
	jq_control_panel.css("left", control_left);
	jq_control_panel.css("top", jq_zone.css("top"));
	jq_control_panel.css("width", suren.game.control_panel_width);
	jq_control_panel.css("height", height);

	jq_zone.after(control_panel);

	var preview = document.createElement("div");
	var jq_preview = $(preview);
	jq_preview.css("height", height * 0.5);
	jq_control_panel.append(preview);

	suren.game.preview_zone = preview;

	var score = document.createElement("div");
	var jq_score = $(score);
	jq_score.css("height", 0.1 * height);
	jq_score.css("line-height", jq_score.css("height"));
	jq_score.css("float", "center");
	jq_control_panel.append(score);

	suren.game.update_score = function(){
		jq_score.html("scroe : " + suren.game.score);
	};
	suren.game.update_score();

	var level = document.createElement("div");
	var jq_level = $(level);
	jq_level.css("height", 0.1 * height);
	jq_level.css("line-height", jq_level.css("height"));
	jq_control_panel.append(level);

	suren.game.update_level = function(){
		jq_level.html("level : " + suren.game.level);
	};
	suren.game.update_level();

	var time = document.createElement("div");
	var jq_time = $(time);
	jq_time.css("height", 0.1 * height);
	jq_time.css("line-height", jq_time.css("height"));
	jq_time.css("overflow", "hidden");
	jq_time.html("time");
	jq_control_panel.append(time);

	suren.game.update_time = function(){
		jq_time.html("time : " + suren.time_human(suren.game.time));
	};
	suren.game.update_time();

	var control = document.createElement("div");
	var jq_control = $(control);
	jq_control.css("height", 0.2 * height);
	jq_control.css("line-height", jq_control.css("height"));
	jq_control.css("text-align", "center");
	jq_control.attr("start", "start");
	jq_control.attr("pause", "pause");
	jq_control.html(jq_control.attr("start"));
	jq_control.click(function(){
		if(suren.game.status.status == suren.game.status.begin){
			suren.game.pause();
			jq_control.html(jq_control.attr("start"));
		}else if(suren.game.status.status == suren.game.status.pause
			|| suren.game.status.status == suren.game.status.init){
			suren.game.begin();	
			jq_control.html(jq_control.attr("pause"));
		}
	});
	jq_control_panel.append(control);
}

suren.game.event_register = function(){
	var game = suren.game;
	var zone = game.zone;
	var jq_zone = $(zone);

	$(document).keypress(function(e){
		var active_cell = game.active_cell;
		if(!active_cell || game.status.status != game.status.begin){
			return;
		}

		var code = e.keyCode;
		var invoke;
		console.log("code : " + code);

		switch(code){
			case 0:
				var num = game.cell_num;
				while(num == game.cell_num){
					active_cell[0].down();
				}
				break;
			case 37:
				invoke = active_cell[0].left;
				break;
			case 38:
				invoke = active_cell[0].rotation;
				break;
			case 39:
				invoke = active_cell[0].right;
				break;
			case 40:
				invoke = active_cell[0].down;
				break;
			case 116:
				return false;
				break;
			default:
				return false;
				break;
		}

		if(invoke && typeof(invoke) == "function"){
			invoke();
		}
	});
}

suren.game.begin = function(){
	var status = suren.game.status.status;

	suren.game.status.status = suren.game.status.begin;
	
	if(status == suren.game.status.init){
		suren.game.create_cells();
	}

	suren.game.last_time = new Date();
}

suren.game.pause = function(){
	suren.game.status.status = suren.game.status.pause;
}

suren.game.over = function(){
	suren.game.status.status = suren.game.status.end;
}

suren.game.cell_type = function(cell){
	if(!cell){
		return;
	}

	var jq_cell = $(cell);
	var game = suren.game;
	var type = {};

	type.left = function(){
		type.move("left", -game.cell);
	};

	type.right = function(){
		type.move("left", game.cell);
	};
	
	type.down = function(){
		type.move("top", game.cell);
	};

	type.rotation = function(){
		var active_cell = game.active_cell;
		var center = {};
		for(var i in active_cell){
			var cell = active_cell[i];
			if(cell.x == 0 && cell.y == 0){
				center = cell;
				break;
			}
		}

		var jq_center = $(center);
		console.log(jq_center.css("left") + "=========rotation=======" + parseInt(jq_center.css("top")));
		var cell_move = [];

		var index = center.index;
		var shape_index = center.shape_index;

		var cell_type = suren.game.all_types[index];
		var data = cell_type.data;
		var unit = cell_type.unit;

		if(++shape_index >= unit){
			shape_index = 0
		}

		console.log("shape_index : " + shape_index);

		var len = (shape_index + 1) * unit;
		for(var i = shape_index * unit; i < len; i++){
			var cell = data[i];
			var x = parseInt(jq_center.css("left")) + cell.x * game.cell;
			var y = parseInt(jq_center.css("top")) + cell.y * game.cell;

			if(type.illegal(x, y)){
				return;
			}

			cell_move[cell_move.length] = {
				id : cell.id,
				x : x,
				y : y
			};
		}

		len = active_cell.length;
		for(var i  = 0; i < len; i++){
			var move = cell_move[i];
			var jq_cell = $("#" + active_cell[i].id);

			console.log(jq_cell.css("left") + "--" + move.x);
			jq_cell.css("left", move.x);
			jq_cell.css("top", move.y);

			jq_cell.get(0).shape_index = shape_index;
		}
	};

	type.move = function(direct, path){
		if(suren.game.status.status != suren.game.status.begin){
			return;
		}

		type.time_check();

		var active_cell = game.active_cell;
		var cell_path = {};
		for(var i in active_cell){
			var cell = active_cell[i];
			var jq_cell = $(cell);

			var touch_path = parseInt(jq_cell.css(direct)) + path;
			if(type.touch_edge(direct, touch_path, true)){
				return;
			}

			var x = 0;
			var y = 0;

			if(direct == "left"){
				x = touch_path / game.cell;
				y = parseInt(jq_cell.css("top")) / game.cell;
			}else if(direct == "top"){
				x = parseInt(jq_cell.css("left")) / game.cell;
				y = touch_path / game.cell;
			}else{
				return;
			}

			if(type.touch_point(x, y)){
				if("top" == direct){
					type.collect_point();
				}
				return;
			}

			cell_path[cell.id] = touch_path;
		}

		for(var i in cell_path){
			$("#" + i).css(direct, cell_path[i]);
		}
	}

	type.time_check = function(){
		var spend = new Date().getTime() - suren.game.last_time.getTime();
		suren.game.time += spend;

		suren.game.last_time = new Date();

		suren.game.update_time();
	}

	type.collect_point = function(){
		var id = game.interval_id;
		if(id == ""){
			return;
		}

		game.interval_id = "";

		var new_points = [];
		var active_cell = game.active_cell;
		for(var i in active_cell){
			var ac = active_cell[i];
			var jq_ac = $(ac);

			var x = parseInt(jq_ac.css("left"));
			var y = parseInt(jq_ac.css("top"));

			if(typeof(game.zone.points["" + y]) == "undefined"){
				game.zone.points["" + y] = [];
			}

			var new_point = {
				x : x / game.cell,
				y : y / game.cell
			};

			game.zone.points["" + y][game.zone.points["" + y].length] = new_point;

			new_points[new_points.length] = new_point;
		}

		game.cell_num++;
		clearInterval(id);

		type.clean_point(new_points);

		game.create_cells();
	}

	type.clean_point = function(new_points){
		var points = game.zone.points;
		var new_y = {};

		for(var i in new_points){
			var point = new_points[i];

			new_y[String(point.y)] = point.y;
		}

		var new_yy = [];
		for(var i in new_y){
			new_yy[new_yy.length] = new_y[i];
		}
		new_yy.sort();

		for(var i in new_yy){
			if(new_yy[i] == 0){
				game.status.status = game.status.end;
				return;
			}

			var y = new_yy[i] * game.cell;
			if(typeof(points["" + y]) == "undefined"){
				continue;
			}

			var len = points["" + y].length;
			console.log("clean ----- " + y);
			if(len == game.width){
				var count = 0;
				var children = $(game.zone).children();
				var ids = [];
				console.log(children.size() + "---");
				for(var j = 0; j < children.size(); j++){
					if(count >= game.width){
						break;
					}

					var cell = children.get(j);
					var jq_cell = $(cell);
					if(parseInt(jq_cell.css("top")) == y){
						ids[ids.length] = cell.id;
						count++;
					}
				}

				console.log("prepare to remove");
				console.log(ids);
				console.log("prepare to remove");
				for(var j in ids){
					var jq_cell = $("#" + ids[j]);
					jq_cell.remove();
				}

				try{
				children = $(game.zone).children();
				console.log(children.size() + "---");
				for(var j = 0; j < children.size(); j++){
					try{
					var cell = children.get(j);
					if(typeof(cell) == "undefined"){
						continue;
					}
					var jq_cell = $(cell);
					var cell_y = parseInt(jq_cell.css("top"));
					if(cell_y < y){
						jq_cell.css("top", cell_y + game.cell);
					}
					}catch(e){console.log(e);}
				}

				var increase_ids = [];
				for(var j in points){
					if(j < y){
						increase_ids[increase_ids.length] = j;
					}
				}
				increase_ids.sort().reverse();
				console.log("------increase_ids-----" + y);
				console.log(increase_ids);
				console.log(points);
				delete points["" + y];
				console.log(points);
				console.log("------increase_ids-----");

				for(var j in increase_ids){
					var increase_id = increase_ids[j];
					var new_ps = points["" + increase_id];
					var new_ps = new_ps.slice(0, new_ps.length);
					for(var p in new_ps){
						new_ps[p].y++;
					}

					delete points["" + increase_id];
					points["" + (parseInt(increase_id) + game.cell)] = new_ps;
				}
				}catch(e){console.log(e);}

				suren.game.score++;
				suren.game.update_score(suren.game.score);
			}
		}
	}

	type.illegal = function(x, y){
		var game = suren.game;
		if(x < 0 || x >= game.cell * game.width || y >= game.cell * game.height){
			return true;
		}

		var points = game.zone.points;
		points = points["" + y];
		if(typeof(points) != "undefined"){
			for(var i in points){
				var point = points[i];
				if(point.x * game.cell == x){
					return true;
				}
			}
		}

		return false;
	}

	type.touch_edge = function(direct, path, collect){
		if(direct == "left"){
			if(path < game.leftEdge || path > game.rightEdge){
				return true;
			}
		}else if(direct == "top" && path > game.bottomEdge){
			if(collect){
				type.collect_point();
			}

			return true;
		}

		return false;
	}

	type.touch_point = function(x, y){
		var points = game.zone.points;
		for(var i in points){
			var len = points[i].length;
			for(var j = 0; j < len; j++){
				if(x == points[i][j].x && y == points[i][j].y){
					return true;
				}
			}
		}

		return false;
	}

	if(typeof(game.interval_id) == "undefined"){
		game.interval_id = {};
	}

	var x = game.width / 2 * game.cell + game.leftEdge;
	var y = 0;
	if(parseInt(jq_cell.css("left")) == x && parseInt(jq_cell.css("top")) == y){
		game.interval_id = window.setInterval(function(){
			type.down();
		}, suren.game.speed - suren.game.level * suren.game.level_unit);
	}

	return type;
}

suren.game.cell_type_t_road = [
{x : 0, y : 0}, {x : 1, y : 0}, {x : -1, y : 0}, {x : 0, y : -1},
{x : 0, y : 0}, {x : 0, y : -1}, {x : 0, y : 1}, {x : -1, y : 0},
{x : 0, y : 0}, {x : -1, y : 0}, {x : 1, y : 0}, {x : 0, y : 1},
{x : 0, y : 0}, {x : 0, y : 1}, {x : 0, y : -1}, {x : 1, y : 0}
];

suren.game.cell_type_square = [
{x : 0,	y : 0}, {x : 1,y : 0}, {x : 1, y : 1}, {x : 0, y : 1},
{x : 0,	y : 0}, {x : 1,y : 0}, {x : 1, y : 1}, {x : 0, y : 1},
{x : 0,	y : 0}, {x : 1,y : 0}, {x : 1, y : 1}, {x : 0, y : 1},
{x : 0,	y : 0}, {x : 1,y : 0}, {x : 1, y : 1}, {x : 0, y : 1}
];

suren.game.cell_type_line = [
{x : -1, y : 0}, {x : 0, y : 0}, {x : 1, y : 0}, {x : 2, y : 0},
{x : 0, y : 1}, {x : 0, y : 0}, {x : 0, y : -1}, {x : 0, y : -2},
{x : -1, y : 0}, {x : 0, y : 0}, {x : 1, y : 0}, {x : 2, y : 0},
{x : 0, y : 1}, {x : 0, y : 0}, {x : 0, y : -1}, {x : 0, y : -2}
];

suren.game.cell_type_corner_l = [
{x : 0, y : -2}, {x : 0, y : -1}, {x : 0, y : 0}, {x : 1, y : 0},
{x : -2, y : 0}, {x : -1, y : 0}, {x : 0, y : 0}, {x : 0, y : -1},
{x : 0, y : 2}, {x : 0, y : 1}, {x : 0, y : 0}, {x : -1, y : 0},
{x : 2, y : 0}, {x : 1, y : 0}, {x : 0, y : 0}, {x : 0, y : 1}
];

suren.game.cell_type_corner_r = [
{x : 0, y : -2}, {x : 0, y : -1}, {x : 0, y : 0}, {x : -1, y : 0},
{x : -2, y : 0}, {x : -1, y : 0}, {x : 0, y : 0}, {x : 0, y : 1},
{x : 0, y : 2}, {x : 0, y : 1}, {x : 0, y : 0}, {x : 1, y : 0},
{x : 2, y : 0}, {x : 1, y : 0}, {x : 0, y : 0}, {x : 0, y : -1}
];

suren.game.cell_type_round_l = [
{x : 0, y : -1}, {x : 0, y : 0}, {x : 1, y : 0}, {x : 1, y : 1},
{x : -1, y : 0}, {x : 0, y : 0}, {x : 0, y : -1}, {x : 1, y : -1},
{x : 0, y : 1}, {x : 0, y : 0}, {x : -1, y : 0}, {x : -1, y : -1},
{x : 1, y : 0}, {x : 0, y : 0}, {x : 0, y : 1}, {x : -1, y : 1}
];

suren.game.cell_type_round_r = [
{x : 1, y : -1}, {x : 1, y : 0}, {x : 0, y : 0}, {x : 0, y : 1},
{x : -1, y : -1}, {x : 0, y : -1}, {x : 0, y : 0}, {x : 1, y : 0},
{x : -1, y : 1}, {x : -1, y : 0}, {x : 0, y : 0}, {x : 0, y : -1},
{x : 1, y : 1}, {x : 0, y : 1}, {x : 0, y : 0}, {x : -1, y : 0}
];

suren.game.create_cell = function(zone, index, shape_index, unit, preview){
	var game = suren.game;
	var jq_zone = $(zone);

	if(arguments.length != 5){
		return;
	}

	var data = suren.game.all_types[index].data;
	var leftEdge = parseInt(jq_zone.css("margin-left"));
	var topEdge = 0;
	game.active_cell = [];

	if(preview){
		var children = jq_zone.children();
		var size = children.size();
		for(var i = 0; i < size; i++){
			$(children.get(i)).remove();
		}

		topEdge = game.cell * 3;
	}

	var len = (shape_index + 1) * unit;
	console.log(shape_index * unit + "---" + len);
	for(var i = shape_index * unit; i < len; i++){
		var offsetLeft = parseInt(jq_zone.css("width")) / 2 + leftEdge;
		var x = game.cell * data[i].x + offsetLeft;
		var y = game.cell * data[i].y + topEdge;

		var cell = document.createElement("span");
		cell.id = Math.random();
		cell.id = String(cell.id).substring(2);
		var jq_cell = $(cell);

		jq_cell.css("background-color", game.cell_color);
		jq_cell.css("width", game.cell).css("height", game.cell);
		jq_cell.css("position", "absolute");
		jq_cell.css("left", x);
		jq_cell.css("top", y);
		jq_cell.css("margin", "0");
		jq_cell.css("padding", "0");
		jq_cell.css("border", "1px solid yellow");

		if(!preview){
			var cell_type = game.cell_type(cell);

			cell.left = cell_type.left;
			cell.right = cell_type.right;
			cell.down = cell_type.down;
			cell.rotation = cell_type.rotation;
			cell.index = index;
			cell.shape_index = shape_index;
			cell.x = data[i].x;
			cell.y = data[i].y;
			console.log("x : " + x + "; y : " + y);

			game.active_cell[game.active_cell.length] = cell;
		}else{
			//jq_cell.show("slow");
			jq_cell.slideDown("normal");
		}

		jq_zone.append(jq_cell);
	}
}

suren.game.all_types = [];

suren.game.create_cells = function(){
	var game = suren.game;
	if(game.status.status != game.status.begin){
		return;
	}

	if(game.all_types.length == 0){
		game.all_types = [
			{data : game.cell_type_t_road, unit : 4},
			{data : game.cell_type_square, unit : 4},
			{data : game.cell_type_line, unit : 4},
			{data : game.cell_type_corner_l, unit : 4},
			{data : game.cell_type_corner_r, unit : 4},
			{data : game.cell_type_round_l, unit : 4},
			{data : game.cell_type_round_r, unit : 4}
		];
	}

	var all_types = game.all_types;
	var index = Math.floor(Math.random() * all_types.length);

	if(suren.game.preview){
		if(typeof(suren.game.preview.index) == "undefined"){
			suren.game.preview.index = index;
			index = Math.floor(Math.random() * all_types.length);
		}else{
			var tmp_index = suren.game.preview.index;
			suren.game.preview.index = index;
			index = tmp_index;
		}
	}

	var type = all_types[index];
	var data = type.data;
	var unit = type.unit;
	var shape_index = Math.floor(Math.random() * (data.length / unit));

	if(suren.game.preview){
		if(typeof(suren.game.preview.shape_index) == "undefined"){
			suren.game.preview.shape_index = shape_index;
			suren.game.preview.unit = unit;
			shape_index = Math.floor(Math.random() * (data.length / unit));
		}else{
			var tmp_shape_index = suren.game.preview.shape_index;
			suren.game.preview.shape_index = shape_index;
			shape_index = suren.game.preview.shape_index;

			var tmp_unit = suren.game.preview.unit;
			suren.game.preview.unit = unit;
			unit = suren.game.preview.unit;
		}
	}

	console.log(index + "---" + shape_index + "---" + type.unit + "---" + data.length);

	if(typeof(suren.game.preview.index) != "undefined"
		&& typeof(suren.game.preview.shape_index) != "undefined"){
		suren.game.create_cell(game.preview_zone, game.preview.index,
			game.preview.shape_index, game.preview.unit, true);
	}

	suren.game.create_cell(game.zone, index, shape_index, unit, false);
}

$(function(){
	var jq_game_zone = $("[game=]");

	if(jq_game_zone.size() > 0){
		suren.game.init(jq_game_zone.get(0));
	}
});
