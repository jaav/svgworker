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

	var makeSkinTone2 = function () {
		var color = {
			"main1": "#FF0000",
			"main2": "#F2C478",
			"main3": "#00FF00"
		};
		changeColors(color);
	}

	var makeSkinTone6 = function () {
		var color = {
			"main1": "#FF0000",
			"main2": "#66432C",
			"main3": "#00FF00"
		};
		changeColors(color);
	}

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
			var bStops = document.querySelectorAll('.color'+counter+' stop');
			for (var j = 0; j < bStops.length; j++) {
          var dataTarget = bStops[j].getAttribute('data-target');
          if (dataTarget) {
            bStops[j].setAttribute('stop-color', shadeColour(color['main'+counter], dataTarget));
          } else {
            bStops[j].setAttribute('stop-color', color['main'+ counter]);
          }
			}
	};

  /**
   * Get shade colour
   */
  var shadeColour = function(color, percent) {
    const f = parseInt(color.slice(1), 16);
    const t = percent < 0 ? 0 : 255;
    const p = percent < 0 ? percent * -1 : percent;
    const R = f >> 16;
    const G = f >> 8 & 0x00FF;
    const B = f & 0x0000FF;

    let R_test = Math.round((t - R) * p) + R;
    let G_test = Math.round((t - G) * p) + G;
    let B_test = Math.round((t - B) * p) + B;

    let test = "#" + this.componentToHex(Math.round((t - R) * p) + R) + this.componentToHex(Math.round((t - G) * p) + G) + this.componentToHex(Math.round((t - B) * p) + B);
    console.log('From color '+color);
    console.log('by percent '+percent);
    console.log('Results in '+test);
    return "#" + this.componentToHex(Math.round((t - R) * p) + R) + this.componentToHex(Math.round((t - G) * p) + G) + this.componentToHex(Math.round((t - B) * p) + B);

    //return '#' + (0x1000000 + (Math.round((t - R) * p) + R) * 0x10000 + (Math.round((t - G) * p) + G) * 0x100 + (Math.round((t - B) * p) + B)).toString(16).slice(1);
  };

  var componentToHex = function(c) {
    var hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
  };
