TippyTaps : CodexHybrid {
	var colorSequence, asciiSpec, <window;
	var keyAction, text, sliders, toggles, views, <group;

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
		asciiSpec = ControlSpec(48, 127, \lin, 1);
		this.getDictionaries;
		this.buildGui;
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

	getDictionaries {
		sliders = ();
		views = ();
		toggles = ();
		modules.synthDef.specs.keysValuesDo({
			| key, value |
			this.buildComponent(key, value);
		});
	}

	buildComponent { | key |
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

		//add view to dictionary of views
		views.add(key -> composite);
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
		^arr;
	}

	buildGui {
		if(window.isNil or: { window.isClosed }){
			var argsComposite = CompositeView().layout = VLayout();
			var viewsArr;
			window = Window.new(
				moduleSet.asString,
				Rect(800, 0.0, 800, 1000),
				scroll: true
			)
			.front.alwaysOnTop_(true).layout = HLayout();

			text = TextView()
			.font_(Font("Monaco", 12))
			.focus(true)
			.editable_(false);

			viewsArr = views.asArray;

			if(views.size.odd, {
				var tmpArr = viewsArr[0..(viewsArr.size - 2)];
				tmpArr = tmpArr.reshape(
					(tmpArr.size / 2).asInteger,
					2
				);
				viewsArr = tmpArr++[viewsArr.last];
			}, {
				viewsArr = viewsArr.reshape(
					(viewsArr.size / 2).asInteger,
					2
				);
			});

			viewsArr.do { | arr |
				var composite = CompositeView();
				composite.layout = HLayout.new;
				arr.do{ | item | composite.layout.add(item) };
				argsComposite.layout.add(composite);
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
