//If you want to control an argument from the gui,
//register a corresponding ControlSpec object for it.
SynthDef(\synth, {
	var buf = \buf.kr(0, spec: ControlSpec(0, 32));
	//The following control will be:
	var timescale = \timescale.kr(1, spec: ControlSpec(0.05, 1.0, \exp));
	var frames = BufFrames.kr(buf);
	var atk = \atk.kr(0, spec: ControlSpec(0.001, 0.5, \exp));
	var release = \release.kr(1, spec: ControlSpec(0.1, 1.0, \exp));
	var env = Env.perc(atk, release).ar(
		timeScale: timescale,
		doneAction: Done.freeSelf
	);
	var phasor = Phasor.ar(
		0,
		BufRateScale.kr(buf) * \rate.kr(1, spec: ControlSpec(0.1, 8.0, \exp)),
		0,
		timescale * SampleRate.ir
	) + (\pos.kr(0.5, spec: ControlSpec(0.0, 4.0)) * frames).wrap(0, frames);
	var sig = BufRd.ar(
		2,
		buf,
		phasor
	);
	sig = sig * env * \ampDB.kr(-12, spec: ControlSpec(-24, 0)).dbamp;
	Out.ar(\out.kr(0), sig)
})
