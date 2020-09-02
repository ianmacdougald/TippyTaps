//Note: input and output argument fields don't work with synths yet.
TippyTaps : CodexHybrid {
	var colorSequence, <window, keyAction, <>group;
	var sliders, toggles, composites, <ios;

	*makeTemplates {  | templater |
		templater.tippyTaps_synthDef;
	}

	*contribute { | versions |
		versions.add(
			[\ian_mono, Main.packages.asDict.at(\TippyTaps)+/+"ian_mono"]
		);

		versions.add(
			[\ian_stereo, Main.packages.asDict.at(\TippyTaps)+/+"ian_stereo"]
		);
	}

	initHybrid {
		colorSequence = Pseq([
			Color(0.5, 0.9, 1.0),
			Color(0.6, 1.0, 0.7),
			Color(1.0, 0.6, 0.9),
			Color(1.0, 0.95, 0.6),
		], inf).asStream;
		Routine({
			server.sync;
			this.initIOs;
			this.buildGui;
		}).play(AppClock);
	}

	moduleSet_{ | to, from |
		window.close;
		super.moduleSet_(to, from);
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

	initIOs {
		var desc = this.class.cache[moduleSet].synthDef.desc;
		var count = 0;
		var fillIO = { | coll | 
			if(coll.isEmpty.not, { 
				coll.do { | io |
					ios.add(io.startingChannel.asSymbol -> count);	
					count = count + 1 + (io.numberOfChannels - 1);
				};
			})
		};
		ios = ();
		fillIO.value(desc.outputs);
		fillIO.value(desc.inputs);
	}

	setBus { | key, value | ios[key] = value }

	formatSliders { | key |
		var boxLo, boxLoText, boxLoComposite;
		var boxHi, boxHiText, boxHiComposite;
		var text, slider, composite, boxView;
		var toggle, toggleText, toggleComposite;
		var collapse, collapseText, collapseComposite;
		var buttonsComposite;

		var font = Font.default.copy.size =
		18 - modules.synthDef.specs.size.clip(1, 24, 0, 6).asInteger;

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
		}).font_(font).maxDecimals_(4);

		boxLoText = StaticText().align_(\center)
		.string_(format("% lo", key.asString)).font_(font);

		boxLoComposite = CompositeView()
		.layout_(VLayout(boxLo, boxLoText)).font_(font);

		boxHi = NumberBox().action_({ | obj |
			var spec = modules.synthDef.specs[key];
			slider.activeHi = spec.unmap(obj.value);
		}).font_(font).maxDecimals_(4);

		boxHiText = StaticText().align_(\center)
		.string_(format("% hi", key.asString)).font_(font);

		boxHiComposite = CompositeView()
		.layout_(VLayout(boxHi, boxHiText));

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
		);

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
		);

		buttonsComposite = CompositeView()
		.layout = HLayout(toggleComposite, collapseComposite);

		composite = CompositeView().minSize_(Size(
			0,
			100
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

	getArguments { | value |
		var arr = [];
		var specs = modules.synthDef.specs;
		sliders.keysValuesDo({
			| key, slider |
			var sliderSpec = ControlSpec(slider.lo, slider.hi);
			var asciiSpec;
			if(toggles.reverse[key].value==0, {
				asciiSpec = ControlSpec(48, 127, \lin, 1);
			}, { asciiSpec = ControlSpec(127, 48, \lin, 1) });
			arr = arr.add(key);
			arr = arr.add(specs[key].map(
				sliderSpec.map(asciiSpec.unmap(value));
			));
		});
		^(arr++ios.asPairs).postln;
	}

	buildGui {
		if(window.isNil or: { window.isClosed }){
			var argsComposite = CompositeView().layout = VLayout();
			var compositesArr, text, textLabel, textComposite, ioComposite;

			window = Window.new(
				moduleSet.asString,
				Rect(800, 0.0, 800, 1000),
				scroll: true
			);

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
				| view, letter, modifier, ascii, keycode, key |
				if(ascii==13){
					text !? { text.string = ""; }
				}{
					text !? {text.string = text.string++letter};
					Synth(
						modules.synthDef.name,
						this.getArguments(ascii.wrap(48, 127)),
						group ?? { server.defaultGroup }
					);
				}
			};

			window.front.alwaysOnTop_(true);
		};
	}

	close {
		if( window.notNil and: { window.isClosed.not }, {
			window.close;
		});
	}

}
