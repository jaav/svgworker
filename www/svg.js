var makeBlue = function () {
		var color = {
			"main1": "#FF0000",
			"main2": "#FFFF00",
			"main3": "#00FF00",
			"main4": "#0000FF",
			"main5": "#00FFFF",
			"main6": "#FF00FF",
			"main7": "#880044",
			"main8": "#44CC88"
		};
		changeColors(color);
	};

	var makeRed = function () {
		var color = {
			"main1": "#FF0000",
			"main2": "#FFFF00",
			"main3": "#880044",
			"main4": "#44CC88",
			"main5": "#00FF00",
			"main6": "#0000FF",
			"main7": "#00FFFF",
			"main8": "#FF00FF"
		};
		changeColors(color);
	};

	var makeYellow = function () {
		var color = {
			"main1": "#FF0000",
			"main2": "#00FF00",
			"main3": "#44CC88",
			"main4": "#880044",
			"main5": "#FFFF00",
			"main6": "#00FFFF",
			"main7": "#0000FF",
			"main8": "#FF00FF"
		};
		changeColors(color);
	};

	var makeGrey = function () {
		var color = {
			"main1": "#888888",
			"main2": "#CCCCCC",
			"main3": "#121212",
			"main4": "#A3A3A3",
			"main5": "#404040",
			"main6": "#B2B2B2",
			"main7": "#343434",
			"main8": "#747474"
		};
		changeColors(color);
	};

	var changeColors = function(color){
		changeColor(color, 1);
		changeColor(color, 2);
		changeColor(color, 3);
		changeColor(color, 4);
		changeColor(color, 5);
		changeColor(color, 6);
		changeColor(color, 7);
		changeColor(color, 8);
	};

	var changeColor = function (color, counter) {
		//color1
		//If color is bright (max rgb >= ee) then only make darker => find minimum data-target and add the Abs() of it to all data-targets
		//Inverse for dark colors
		var bright = isBright(color['main'+counter]);
		var minimumDataTarget = 0;
		if(bright){
			var bStops = document.querySelectorAll('.color_'+counter+' stop');
			for (var j = 0; j < bStops.length; j++) {
				var bStop = parseInt(bStops[j].getAttribute('data-target'));
				if(bStop<minimumDataTarget)
					minimumDataTarget = bStop;
			}
		}
		var stops = document.querySelectorAll('.color_'+counter+' stop');
		for (var j = 0; j < stops.length; j++) {
			var thisStop = stops[j];
			if (thisStop.getAttribute('data-target')){
				isGreen(color.main1);
				thisStop.setAttribute('stop-color', shadeBlendConvert((parseInt(thisStop.getAttribute('data-target'))+minimumDataTarget)/100, color['main'+counter]));
			}
			else
				thisStop.setAttribute('stop-color', color.main1);
		}
	};

	var isBright = function(color){
	    var f=parseInt(color.slice(1),16),
	    	R=f>>16,
	    	G=f>>8&0x00FF,
	    	B=f&0x0000FF;

    	return R>=240 || G>=240 || B>=240;
	};

	var isYellow = function(color){
	    var f=parseInt(color.slice(1),16),
	    	R=f>>16,
	    	G=f>>8&0x00FF,
	    	B=f&0x0000FF;
    	return (R-G > 50) || (R-G >50);
	}

	var isGreen = function(color){
	    var f=parseInt(color.slice(1),16),
	    	R=f>>16,
	    	G=f>>8&0x00FF,
	    	B=f&0x0000FF;
    	var hsv = RGBtoHSV(R, G, B);
    	console.log('Hue for '+ color+' is ' + hsv.h)

    	return (R>200 && G>150) || (R>150 && G>200);
	}

	function shadeBlendConvert(p, from, to) {
    if(typeof(p)!="number"||p<-1||p>1||typeof(from)!="string"||(from[0]!='r'&&from[0]!='#')||(typeof(to)!="string"&&typeof(to)!="undefined"))return null; //ErrorCheck
    if(!this.sbcRip)this.sbcRip=function(d){
        var l=d.length,RGB=new Object();
        if(l>9){
            d=d.split(",");
            if(d.length<3||d.length>4)return null;//ErrorCheck
            RGB[0]=i(d[0].slice(4)),RGB[1]=i(d[1]),RGB[2]=i(d[2]),RGB[3]=d[3]?parseFloat(d[3]):-1;
        }else{
            if(l==8||l==6||l<4)return null; //ErrorCheck
            if(l<6)d="#"+d[1]+d[1]+d[2]+d[2]+d[3]+d[3]+(l>4?d[4]+""+d[4]:""); //3 digit
            d=i(d.slice(1),16),RGB[0]=d>>16&255,RGB[1]=d>>8&255,RGB[2]=d&255,RGB[3]=l==9||l==5?r(((d>>24&255)/255)*10000)/10000:-1;
        }
        return RGB;}
    var i=parseInt,r=Math.round,h=from.length>9,h=typeof(to)=="string"?to.length>9?true:to=="c"?!h:false:h,b=p<0,p=b?p*-1:p,to=to&&to!="c"?to:b?"#000000":"#FFFFFF",f=sbcRip(from),t=sbcRip(to);
    if(!f||!t)return null; //ErrorCheck
    if(h)return "rgb("+r((t[0]-f[0])*p+f[0])+","+r((t[1]-f[1])*p+f[1])+","+r((t[2]-f[2])*p+f[2])+(f[3]<0&&t[3]<0?")":","+(f[3]>-1&&t[3]>-1?r(((t[3]-f[3])*p+f[3])*10000)/10000:t[3]<0?f[3]:t[3])+")");
    else return "#"+(0x100000000+(f[3]>-1&&t[3]>-1?r(((t[3]-f[3])*p+f[3])*255):t[3]>-1?r(t[3]*255):f[3]>-1?r(f[3]*255):255)*0x1000000+r((t[0]-f[0])*p+f[0])*0x10000+r((t[1]-f[1])*p+f[1])*0x100+r((t[2]-f[2])*p+f[2])).toString(16).slice(f[3]>-1||t[3]>-1?1:3);
}

	var RGBtoHSV = function(r, g, b) {
    if (arguments.length === 1) {
        g = r.g, b = r.b, r = r.r;
    }
    var max = Math.max(r, g, b), min = Math.min(r, g, b),
        d = max - min,
        h,
        s = (max === 0 ? 0 : d / max),
        v = max / 255;

    switch (max) {
        case min: h = 0; break;
        case r: h = (g - b) + d * (g < b ? 6: 0); h /= 6 * d; break;
        case g: h = (b - r) + d * 2; h /= 6 * d; break;
        case b: h = (r - g) + d * 4; h /= 6 * d; break;
    }

    return {
        h: h,
        s: s,
        v: v
    };
}