#!/bin/bash

suren_game_cell_type_t_road=(
	0 0	1 0	-1 0	0 -1
	0 0	0 -1	0 1	-1 0
	0 0	0 -1	0 1	0 1
	0 0	0 1	0 -1	1 0
)

suren_game_cell_type_square=(
	0 0	1 0	1 1	0 1
	0 0	1 0	1 1	0 1
	0 0	1 0	1 1	0 1
	0 0	1 0	1 1	0 1
)

suren_game_cell_type_line=(
	-1 0	0 0	1 0	2 0
	0 1	0 0	0 -1	0 -2
	-1 0	0 0	1 0	2 0
	0 1	0 0	0 -1	0 -2
)

suren_game_cell_type_corner_l=(
	0 -2	0 -1	0 0	1 0
	-2 0	-1 0	0 0	0 -1
	0 2	0 1	0 0	-1 0
	2 0	1 0	0 0	0 1
)

suren_game_cell_type_corner_r=(
	0 -2	0 -1	0 0	-1 0
	-2 0	-1 0	0 0	0 1
	0 2	0 1	0 0	1 0
	2 0	1 0	0 0	0 -1
)

suren_game_cell_type_round_l=(
	0 -1	0 0	1 0	1 1
	-1 0	0 0	0 -1	1 -1
	0 1	0 0	-1 0	-1 -1
	1 0	0 0	0 1	-1 1
)

suren_game_cell_type_round_r=(
	1 -1	1 0	0 0	0 1
	-1 -1	0 -1	0 0	1 0
	-1 1	-1 0	0 0	0 -1
	1 1	0 1	0 0	-1 0
)

suren_lines=0
suren_cols=0
suren_control_zone_width=0
suren_render_speed=0
suren_log=

suren_points=()

suren_status_init=0
suren_status_begin=1
suren_status_pause=2
suren_status_end=3
suren_game_status=0

suren_all_cells=()
suren_last_cell=()
suren_last_preview_cell=()

suren_preview_index=0
suren_preview_shape_index=0

function suren_init(){
#	echo $PPID
#	echo $BASHPID
#	echo $BASH_LINENO
#	echo $BASH_SOURCE
#	echo $FUNCNAME
#	echo $LINENO
#	echo "`tput cols`"
#	echo "`tput lines`"

	suren_lines=`tput lines`
	suren_cols=`tput cols`
	
	suren_log="log"

	suren_control_zone_width=20
	suren_render_speed=0

	suren_game_status=$suren_status_init

	suren_preview_index=-1
	suren_preview_shape_index=-1

	suren_render
}

function prepare(){
	tput cols > /dev/null 2> /dev/null
	
	if ! [ "$?" == "0" ]
	then
		echo 1
	fi

	echo 0
}

function suren_render(){
	echo $FUNCNAME
	export PS1=''
	echo $FUNCNAME

	if ! [ "`prepare`" == "0" ]
	then
		echo "prepare failure.";

		exit 1
	fi

	tput clear

	lines=$suren_lines
	cols=$suren_cols

	game_zone_width=$(($cols-$suren_control_zone_width))

	line=0
	col=0
	while [ $line -lt $lines ]
	do
		tput cup $line 0
		echo -ne "*"

		tput cup $line $game_zone_width
		echo -ne "*"

		tput cup $line $cols
		echo -ne "*"

		sleep $suren_render_speed

		line=$(($line+1))
	done

	line=0
	col=0
	while [ $col -lt $cols ]
	do
		tput cup 0 $col
		echo -ne "*"

		tput cup $lines $col
		echo -ne "*"

		sleep $suren_render_speed

		col=$(($col+1))
	done

	tput cup $(($lines/2)) $(($cols/2))

	suren_event_register

	while [ 1 == 1 ]
	do
		read -s -n 1 code

		if [ "h" == "$code" ]
		then
			suren_move x -1
		elif [ "j" == "$code" ]
		then
			suren_move y 1
		elif [ "k" == "$code" ]
		then
			suren_move y -1
		elif [ "l" == "$code" ]
		then
			suren_move x 1
		elif [ "s" == "$code" ]
		then
			suren_begin
		elif [ "q" == "$code" ]
		then
			suren_quit
		fi
	done
}

function suren_event_register(){
	trap 'event_register' 10
	trap 'event_register' 12
	trap 'event_register' 16
	trap 'event_register' 30
	trap 'suren_pause' 31
}

function suren_begin(){
	if ! [ "$suren_game_status" == "$suren_status_begin" ]
	then
		suren_game_status=$suren_status_begin

		suren_shape_init
	fi
}

function suren_pause(){
	suren_game_status=$suren_status_pause
}

function suren_end(){
	suren_game_status=$suren_status_end
}

function suren_quit(){
	clear
	echo "you have quit."
	exit 0
}

function suren_move(){
	if ! [ "$#" == "2" ]
	then
		exit 1
	fi

	local direction=$1
	local step=$2
	local last_cell=(${suren_last_cell[@]})
	local size=${#last_cell[@]}
	local index=0

	while [ $index -lt $size ]
	do
		tput cup ${last_cell[$((index+1))]} ${last_cell[$index]}
		echo -ne " "

		index=$(($index+2))
	done

	index=0
	local tmp_x=0
	local tmp_y=0
	local tmp_last_cell=()
	if [ "$direction" == "x" ]
	then
		while [ $index -lt $size ]
		do
			tmp_x=${last_cell[$index]}
			tmp_y=${last_cell[$((index+1))]}

			tmp_x=$(($tmp_x+$step))

			#tput cup $tmp_y $tmp_x
			#echo -ne "#"

			tmp_last_cell=(${tmp_last_cell[@]} $tmp_x $tmp_y)

			index=$(($index+2))
		done
	elif [ "$direction" == "y" ]
	then
		while [ $index -lt $size ]
		do
			tmp_x=${last_cell[$index]}
			tmp_y=${last_cell[$((index+1))]}

			tmp_y=$(($tmp_y+$step))

			#tput cup $tmp_y $tmp_x
			#echo -ne "#"

			tmp_last_cell=(${tmp_last_cell[@]} $tmp_x $tmp_y)

			index=$(($index+2))
		done
	fi

	if [ "`suren_edge_detect ${tmp_last_cell[@]}`" == "2" ]
	then
		index=0
		while [ $index -lt $size ]
		do
			tmp_x=${tmp_last_cell[$index]}
			tmp_y=${tmp_last_cell[$((index+1))]}

			tput cup $tmp_y $tmp_x
			echo -ne "#"

			index=$(($index+2))
		done

		suren_last_cell=(${tmp_last_cell[@]})
	fi
}

#touch edge 0
#touch under_line 1
#touch empty 2
function suren_edge_detect(){
	if ! [ "$#" == "8" ]
	then
		echo 0
		return
	fi

	local i=0
	local next_cell=($@)
	local size=${#next_cell[@]}
	echo "suren_edge_detect : ${next_cell[@]}" >> $suren_log

	local tmp_x=0
	local tmp_y=0
	while [ $i -lt $size ]
	do
		tmp_x=${next_cell[$index]}
		tmp_y=${next_cell[$(($index+1))]}

		if [ $tmp_x -le 0 ] || [ $tmp_y -le 0 ]
		then
			echo 0
			return
		fi

		i=$(($i+2))
	done

	echo 2
}

function suren_point_collect(){
	if [ "" == "" ]
	then
		local i=0
	fi
}

function suren_shape_init(){
	if [ "$suren_all_cells" == "" ]
	then
		suren_all_cells=(
			${suren_game_cell_type_t_road[@]} 4
			${suren_game_cell_type_square[@]} 4
			${suren_game_cell_type_line[@]} 4
			${suren_game_cell_type_corner_l[@]} 4
			${suren_game_cell_type_corner_r[@]} 4
			${suren_game_cell_type_round_l[@]} 4
			${suren_game_cell_type_round_r[@]} 4
		)
	fi

	size=7
	unit=$((${#suren_all_cells[@]}/$size-1))
	unit=$(($unit/2/4))

	if [ "$suren_preview_index" == "" ] || [ "$suren_preview_index" == "-1" ]
	then
		suren_preview_index=$(($RANDOM%$size))
	fi

	if [ "$suren_preview_shape_index" == "" ] || [ "$suren_preview_shape_index" == "-1" ]
	then
		suren_preview_shape_index=$(($RANDOM%$unit))
	fi
	
	#game zone cells create
	suren_create_cells $suren_preview_index $suren_preview_shape_index 0 -1 4

	#cal preview shape index, random index
	suren_preview_index=$(($RANDOM%$size))
	suren_preview_shape_index=$(($RANDOM%$unit))

	#game control zone cells create
	suren_create_cells $suren_preview_index $suren_preview_shape_index $suren_control_zone_width 0 4
}

function suren_create_cells(){
	if ! [ "$#" == "5" ]
	then
		return
	fi

	if ! [ "$suren_game_status" == "$suren_status_begin" ]
	then
		exit 2
	fi

	index=$1
	shape_index=$2
	zone=$3
	preview=$4
	local tmp_size=$5

	x=$(($suren_cols-$suren_control_zone_width))
	if [ "$zone" == "0" ]
	then
		x=$(($x/2))
	else
		x=$(($x+$suren_control_zone_width/2))
	fi
	y=10

	local tmp_index=$(($index*33))
	local tmp_index=$(($shape_index*8+$tmp_index))
	last_cell=()

	suren_clean_last_cells $zone

	local index=0
	while [ $index -lt $tmp_size ]
	do
		x_value=${suren_all_cells[$tmp_index]}
		y_value=${suren_all_cells[$(($tmp_index+1))]}

		tmp_x=$(($x+$x_value))
		tmp_y=$(($y+$y_value))

		#tput cup $y $x
		tput cup $tmp_y $tmp_x
		#echo -ne "$x_value,$y_value,$tmp_index,$shape_index"
		echo -ne "#"

		last_cell=(${last_cell[@]} $tmp_x $tmp_y)

		index=$(($index+1))
		tmp_index=$(($tmp_index+2))
		#y=$(($y+1))
	done

	if [ "$zone" == "0" ]
	then
		suren_last_cell=${last_cell[@]}
	else
		suren_last_preview_cell=${last_cell[@]}
	fi
}

function suren_clean_last_cells(){
	if ! [ "$#" == "1" ]
	then
		return
	fi

	local zone=$1
	local last_cell=()

	if [ "$zone" == "0" ]
	then
		last_cell=(${suren_last_cell[@]})
	else
		last_cell=(${suren_last_preview_cell[@]})
	fi

	local index=0
	local tmp_size=${#last_cell[@]}
	while [ $index -lt $tmp_size ]
	do
		tput cup ${last_cell[$(($index+1))]} ${last_cell[$index]}

		echo -ne " "

		index=$(($index+2))
	done
}

function suren_test(){
	local abc="123123"
	abc="123123"
}

suren_init

#echo ${suren_game_cell_type_t_road[2]}

#suren_test
