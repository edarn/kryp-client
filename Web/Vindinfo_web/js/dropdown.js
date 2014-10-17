var DDSPEED = 10;
var DDTIMER = 15;

var mouseX;
var mouseY;

var IE = document.all?true:false

if (!IE) document.captureEvents(Event.MOUSEMOVE)

var MAX_OPACITY_SHOW = 1;
var FADE_MULTIPLIER = 0.008;

if(IE) {
    MAX_OPACITY_SHOW *= 10;
    FADE_MULTIPLIER *= 10;
}

var mouseOut;
var currentComponent;
var fadeValue;

var compId;

// Set-up to use getMouseXY function onMouseMove
document.onmousemove = getXY;

// main function to handle the mouse events //
function ddMenu(id,dir) {
  var head = document.getElementById(id + '-ddheader');
  var cont = document.getElementById(id + '-ddcontent');
  clearInterval(cont.timer);
  if(dir == 1) {
    clearTimeout(head.timer);
    if(cont.maxh && cont.maxh <= cont.offsetHeight) {
      return;
    } else if(!cont.maxh) {
      cont.style.display = 'block';
      cont.style.height = 'auto';
      cont.maxh = cont.offsetHeight;
      cont.style.height = '0px';
    }
    cont.timer = setInterval("ddSlide('" + id + "-ddcontent', 1)", DDTIMER);
  } else {
    head.timer = setTimeout('ddCollapse(\'' + id + '-ddcontent\')', 50);
  }
}

// collapse the menu //
function ddCollapse(id) {
  var cont = document.getElementById(id);
  cont.timer = setInterval("ddSlide('" + id + "', -1)", DDTIMER);
}

// cancel the collapse if a user rolls over the dropdown content //
function cancelHide(id) {
  var head = document.getElementById(id + '-ddheader');
  var cont = document.getElementById(id + '-ddcontent');
  clearTimeout(head.timer);
  clearInterval(cont.timer);
  if(cont.offsetHeight < cont.maxh) {
    cont.timer = setInterval("ddSlide('" + id + "-ddcontent', 1)", DDTIMER);
  }
}

// incrementally expand/contract the dropdown and change the opacity //
function ddSlide(id,dir) {
  var cont = document.getElementById(id);
  var currheight = cont.offsetHeight;
  var dist;
  if(dir == 1) {
    dist = (Math.round((cont.maxh - currheight) / DDSPEED));
  } else {
    dist = (Math.round(currheight / DDSPEED));
  }
  if(dist <= 1 && dir == 1) {
    dist = 1;
  }
  cont.style.height = currheight + (dist * dir) + 'px';
  cont.style.opacity = currheight / cont.maxh;
  cont.style.filter = 'alpha(opacity=' + (currheight * 100 / cont.maxh) + ')';
  if((currheight < 2 && dir != 1) || (currheight > (cont.maxh - 2) && dir == 1)) {
    clearInterval(cont.timer);
  }
}

function show(controlId, timeout, setPos) {
    mouseOut = false;
    currentComponent = controlId;
    var control = document.getElementById(controlId);
    if(control.style.visibility == 'hidden' || control.style.visibility == '') {
        setTimeout(function(){showAfterDelay(controlId, setPos); parameter = null}, timeout);
    } else { /* Skip the fading */
        clearInterval(compId);
        control.style.opacity = MAX_OPACITY_SHOW;
    	control.style.filter = 'alpha(opacity=' + MAX_OPACITY_SHOW*10 + ')';
    }
}

function fadeIn(controlId) {
    if(controlId != currentComponent) {
        clearInterval(compId);
        return;    
    }
    var control = document.getElementById(controlId);
    fadeValue += FADE_MULTIPLIER;
    control.style.opacity = fadeValue;
	control.style.filter = 'alpha(opacity=' + fadeValue*10 + ')';
    
    if(fadeValue >= MAX_OPACITY_SHOW) {
        fadeValue = MAX_OPACITY_SHOW;
        clearInterval(compId);
    }
}

function fadeOut(controlId) {
    if(controlId != currentComponent) {
        clearInterval(compId);
        return;    
    }
    var control = document.getElementById(controlId);
    fadeValue -= FADE_MULTIPLIER;
    control.style.opacity = fadeValue;
	control.style.filter = 'alpha(opacity=' + fadeValue*10 + ')';
    
    if(fadeValue <= 0) {
        hide(controlId);
        fadeValue = 0;
    }
}

function setPosition(controlId) {
    var control = document.getElementById(controlId);
    control.style.position = 'absolute';
    control.style.top = mouseY + 'px';
    control.style.left = mouseX + 25 + 'px';
}

function showAfterDelay(controlId, setPos) {
    if(!mouseOut && controlId == currentComponent) {
        var control = document.getElementById(controlId);
        control.style.visibility = 'visible';
        control.style.opacity = 0;
        if(setPos) {
            setPosition(controlId);
        }
    }
    fadeValue = 0;
    /* Just to make sure */
    clearInterval(compId);
    compId = setInterval(function(){fadeIn(controlId); parameter = null}, DDTIMER);
}

function hideAfterDelay(controlId) {
    if(mouseOut || controlId != currentComponent) {
        hide(controlId);
    } else {
        fadeValue = MAX_OPACITY_SHOW;
        clearInterval(compId);
        compId = setInterval(function(){fadeOut(controlId); parameter = null}, DDTIMER);
    }
}

function hide(controlId) {
    mouseOut = true;
    var control = document.getElementById(controlId);
    control.style.visibility = 'hidden';
    clearInterval(compId);
}

function getXY(e) {
  if (IE) { // grab the x-y pos.s if browser is IE
    mouseX = event.clientX + document.body.scrollLeft
    mouseY = event.clientY + document.body.scrollTop
  } else {  // grab the x-y pos.s if browser is NS
    mouseX = e.pageX
    mouseY = e.pageY
  }  
}