TippyTaps : CodexHybrid {
	var <typingLayout, colorSequence, asciiSpec;
	var keyAction, text, <sliders, <views;
	var <window, <>activeBuffer, <activeBufferIndex = 0;

	*makeTemplates {  | templater |
		templater.tippyTaps_synthDef;
	}

	*contribute { | versions |
		versions.add(
			[\cobra, Main.packages.asDict.at(\TippyTaps)+/+"cobra"]
		);
	}

	initHybrid {
		colorSequence = Pseq([
			Color(0.501, 0.91, 0.98),
			Color(0.6, 1.0, 0.7),
			Color(1.0, 0.5, 0.7),
			Color(1.0, 1.0, 0.0)
		], inf).asStream;
		asciiSpec = ControlSpec(48, 127, \lin, 1.0);
		this.getDictionaries;
		this.buildGui;
	}

	getDictionaries {
		sliders = ();
		views = ();
		modules.synthDef.specs.keysValuesDo({
			| key, value |
			this.buildComponent(key, value);
		});
	}

	buildComponent { | name, spec |
		var boxLo, boxLoText, boxLoComposite;
		var boxHi, boxHiText, boxHiComposite;
		var text, slider, composite, boxView;

		text = StaticText().align_(\center)
		.string_(format("% values", name.asString));

		slider = RangeSlider().orientation_('horizontal')
		.action_({ | obj |
			boxLo.value = spec.map(obj.value.lo);
			boxHi.value = spec.map(obj.value.hi);
		});

		//add slider to dictionary of sliders
		sliders.add(name -> slider);

		boxLo = NumberBox().action_({ | obj |
			slider.activeLo = spec.unmap(obj.value);
		});

		boxLoText = StaticText().align_(\center)
		.string_(format("% lo", name.asString));

		boxLoComposite = CompositeView()
		.layout_(VLayout(boxLo, boxLoText));

		boxHi = NumberBox().action_({ | obj |
			slider.activeHi = spec.unmap(obj.value);
		});

		boxHiText = StaticText().align_(\center)
		.string_(format("% hi", name.asString));

		boxHiComposite = CompositeView()
		.layout_(VLayout(boxHi, boxHiText));

		boxView = CompositeView()
		.layout_(HLayout(boxLoComposite, boxHiComposite));

		composite = CompositeView();
		composite.background = colorSequence.next;
		composite.layout = VLayout(text, slider, boxView);

		slider.activeLo = 0; 
		slider.activeHi = 1;

		//add view to dictionary of views
		views.add(name -> composite);
	}

	getArguments { | value |
		var arr = [];
		var specs = modules.synthDef.specs;
		sliders.keysValuesDo({
			| key, slider |
			var tmpspec = ControlSpec(slider.lo, slider.hi);
			arr = arr.add(key);
			arr = arr.add(specs[key].map(
				tmpspec.map(asciiSpec.unmap(value));
			));
		});
		^arr;
	}

	buildGui {
		if(window.isNil){
			var argsComposite = CompositeView().layout = VLayout();
			window = Window.new(
				"Cobra Window",
				Rect(800, 0.0, 800, 1000), 
				scroll: true
			)
			.front.alwaysOnTop_(true).layout = HLayout();

			text = TextView()
			.font_(Font("Monaco", 12))
			.focus(true)
			.editable_(false);

			views.do { | item | 
				argsComposite.layout.add(item);
			};

			window.layout.add(text);
			window.layout.add(argsComposite);

			window.view.keyDownAction = {
				| view, letter, modifier, ascii, keycode, key |

				if(ascii==13){
					var arguments = this.getArguments(ascii.wrap(48, 127));
					var newHeader;
					text !? {text.string = newHeader++"\n\n"};
				}{
					text !? {text.string = text.string++letter};
					Synth(
						modules.synthDef.name,
						this.getArguments(ascii.wrap(48, 127));
					);
				}
			};
		};
	}

	free {
		if(window.isNil.not){
			window.onClose = nil;
			window.close;
			window = nil;
		};
	}

}
