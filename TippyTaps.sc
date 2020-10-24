TippyTaps : CodexInstrument {
	var keysDown, synths;
	var keyAction, sliders, toggles;
	var composites, ascii, colorSequence;

	*contribute { | versions |
		var path = Main.packages.asDict.at(\TippyTaps);

		versions.add(
			[\ian_mono, path+/+"ian_mono"]
		);

		versions.add(
			[\ian_stereo, path+/+"ian_stereo"]
		);

		versions.add(
			[\ian_pad, path+/+"ian_pad"]
		);

	}

	initSliders {
		sliders = ();
		composites = ();
		toggles = (
			reverse: (),
			collapse: ()
		);
		modules.synthDef.specs.keysValuesDo({
			| key, value |
			this.formatSliders(key, value);
		});
	}

	formatSliders { | key |
		var boxLo, boxLoText, boxLoComposite;
		var boxHi, boxHiText, boxHiComposite;
		var text, slider, composite, boxView;
		var toggle, toggleText, toggleComposite;
		var collapse, collapseText, collapseComposite;
		var buttonsComposite;

		var font = Font.default.copy.size =
		21 - modules.synthDef.specs.size.explin(1, 12, 0.0, 12.0);

		var prevlo = 0, prevhi = 1;

		text = StaticText().align_(\center)
		.string_(format("% values", key.asString))
		.font_(font);

		slider = RangeSlider().orientation_('horizontal')
		.action_({ | obj |
			var spec = modules.synthDef.specs[key];
			if(toggles.collapse[key].value==1, {
				case { obj.value.lo!=prevlo }{
					obj.value.hi = obj.value.lo;
				}{ obj.value.hi!=prevhi }{
					obj.value.lo = obj.value.hi
				};
			});

			boxLo.value = spec.map(obj.value.lo);
			boxHi.value = spec.map(obj.value.hi);
			prevlo = obj.value.lo;
			prevhi = obj.value.hi;
		});

		//add slider to dictionary of sliders
		sliders.add(key -> slider);

		boxLo = NumberBox().action_({ | obj |
			var spec = modules.synthDef.specs[key];
			slider.activeLo = spec.unmap(obj.value);
		}).font_(font).maxDecimals_(4).align_(\center)
		.minSize_(Size(0, font.size * 1.5));

		boxLoText = StaticText().align_(\center)
		.string_(format("% lo", key.asString)).font_(font);

		boxLoComposite = CompositeView()
		.layout_(VLayout(boxLo, boxLoText))
		.minSize_(Size(0, font.size * 5));

		boxHi = NumberBox().action_({ | obj |
			var spec = modules.synthDef.specs[key];
			slider.activeLo = spec.unmap(obj.value);
		}).font_(font).maxDecimals_(4).align_(\center)
		.minSize_(Size(0, font.size * 1.5));

		boxHiText = StaticText().align_(\center)
		.string_(format("% hi", key.asString)).font_(font);

		boxHiComposite = CompositeView()
		.layout_(VLayout(boxHi, boxHiText))
		.minSize_(Size(0, font.size * 5));

		boxView = CompositeView()
		.layout_(HLayout(boxLoComposite, boxHiComposite));

		toggle = Button()
		.states_([
			["", Color.black, Color.white],
			["X", Color.black, Color.white]
		]).font_(font);

		toggles.reverse.add(key -> toggle);

		toggleText = StaticText()
		.align_(\center).font_(font).string = "reverse mapping";

		toggleComposite = CompositeView().layout_(
			VLayout(toggle, toggleText)
		).minSize_(Size(0, font.size * 5));

		collapse = Button()
		.states_([
			["", Color.black, Color.white],
			["X", Color.black, Color.white]
		]).font_(font)
		.action_({
			| obj |
			if(obj.value==1, {
				sliders[key].setSpanActive(sliders[key].lo, sliders[key].lo);
			})
		});

		toggles.collapse.add(key -> collapse);

		collapseText = StaticText()
		.align_(\center).font_(font).string = "collapse range";

		collapseComposite = CompositeView().layout_(
			VLayout(collapse, collapseText)
		).minSize_(Size(0, font.size * 5));

		buttonsComposite = CompositeView()
		.layout = HLayout(toggleComposite, collapseComposite);

		composite = CompositeView().minSize_(Size(
			0,
			50
		));
		composite.background = colorSequence.next;
		composite.layout = VLayout(
			text,
			slider,
			boxView,
			buttonsComposite
		);

		slider.activeLo = 0;
		slider.activeHi = 1;

		//add view to dictionary of composites
		composites.add(key -> composite);
	}

	updateSpec { | key, spec |
		if(spec.isKindOf(ControlSpec), {
			modules.synthDef.specs[key] = spec;
			sliders[key].activeLo = sliders[key].lo;
			sliders[key].activeHi = sliders[key].hi;
		});
	}

	getArguments { | specs |
		var arr;
		specs.keysValuesDo({ | key, value |
			var slider = sliders[key];
			var sliderSpec = ControlSpec(slider.lo, slider.hi);
			var asciiSpec;
			if(toggles.reverse[key].value==0)
			{
				asciiSpec = ControlSpec(48, 127, \lin, 1);
			}
			//else
			{
				asciiSpec = ControlSpec(127, 48, \lin, 1);
			};
			arr = arr.add(key);
			arr = arr.add(specs[key].map(
				sliderSpec.map(asciiSpec.unmap(ascii));
			));
		});
		^arr;
	}

	initInstrument {
		var argsComposite, compositesArr;
		var text, textLabel, textComposite;

		synths = Dictionary.new;
		keysDown = Dictionary.new;

		argsComposite = CompositeView().layout = VLayout();

		colorSequence = Pseq([
			Color(0.5, 0.9, 1.0),
			Color(0.6, 1.0, 0.7),
			Color(1.0, 0.6, 0.9),
			Color(1.0, 0.95, 0.6),
		], inf).asStream;

		this.window = Window.new(
			moduleSet.asString,
			Rect(800, 0.0, 800, 1000),
			scroll: true
		).alwaysOnTop_(true).front;

		this.initSliders;

		textLabel = StaticText()
		.align_(\center).string_("type here!")
		.font_(Font.default.copy.size_(24));

		text = TextView()
		.font_(Font.default.copy.size_(18))
		.focus(true)
		.editable_(false);

		textComposite = CompositeView()
		.layout_(VLayout(textLabel, text));

		compositesArr = composites.asArray;

		if(composites.size.odd, {
			var tmpArr = compositesArr[0..(compositesArr.size - 2)];
			tmpArr = tmpArr.reshape(
				(tmpArr.size / 2).asInteger,
				2
			);
			compositesArr = tmpArr++[compositesArr.last];
		}, {
			compositesArr = compositesArr.reshape(
				(compositesArr.size / 2).asInteger,
				2
			);
		});

		compositesArr.do { | arr |
			var composite = CompositeView();
			composite.layout = HLayout.new;
			arr.do{ | item | composite.layout.add(item) };
			argsComposite.layout.add(composite);
		};

		window.layout = HLayout(textComposite, argsComposite);

		window.view.keyDownAction = {
			| view, letter, modifier, asciiVal, keycode, key |
			if(asciiVal==13)
			{
				text.string = "";
			}
			//else
			{
				keysDown.at(letter.asSymbol) ?? {
					text.string = text.string++letter;
					ascii  = asciiVal.wrap(48, 127);
					this.makeSynth(letter.asSymbol);
					keysDown.add(letter.asSymbol -> letter);
				};
			};
		};

		window.view.keyUpAction = {
			| view, letter, modifier, asciiVal, keycode, key |
			if(asciiVal!=13)
			{
				this.releaseSynth(letter.asSymbol);
				keysDown.removeAt(letter.asSymbol);
			};
		};
	}

	makeSynth { | symbol |
		synths.at(symbol) ?? {
			synths.add(symbol -> super.makeSynth);
		};
	}

	releaseSynth { | symbol |
		if(synths.at(symbol).isPlaying, {
			synths.removeAt(symbol).release;
		});
	}
}
