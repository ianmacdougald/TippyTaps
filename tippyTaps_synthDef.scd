//If you want to control an argument from the gui,
//register a corresponding ControlSpec object for it.
SynthDef(\synth, {
	//The following control will not be accessible from the gui:
	var freq = \freq.kr(400, spec: \freq.asSpec);
	//The following control will be:
	var phase = \phase.kr(0);
	Out.ar(
		\out.kr(0),
		SinOsc.ar(freq, phase)
		* Env.perc.ar(doneAction: 2)
		* 0.2 ! 2
	);
});

	