//If you want to control an argument from the gui,
//register a corresponding ControlSpec object for it.
SynthDef(\mono, {
	//The following control will not be accessible from the gui:
	var buf = \buf.kr(0);
	//The following control will be:
	var timescale = \timescale.kr(1, spec: ControlSpec(0.05, 4.0, \exp));
	var frames = BufFrames.kr(buf);
	var atk = \atk.kr(0, spec: ControlSpec(0.05, 1.0, \exp));
	var release = \release.kr(1, spec: ControlSpec(0.05, 1.0, \exp));
	var env = Env.perc(atk, release).ar(
		timeScale: timescale,
		doneAction: Done.freeSelf
	);
	var phasor = Phasor.ar(
		0,
		BufRateScale.kr(buf) * \rate.kr(1, spec: ControlSpec(0.05, 8.0, \exp)),
		0,
		timescale * SampleRate.ir
	) + (\pos.kr(0.5, spec: ControlSpec(0.0, 4.0)) * frames).wrap(0, frames);
	var sig = BufRd.ar(
		1,
		buf,
		phasor
	);
	sig = sig * env * \ampDB.kr(-12, spec: \db.asSpec);
	sig = Pan2.ar(sig, \pan.kr(0, spec: ControlSpec(-1.0, 1.0)));
	Out.ar(\out.kr(0), sig)
})
