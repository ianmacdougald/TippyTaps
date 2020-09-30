var s = Server.default;
var scale = Scale.zhi
.degrees.collect({ | item | item + [0, 7, 14, 21, 28] })
.flat.sort.as(FloatArray);
var buffer = Buffer.alloc(
	s,
	scale.size,
	1,
	{ | b | b.setnMsg(0, scale) }
);
SynthDef(\synth, {
	var atk = \atk.kr(
		0.2,
		spec: ControlSpec(0.05, 4.0)
	);
	var release = \release.kr(
		1,
		spec: ControlSpec(0.2, 4.0)
	);
	var timescale = \timescale.kr(
		1,
		spec: ControlSpec(0.5, 2.0, \exp)
	);
	var env = Env.perc(atk, release)
	.ar(
		timeScale: timescale,
		doneAction: Done.freeSelf
	);
	var freq = DegreeToKey.kr(
		buffer.bufnum,
		\freq.kr(
			0,
			spec: ControlSpec(0, scale.size * 4, \lin, 1)
		) % scale.size,
		12,
		1,
		48
	).midicps;
	var ffreq = (freq * ExpRand(1.5, 4.0)).clip(20.0, 16000.0);
	var width_freq = timescale * \width_freq.kr(
		1.0,
		spec: ControlSpec(0.25, 0.75, \exp)
	);
	var width = SinOsc.kr(width_freq)
	.linexp(-1.0, 1.0, 0.25, 0.75);
	var sig = RLPF.ar(
		Pulse.ar(freq, width),
		ffreq,
		\rq.kr(
			0.125,
			spec: ControlSpec(0.09, 0.2, \exp)
		)
	);
	sig = sig * env
	* \ampDb.kr(
		-12,
		spec: ControlSpec(-24, -48)
	).dbamp;
	sig = Pan2.ar(sig, Rand(-1.0, 1.0));
	Out.ar(\out.kr(0), sig);
});