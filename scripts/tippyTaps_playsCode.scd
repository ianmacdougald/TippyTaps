//To run, make sure the Feedback quark is installed.
(
s.options.memSize = 2.pow(20);
s.quit;

Event.addEventType(\tippyTaps, {
	var tippy = ~tippyTaps.value;
	var char = ~char.value;
	if(tippy.notNil and: { char.notNil }, {
		defer{
			if(tippy.window.isClosed.not, {
				tippy.setChar(char);
			});
		};
		fork{
			(~dur.value * 0.99).wait;
			tippy.releaseSynth(char.asSymbol);
		};
	}, { format("There be no tippy taps, argh!").postln });
});

ServerTree.removeAll;
~make_nodes = {
	~to_delay = ~to_delay ?? { Bus.audio(s, 2) };
	~to_second_reverb = ~to_second_reverb ?? { Bus.audio(s, 2) };

	if(~tippy_taps.isNil, {
		~tippy_taps = TippyTaps(\ian_pad);
	}, { ~tippy_taps.reloadScripts });

	~tippy_taps.output_(~to_delay);

	Pdef(\ian_test).clear;
	~tippy_taps.window.onClose = { Pdef(\ian_test).stop };

	~fx_group = Group.after(s.defaultGroup);

	~short_delay = Synth(\delay, [
		\in, ~to_delay,
		\feedback, 1/2,
		\delay_time, 1/3,
		\out, 0,
	], ~fx_group);

	~long_delay0 = Synth(\delay, [
		\in, ~to_delay,
		\delay_time, 4.0,
		\feedback, 0.8,
		\ampDb, -16,
		// \wet, 1,
		\out, ~to_second_reverb,
	], ~fx_group);

	~long_delay1 = Synth(\delay, [
		\in, ~to_delay,
		\delay_time, 7.7,
		\feedback, 0.8,
		\ampDb, -16,
		\out, ~to_second_reverb
	], ~fx_group);

	~long_delay2 = Synth(\delay, [
		\in, ~to_delay,
		\delay_time, 12.666631,
		\feedback, 0.8,
		\ampDb, -16,
		\out, ~to_second_reverb
	], ~fx_group);

	~reverb = Synth(\reverb, [
		\in, 0,
		\wet, 0.2,
	], ~fx_group, \addToTail);

	~second_reverb = Synth(\reverb_alt, [
		\in, ~to_second_reverb,
		\wet, 0.8,
		\ampDb, -9,
		\decay, 16.0,
		\ffreq, 1200,
	], ~reverb, \addAfter);
};

ServerTree.removeAll;
ServerTree.add(~make_nodes);

s.waitForBoot({

	SynthDef(\reverb, {
		var in = In.ar(\in.kr(0), 2);
		ReplaceOut.ar(
			\out.kr(0),
			FreeVerb.ar(in, 1)
			.blend(in, 1 - \wet.kr(0.2, 0.2)).tanh * 0.8
		)
	}).add;

	SynthDef(\reverb_alt, {
		var in = In.ar(\in.kr(0), 2);
		Out.ar(
			\out.kr(0),
			FreeVerb.ar(in, 1)
			.blend(in, 1 - \wet.kr(0.5, 0.2)) * \ampDb.kr(-18).dbamp;
		);
	}).add;

	SynthDef(\delay, {
		var in = In.ar(\in.kr(0), 2);
		Out.ar(
			\out.kr(0),
			FbL({ | r |
				in + (r * \feedback.kr(0.5, 0.2))
			}, 16, \delay_time.kr(0.2, 0.2))
			.blend(in, 1 - \wet.kr(0.5, 0.2)) * \ampDb.kr(0).dbamp
		);
	}).add;

	s.sync;

	Pdef(\ian_test, Pbind(
		\type, \tippyTaps,
		\tippyTaps, ~tippy_taps,
		\char, Pseq([~text_code, \].flat, inf),
		\dur, Pxrand([1, Pn(0.2, 4), Pn(0.5, 2), 2.0, 4.0], inf) / 8
	)).play
});

~text_code = this.cmdLine;
)
