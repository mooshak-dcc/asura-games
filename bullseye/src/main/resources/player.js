/*
  Simple movie player. Frames have a single background and show sprites
  in different positions, with rotations and scaling


  				José Paulo Leal
				zp@dcc.fc.up.pt

   December 2016
 */

var filename          = "movie.json";    				// name of example movie
var logger            = undefined;      				// text area for logging messages
var player            = getParameterByName("playerId"); // id of player seeing watching the movie;
var canvas            = undefined;       				// canvas DOM object
var context           = undefined;       				// canvas's context
var background        = undefined;       				// movie background image
var sprites           = Object();    					// movie sprites
var spriteCoordinates = undefined;       				// position in sprite of its coordinates
var waitingFor        = undefined;       				// images still being loaded
var frames            = undefined;       				// movie frames
var framesPerSecond   = 1;               				// movie fps
var startTime         = undefined;       				// moment when movie start playing
var lastFrameIndex    = undefined;       				// last frame displayed

/*
  load movie and start playing it
 */
window.onload = function() {
	var input = document.getElementById("input");    
	var req = new XMLHttpRequest();

	req.onreadystatechange = function() {
		if (this.readyState !== 4)
			return;
			if(this.status !== 200) {
				throw req.statusText;
			}

		play(JSON.parse(req.responseText));

	};
	req.open("GET", filename, true);
	req.send();
};

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
   Start by loading images and setting global variables
 */
function play(movie) {
	var header = movie.header;

	if(header === undefined)
		throw "header missing in movie";

	logger = document.getElementById("logArea");
	canvas = document.getElementById("movieCanvas");
	context = canvas.getContext("2d");
	waitingFor = 0; 

	if(header.background === undefined)
		throw "background missing in movie header";

	waitingFor++;
	background = new Image();
	background.onload = function() { startPlaying(); };
	background.src = header.background;

	if(header.sprites === undefined)
		throw "spites missing in movie header";

	for(var id in header.sprites) {
		var image = new Image();

		waitingFor++;
		image.onload = function() { startPlaying(); };
		image.src = header.sprites[id];
		sprites[id] = image;
	}

	if(movie.frames === undefined)
		throw "frames missing in movie";
	frames = movie.frames;

	if(movie.header.fps === undefined)
		framesPerSecond = 1;
	else
		framesPerSecond = header.fps;
	
	anchor_point = header.anchor_point;
}

/**
   When all images are loaded set start time and continue playing
 */
function startPlaying() {

	if(--waitingFor === 0) {
		startTime	= new Date().getTime();
		lastFrameIndex 	= -1;

		continuePlaying();
	}
}

/**
   Compute current frame index. If not diplayed yet show it.
   Keep asking for more animation frames while there are frames to show.
 */
function continuePlaying() {
	var currentTime	= new Date().getTime();
	var frameIndex	= Math.floor((
			currentTime - startTime) * framesPerSecond / 1000);

	if(frameIndex > lastFrameIndex) {
		lastFrameIndex = frameIndex;
		if(frameIndex < frames.length)
			showFrame(frames[frameIndex]);
	}
	if(frameIndex < frames.length-1)
		window.requestAnimationFrame(continuePlaying);
}

/*
  Show a single frame: draw background and all sprites
 */
function showFrame(frame) {

	context.clearRect(0,0,canvas.width,canvas.height);
	context.drawImage(background,0,0);

	if(frame.items === undefined)
		throw "missing item in frame:"+lastFrameIndex;
	
	var messages = frame.messages[player];
	
	if(messages !== undefined) {
		logger.value += frame.messages[player];
		logger.scrollTop = logger.scrollHeight;
	}

	for(var i in frame.items) {
		var item = frame.items[i];
		var sprite = sprites[item.sprite];
		var msg = item.message;
		var posX = item.x;
		var posY = item.y;
		var relX, relY;
		
		if(sprite === undefined && msg === undefined)
			throw "unknown sprite with id:"+item.sprite;

		context.save();
		
		context.translate(item.x,item.y);
		if(item.scale !== undefined)
			context.scale(item.scale,item.scale);
		if(item.rotate !== undefined)
			context.rotate(item.rotate);
		if(sprite)
		{
			switch(anchor_point) {
			case "TOP":
				relX = -sprite.width/2;
				relY = 0;
				break;
			case "TOP_LEFT":
				relX = 0;
				relY = 0;
				break;
			case "TOP_RIGHT":
				relX = -sprite.width;
				relY = 0;
				break;
			case "LEFT":
				relX = 0;
				relY = -sprite.height/2;
				break;
			case "RIGHT":
				relX = -sprite.width;
				relY = -sprite.height/2;
				break;
			case "BOTTOM":
				relX = -sprite.width/2;
				relY = -sprite.height;
				break;
			case "BOTTOM_LEFT":
				relX = 0;
				relY = -sprite.height;
				break;
			case "BOTTOM_RIGHT":
				relX = -sprite.width;
				relY = -sprite.height;
				break;
			case "CENTER":
			default:
				relX = -sprite.width/2;
				relY = -sprite.height/2;
				break;	
			}
			
			context.drawImage(sprite,relX,relY);
		}
		if(msg)
		{
			context.font="40px Verdana";
			context.fillStyle = 'white';
			context.fillText(msg, posX, posY);
		}
		context.restore();
	}
}
