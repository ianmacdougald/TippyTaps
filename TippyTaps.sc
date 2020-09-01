TippyTaps : CodexHybrid {
	var colorSequence, <window, keyAction, group;
	var sliders, toggles, composites, <ioViews, <ios;

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
			Color(0.501, 0.91, 0.98),
			Color(0.6, 1.0, 0.7),
			Color(1.0, 0.5, 0.7),
			Color(1.0, 1.0, 0.0)
		], inf).asStream;
		Routine({
			server.sync;
			this.initSliders;
			this.initIOs;
			this.initGroup;
			this.buildGui;
		}).play(AppClock);
	}

	initGroup {
		group ?? {
			group = Group.new;
			group.onFree({ group = nil });
		}
	}

	moduleSet_{ | to, from |
		window.close;
		super.moduleSet_(to, from);
	}

	initSliders {
		sliders = ();
		composites = ();
		toggles = ();
		modules.synthDef.specs.keysValuesDo({
			| key, value |
			this.sliders(key, value);
		});
	}

	initIOs {
		ios = ();
		ioViews = ();
		this.fillIOs(\inputs);
		this.fillIOs(\outputs);
	}

	fillIOs { | type(\outputs) |
		var synthDefDesc = this.class.cache[moduleSet].synthDef.desc;
		var coll = synthDefDesc.perform(type);
		if(coll.isEmpty.not, {
			var arr = [];

			arr = arr.add(
				StaticText()
				.align_(\center)
				.string_(type.asString)
				.font_(Font.default.copy.size_(24));
			);

			coll.do { | desc, index |
				var channels = desc.numberOfChannels;
				var offset = if(
					channels >= 2 and: { index > 0 },
					{ channels - 1 },
					{ 0 }
				);
				var name = desc.startingChannel.asSymbol.postln;
				var composite = CompositeView();

				var label = StaticText()
				.align_(\center)
				.string_(name.asString)
				.font_(Font.default.copy.size_(18));

				var box = NumberBox()
				.align_(\center)
				.string_(index + offset)
				.font_(Font.default.copy.size_(18))
				.action_({ | obj | ios[name] = obj.string });

				composite.layout = HLayout(label, box);
				arr = arr.add(composite);
			};
			ioViews[type] = arr;
		});
	}

	sliders { | key |
		var boxLo, boxLoText, boxLoComposite;
		var boxHi, boxHiText, boxHiComposite;
		var text, slider, composite, boxView;
		var toggle, toggleText, toggleComposite;

		var font = Font.default.copy.size =
		18 - modules.synthDef.specs.size.clip(1, 24, 0, 12);

		text = StaticText().align_(\center)
		.string_(format("% values", key.asString))
		.font_(font);

		slider = RangeSlider().orientation_('horizontal')
		.action_({ | obj |
			var spec = modules.synthDef.specs[key];
			boxLo.value = spec.map(obj.value.lo);
			boxHi.value = spec.map(obj.value.hi);
		});

		//add slider to dictionary of sliders
		sliders.add(key -> slider);

		boxLo = NumberBox().action_({ | obj |
			var spec = modules.synthDef.specs[key];
			slider.activeLo = spec.unmap(obj.value);
		}).font_(font);

		boxLoText = StaticText().align_(\center)
		.string_(format("% lo", key.asString)).font_(font);

		boxLoComposite = CompositeView()
		.layout_(VLayout(boxLo, boxLoText)).font_(font);

		boxHi = NumberBox().action_({ | obj |
			var spec = modules.synthDef.specs[key];
			slider.activeHi = spec.unmap(obj.value);
		}).font_(font);

		boxHiText = StaticText().align_(\center)
		.string_(format("% hi", key.asString)).font_(font);

		boxHiComposite = CompositeView()
		.layout_(VLayout(boxHi, boxHiText));

		boxView = CompositeView()
		.layout_(HLayout(boxLoComposite, boxHiComposite));

		toggle = Button()
		.states_([
			["", Color.black, Color.white],
			["X", Color(1.0, 0.5, 0.7), Color.white]
		]).font_(font);

		toggles.add(key -> toggle);

		toggleText = StaticText()
		.align_(\center).font_(font).string = "Reverse mapping";

		toggleComposite = VLayout(
			HLayout(CompositeView(), toggle, CompositeView()), toggleText
		);

		composite = CompositeView();
		composite.background = colorSequence.next;
		composite.layout = VLayout(
			text,
			slider,
			boxView,
			toggleComposite
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
			if(toggles[key].value==0, {
				asciiSpec = ControlSpec(48, 127, \lin, 1);
			}, { asciiSpec = ControlSpec(127, 48, \lin, 1) });
			arr = arr.add(key);
			arr = arr.add(specs[key].map(
				sliderSpec.map(asciiSpec.unmap(value));
			));
		});
		ios.do { | dict | arr = arr++dict.asPairs };
		^arr;
	}

	buildGui {
		if(window.isNil or: { window.isClosed }){
			var argsComposite = CompositeView().layout = VLayout();
			var compositesArr, text, textLabel, textComposite, ioComposite;
			window = Window.new(
				moduleSet.asString,
				Rect(800, 0.0, 800, 1000),
				scroll: true
			)
			.front.alwaysOnTop_(true).layout = HLayout();

			textLabel = StaticText()
			.align_(\center).string_("Type here!")
			.font_(Font.default.copy.size_(24));

			text = TextView()
			.font_(Font.default.copy.size_(18))
			.focus(true)
			.editable_(false);

			ioComposite = CompositeView().layout = VLayout();
			ioViews.do{ | array |
				if(array.isEmpty.not, {
					array.do{ | item | ioComposite.layout.add(item) };
				});
			};

			textComposite = CompositeView()
			.layout_(VLayout(ioComposite, textLabel, text));

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

			window.layout.add(textComposite);
			window.layout.add(argsComposite);

			window.view.keyDownAction = {
				| view, letter, modifier, ascii, keycode, key |
				if(ascii==13){
					var arguments = this.getArguments(ascii.wrap(48, 127));
					var newHeader;
					text !? {text.string = newHeader++"\n\n"};
				}{
					text !? {text.string = text.string++letter};
					this.initGroup;
					Synth(
						modules.synthDef.name,
						this.getArguments(ascii.wrap(48, 127)),
						group
					);
				}
			};
		};
	}

	reloadScripts {
		super.reloadScripts;
		this.buildGui;
	}

	close {
		if( window.notNil and: { window.isClosed.not }, {
			window.close;
			if(group.notNil, { group.free });
		});
	}

}
