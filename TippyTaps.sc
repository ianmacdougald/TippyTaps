TippyTaps : CodexHybrid {
	var <typingLayout, colorSequence;
	var keyAction, text, sliders, views;
	var <window, <>activeBuffer, <activeBufferIndex = 0;

	*makeTemplates {  | templater |
		templater.tippyTaps_synthDef("synthDef");
	}

	*contribute { | versions |
		versions.add(
			[\cobra, Main.packages.asDict.at(\TippyTaps)+/+"cobra"]
		)
	}

	initHybrid {
		colorSequence = Pseq([
			Color(0.501, 0.91, 0.98),
			Color(0.6, 1.0, 0.7),
			Color(0.8, 0.5, 0.1),
			Color(1.0, 1.0, 0.0)
		], inf).asStream;
		this.getSliders
	}

	getSliders {
		sliders = ();
		modules.tippySpecs.keysValuesDo({
			| key, value |
			if(value.isKindOf(Spec), {
				this.buildComponent(key, value);
			});
		});
	}

	makeComposite { | name, spec |
		var boxLo, boxLoText, boxLoComposite;
		var boxHi, boxHiText, boxHiComposite;
		var text, slider, composite, boxView;

		text = StaticText().align_(\center)
		.string_("% values", name.asString);

		slider = RangeSlider().orientation_('horizontal')
		.action_({ | obj |
			boxLo.value = spec.map(obj.value.lo);
			boxHi.value = spec.map(obj.value.hi);
		});

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

		views.add(name -> CompositeView.background_(colorSequence.next)
			.layout_(VLayout(text, slider, boxView))
		);

		/*;
		var rateBoxLo = NumberBox().action_({|view|
		var maxLo = this.class.rateLoMax;
		var maxHi = this.class.rateHiMax;
		var val = view.value;
		rateSlider.activeLo = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var rateBoxLoText = StaticText()
		.string_("Rate Lo").font_(Font("Monaco", 12));

		var rateBoxLoView = CompositeView()
		.layout_(VLayout(rateBoxLo, rateBoxLoText));

		var rateBoxHi = NumberBox().action_({|view|
		var maxLo = this.class.rateLoMax;
		var maxHi = this.class.rateHiMax;
		var val = view.value;
		rateSlider.activeHi = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var rateBoxHiText = StaticText()
		.string_("Rate Hi").font_(Font("Monaco", 12));

		var rateBoxHiView = CompositeView()
		.layout_(VLayout(rateBoxHi, rateBoxHiText));

		var rateBoxView = CompositeView()
		.layout_(HLayout(rateBoxLoView, rateBoxHiView));

		var rateView = CompositeView()
		.layout_(VLayout(rateText, rateSlider, rateBoxView))
		.background_(Color.new255(176, 224, 240)).alpha_(0.5);		var compositeView;
		*/
	}

	/*buffers_{ | newBuffers |
		if(newBuffers.isCollection, {
			buffers = newBuffers.select({ | item |
				item.isKindOf(Buffer);
			});
		});
		activeBuffer = buffers[activeBufferIndex % buffers.size];
		if(window.isNil or: { window.isClosed }, {
			this.buildGui;
		});
	}*/

	getArguments { | value |
		var arr = [];
		sliders.keysValuesDo({
			| key, slider |
			var min = slider[key].lo;
			var max = slider[key].hi;
			var value = modules.tippySpecs[key].map(rrand(min, max));
			arr = arr.add(key);
			arr = arr.add(value);
		});
	}

	buildGui {

		if(window.isNil){
			window = Window.new(
				"Cobra Window",
				Rect(901.0, 0.0, 782.0, 1005.0))
			.front.alwaysOnTop_(true);
			window.onClose_({ this.free });

			text = TextView()
			.font_(Font("Monaco", 12))
			.focus(true)
			.editable_(false)
			.string_(
				(format("Active buffer name: %",
					PathName(activeBuffer.path).fileNameWithoutExtension)
				)++"\n\n"
			);

			window.view.keyDownAction = {
				| view, letter, modifier, ascii, keycode, key |

				if(ascii==13){
					var arguments = this.getArguments(ascii.wrap(48, 127));
					var newHeader;
					//activeBufferIndex = (activeBufferIndex + 1)
					//% buffers.size;
					//activeBuffer = buffers[activeBufferIndex];

					newHeader = format("Active buffer name: %",
						PathName(activeBuffer.path).fileNameWithoutExtension);

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

	//this.setUpParticleLayout;
	//window.layout_(HLayout(typingLayout));
	// window.layout = VLayout(typingLayout, particleLayout);
	//window.layout = VLayout(HLayout(typingLayout), 25, particleLayout);

	setUpTypingLayout {
		var decNum = 2;
		var bufferIndex = 0;
		//var bufferSize = buffers.size;

		/*//RATE STUFF//
		var rateText = StaticText()
		.string_("Rate Values").font_(Font("Monaco", 16));

		var rateReverseText = StaticText()
		.string_("Reverse Mappings").font_(Font("Monaco, 6"));

		var rateReverse = false;

		var rateButton = Button().states_([
		["", Color.black],
		["X", Color.black]
		])
		.action_({|b| var val = b.value;
		if(b.value==1){
		rateReverse = true;
		}
		});

		var rateButtonView = CompositeView()
		.layout_(VLayout(rateReverseText, rateButton));

		var textButtonView = CompositeView()
		.layout_(HLayout(rateText, rateButtonView)).minSize_(Size(100, 25));

		var rateSlider = RangeSlider()
		.orientation_('horizontal').action_({|view|
		var lo = view.lo, hi = view.hi;
		var maxLo = this.class.rateLoMax;
		var maxHi = this.class.rateHiMax;
		rateLo = lo.linlin(0.00, 1.00, maxLo, maxHi);
		rateHi = hi.linlin(0.00, 1.00, maxLo, maxHi);
		if(rateReverse){
		rateBoxLo.value = rateHi;
		rateBoxHi.value = rateLo;
		}{
		rateBoxLo.value = rateLo;
		rateBoxHi.value = rateHi;
		};
		}).font_(Font("Monaco", 12)).keyDownAction_({""});

		var rateBoxLo = NumberBox().action_({|view|
		var maxLo = this.class.rateLoMax;
		var maxHi = this.class.rateHiMax;
		var val = view.value;
		rateSlider.activeLo = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var rateBoxLoText = StaticText()
		.string_("Rate Lo").font_(Font("Monaco", 12));

		var rateBoxLoView = CompositeView()
		.layout_(VLayout(rateBoxLo, rateBoxLoText));

		var rateBoxHi = NumberBox().action_({|view|
		var maxLo = this.class.rateLoMax;
		var maxHi = this.class.rateHiMax;
		var val = view.value;
		rateSlider.activeHi = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var rateBoxHiText = StaticText()
		.string_("Rate Hi").font_(Font("Monaco", 12));

		var rateBoxHiView = CompositeView()
		.layout_(VLayout(rateBoxHi, rateBoxHiText));

		var rateBoxView = CompositeView()
		.layout_(HLayout(rateBoxLoView, rateBoxHiView));

		var rateView = CompositeView()
		.layout_(VLayout(rateText, rateSlider, rateBoxView))
		.background_(Color.new255(176, 224, 240)).alpha_(0.5);

		//TIME STUFF//
		var timeText = StaticText()
		.string_("Time Values").font_(Font("Monaco", 16));

		var timeSlider = RangeSlider()
		.orientation_('horizontal').action_({|view|
		var lo = view.lo, hi = view.hi;
		var maxLo = this.class.timeLoMax;
		var maxHi = this.class.timeHiMax;
		timeLo = lo.linexp(0.00, 1.00, maxLo, maxHi);
		timeHi = hi.linexp(0.00, 1.00, maxLo, maxHi);
		timeBoxLo.value = timeLo;
		timeBoxHi.value = timeHi;
		}).font_(Font("Monaco", 12)).keyDownAction_({""});

		var timeBoxLo = NumberBox().action_({|view|
		var maxLo = this.class.timeLoMax;
		var maxHi = this.class.timeHiMax;
		var val = view.value;
		timeSlider.activeLo = val.explin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var timeBoxLoText = StaticText()
		.string_("Time Lo").font_(Font("Monaco", 12));

		var timeBoxLoView = CompositeView()
		.layout_(VLayout(timeBoxLo, timeBoxLoText));

		var timeBoxHi=  NumberBox().action_({|view|
		var maxLo = timeLoMax;
		var maxHi = timeHiMax;
		var val = view.value;
		timeSlider.activeHi = val.explin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var timeBoxHiText = StaticText()
		.string_("Time Hi").font_(Font("Monaco", 12));

		var timeBoxHiView = CompositeView()
		.layout_(VLayout(timeBoxHi, timeBoxHiText));

		var timeBoxView = CompositeView()
		.layout_(HLayout(timeBoxLoView, timeBoxHiView));

		var timeView = CompositeView()
		.layout_(VLayout(timeText, timeSlider, timeBoxView))
		.background_(Color.new255(154, 255, 154)).alpha_(0.5);

		//AMP STUFF//
		var ampText = StaticText()
		.string_("Amp Values").font_(Font("Monaco", 16));

		var ampSlider = RangeSlider()
		.orientation_('horizontal').action_({|view|
		var lo = view.lo, hi = view.hi;
		var maxLo = ampLoMax;
		var maxHi = ampHiMax;
		ampLo = lo.linexp(0.00, 1.00, maxLo, maxHi);
		ampHi = hi.linexp(0.00, 1.00, maxLo, maxHi);
		ampBoxLo.value = ampLo;
		ampBoxHi.value = ampHi;
		}).font_(Font("Monaco", 12)).keyDownAction_({""});

		var ampBoxLo = NumberBox().action_({|view|
		var maxLo = this.class.ampLoMax;
		var maxHi = this.class.ampHiMax;
		var val = view.value;
		ampSlider.activeLo = val.explin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var ampBoxLoText = StaticText()
		.string_("Amp Lo").font_(Font("Monaco", 12));

		var ampBoxLoView = CompositeView()
		.layout_(VLayout(ampBoxLo, ampBoxLoText));

		var ampBoxHi = NumberBox().action_({|view|
		var maxLo = this.class.ampLoMax;
		var maxHi = this.class.ampHiMax;
		var val = view.value;
		ampSlider.activeHi = val.explin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var ampBoxHiText = StaticText()
		.string_("Amp Hi").font_(Font("Monaco", 12));

		var ampBoxHiView = CompositeView()
		.layout_(VLayout(ampBoxHi, ampBoxHiText));

		var ampBoxView = CompositeView()
		.layout_(HLayout(ampBoxLoView, ampBoxHiView));

		var ampView = CompositeView()
		.layout_(VLayout(ampText, ampSlider, ampBoxView))
		.background_(Color.yellow).alpha_(0.5);

		//ATK STUFF//
		var atkText = StaticText()
		.string_("Attack Values")
		.font_(Font("Monaco", 16));

		var atkSlider = RangeSlider()
		.orientation_('horizontal').action_({|view|
		var lo = view.lo, hi = view.hi;
		var maxLo = atkLoMax;
		var maxHi = atkHiMax;
		atkLo = lo.linlin(0.00, 1.00, maxLo, maxHi);
		atkHi = hi.linlin(0.00, 1.00, maxLo, maxHi);
		atkBoxLo.value = atkLo;
		atkBoxHi.value = atkHi;
		}).font_(Font("Monaco", 12)).keyDownAction_({""});

		var atkBoxLo = NumberBox().action_({|view|
		var maxLo = this.class.atkLoMax;
		var maxHi = this.class.atkHiMax;
		var val = view.value;
		atkSlider.activeLo = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var atkBoxLoText = StaticText()
		.string_("Attack Lo").font_(Font("Monaco", 12));

		var atkBoxLoView = CompositeView()
		.layout_(VLayout(atkBoxLo, atkBoxLoText));

		var atkBoxHi = NumberBox().action_({|view|
		var maxLo = this.class.atkLoMax;
		var maxHi = this.class.atkHiMax;
		var val = view.value;
		atkSlider.activeHi = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var atkBoxHiText = StaticText()
		.string_("Attack Hi").font_(Font("Monaco", 12));

		var atkBoxHiView = CompositeView()
		.layout_(VLayout(atkBoxHi, atkBoxHiText));

		var atkBoxView = CompositeView()
		.layout_(HLayout(atkBoxLoView, atkBoxHiView));

		var atkView = CompositeView()
		.layout_(VLayout(atkText, atkSlider, atkBoxView))
		.background_(Color.new255(235, 150, 135)).alpha_(0.5);

		//PAN STUFF//
		var panText = StaticText()
		.string_("Pan Values").font_(Font("Monaco", 16));

		var panSlider = RangeSlider()
		.orientation_('horizontal').action_({|view|
		var lo = view.lo, hi = view.hi;
		var maxLo = panLoMax;
		var maxHi = panHiMax;
		panLo = lo.linlin(0.00, 1.00, maxLo, maxHi);
		panHi = hi.linlin(0.00, 1.00, maxLo, maxHi);
		panBoxLo.value = panLo;
		panBoxHi.value = panHi;
		}).font_(Font("Monaco", 12)).keyDownAction_({""});

		var panBoxLo = NumberBox().action_({|view|
		var maxLo = this.class.panLoMax;
		var maxHi = this.class.panHiMax;
		var val = view.value;
		panSlider.activeLo = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var panBoxLoText = StaticText()
		.string_("Pan Lo").font_(Font("Monaco", 12));

		var panBoxLoView = CompositeView()
		.layout_(VLayout(panBoxLo, panBoxLoText));

		var panBoxHi = NumberBox().action_({|view|
		var maxLo = this.class.panLoMax;
		var maxHi = this.class.panHiMax;
		var val = view.value;
		panSlider.activeHi = val.linlin(maxLo, maxHi, 0.00, 1.00);
		}).scroll_step_(0.01).decimals_(decNum).keyDownAction_({""});

		var panBoxHiText = StaticText()
		.string_("Pan Hi").font_(Font("Monaco", 12));

		var panBoxHiView = CompositeView()
		.layout_(VLayout(panBoxHi, panBoxHiText));

		var panBoxView = CompositeView()
		.layout_(HLayout(panBoxLoView, panBoxHiView));

		var panView = CompositeView()
		.layout_(VLayout(panText, panSlider, panBoxView))
		.background_(Color.new255(238, 210, 238)).alpha_(0.5);

		//POS STUFF//
		var posText = StaticText()
		.string_("Pos Amp Scalar").font_(Font("Monaco", 16));

		var posSlider = Slider().orientation_('horizontal').action_({|view|
		var val = view.value;
		var maxPos = this.class.posPreAmpMax;
		posPreAmp = val.linlin(0, 1, 0, maxPos);
		posBox.value = posPreAmp;
		}).keyDownAction_({""});

		var posBox = NumberBox().decimals_(decNum).keyDownAction_({""});

		var posView = CompositeView().layout_(
		VLayout(posText, posSlider, posBox)
		).background_(Color.white);

		var widthScalar = 1/12;

		var heightScalar = 1/16;

		var minWidth = widthScalar * windowWidth;

		var minHeight = heightScalar * windowWidth;

		//ARGS STUFF//
		var argsView = CompositeView().layout_(VLayout(
		rateView.minSize_(Size(minWidth, minHeight)),
		atkView.minSize_(Size(minWidth, minHeight)),
		timeView.minSize_(Size(minWidth, minHeight)),
		ampView.minSize_(Size(minWidth, minHeight)),
		panView.minSize_(Size(minWidth, minHeight)),
		posView.minSize_(Size(minWidth, minHeight))
		));

		//TEXT STUFF//
		var textView = TextView()
		.font_(Font("Monaco", 12))
		.focus(true)
		.editable_(false)
		.string_(
		(format("Active buffer name: %",
		PathName(activeBuffer.path).fileNameWithoutExtension)
		)++"\n\n"
		);

		var textComp = CompositeView()
		.layout_(HLayout(textView));

		text = textView;

		typingLayout = CompositeView()
		.layout_(HLayout(textComp, argsView));

		sliderDictionary = (
		rateSlider: rateSlider,
		atkSlider: atkSlider,
		timeSlider: timeSlider,
		ampSlider: ampSlider,
		panSlider: panSlider,
		posSlider: posSlider,
		);

		posSlider.valueAction = posPreAmp.linlin(0, posPreAmpMax, 0.0, 1.0);
		*/
	}

	free{
		if(window.isNil.not){
			window.onClose = nil;
			window.close;
			window = nil;
		};
	}

}
