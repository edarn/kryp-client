var opacity;
var sign;
var currentImage;
var next = 0;

var id = "";
var state;

/* This also decides the sleep time between the fadings */
var MAX_OPACITY = 3;

if(IE) {
    MAX_OPACITY *= 10;
}


function start() {
    if(id != "") {
        return;
    }
    reset('start');
    opacity = 0;
    nextImage();
    if(id == "") {
        clearInterval(id);
    }
    id = setInterval('fade()', 15);
}

function resume() {
    reset('resume');
    if(id == "") {
        clearInterval(id);
    }
    id = setInterval('fade()', 15);
}

function handleKeyEvent() {
    /* Get the mouse location and decide what control it is */
    if(mouseY > 160 && mouseY < 240) {
        if(mouseX > 230 && mouseX < 310) {
            previousImage();
        } else if(mouseX > 430 && mouseX < 510) {
            if(state == 'running') {
                stop();
            } else {
                resume();
            }
            showIcon();
        } else if(mouseX > 630 && mouseX < 710) {
            nextImage();
        }
        
    }
}

function reset(status) {
    if(status == 'stop' && currentImage != null) {
        opacity = MAX_OPACITY;
        currentImage.style.opacity = opacity;
        currentImage.style.filter = 'alpha(opacity=' + opacity*10 + ')';
        sign = 0;
        state = 'stopped';
    } else if(status == 'start') {
        sign = 1;
        if(currentImage != null) {
            hideImage();
        }
        state = 'running';
    } else if(status == 'resume') {
        sign = -1;
        state = 'running';
    }

}

function stop() {
    if(id != "") {
        clearInterval(id);
    }
    reset('stop');
}

function fade() {
    opacity += (FADE_MULTIPLIER * sign);
    currentImage.style.opacity = opacity;
	currentImage.style.filter = 'alpha(opacity=' + opacity*10 + ')';
    
    if(opacity >= MAX_OPACITY) {
        opacity = MAX_OPACITY;
        sign *= -1;
    }
    if(opacity <= 0) {
        hideImage();
        opacity = 0;
        sign *= -1;
        nextImage();
    }
}

function nextImage() {
    /* make the last image hidden */
    if(currentImage != null) {
        hideImage();
    }
    
    currentImage = document.getElementById('image' + next);
    showImage();
    
    next += 1;
    if(next >= 5) {
        next = 0;
    }
}

function previousImage() {
    /* make the last image hidden */
    if(currentImage != null) {
        hideImage();
    }
    
    currentImage = document.getElementById('image' + next);
    showImage();
    
    next -= 1;
    if(next < 0) {
        next = 4;
    }
}

function showImage() {
    currentImage.style.visibility = 'visible';
    if(state == 'running') {
        currentImage.style.opacity = 0;
    } else {
        currentImage.style.opacity = MAX_OPACITY;
    }
}

function hideImage() {
    currentImage.style.visibility = 'hidden';
}

function showIcon() {
    if(state == 'running') {
        document.getElementById('mediaBar').src = './design/mediaBar_stop.png';
    } else {
        document.getElementById('mediaBar').src = './design/mediaBar_play.png';
    }
}

var zoomId = "";

function zoomIn(controlId) {
    var control = document.getElementById(controlId);
    zoomInRec(control, 100);
}

function zoomInRec(control, x) {
    x += 2;
    control.style.width = x + 'px';
    
    if(x >= 125) {
        return;
    } else {
        setTimeout(function(){zoomInRec(control, x); parameter = null}, DDTIMER);
    }
}

function zoomOutRec(control, x) {
    x -= 2;
    control.style.width = x + 'px';
    
    if(x <= 100) {
        return;
    } else {
        setTimeout(function(){zoomOutRec(control, x); parameter = null}, DDTIMER);
    }
}

function zoomOut(controlId) {
    var control = document.getElementById(controlId);
    zoomOutRec(control, 125);
}

function slideEffect(controlId, stopAt) {
    var control = document.getElementById(controlId);
    control.style.position = 'absolute';
    
    slide(control, stopAt, 250);
}

function slide(control, slideUntil, x) {
    x += 5;
    control.style.left = x + 'px';
    if(x >= slideUntil) {
        return;
    } else {
        setTimeout(function(){slide(control, slideUntil, x); parameter = null}, DDTIMER);        
    }
}
function setOpacity(controlId, opacity) {
    var control = document.getElementById(controlId);
    if(IE) {
        opacity *= 10;
    }
    control.style.opacity = opacity;
    control.style.filter = 'alpha(opacity=' + opacity*10 + ')';
}