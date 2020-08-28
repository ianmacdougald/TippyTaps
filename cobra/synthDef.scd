SynthDef.new(\mono, {
	var timescale = \timescale.kr(1);
	var envTimeArray = [\atk.kr(0), \release.kr(1)].normalizeSum;
	var atk = envTimeArray[0], release = envTimeArray[1];
	var env = EnvGen.ar(Env.perc(
		atk, release, curve: \curve.kr(-4.0)
	), timeScale: timescale, doneAction: Done.freeSelf);
	var buf = \buf.kr(0);
	var frames = BufFrames.kr(buf);
	var end = timescale * SampleRate.ir;
	var phasor = Phasor.ar(
		0, BufRateScale.kr(buf) * \rate.kr(1),
		end: end) + (\pos.kr(0.5) * frames);
	var sig = BufRd.ar(1, buf, phasor, 1.0, 4);
	var out = sig * env * \amp.kr(0.5);
	OffsetOut.ar(\out.kr(0), Pan2.ar(out, \pan.kr(0)));
});
