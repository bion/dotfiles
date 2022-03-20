s.options.memSize_(100000);
Server.local.options.numOutputBusChannels = 20;
s.boot;

s.waitForBoot({
	SynthDef(\bufPreviewerMono, {
		|buf|
		Out.ar([0, 1], PlayBuf.ar(1, buf, BufRateScale.ir(buf), doneAction: Done.freeSelf) * 0.2);
	}).send(s);
	SynthDef(\bufPreviewerStereo, {
		|buf|
		Out.ar([0, 1], PlayBuf.ar(2, buf, BufRateScale.ir(buf), doneAction: Done.freeSelf) * 0.2);
	}).send(s);
});

~previewBuffers = Dictionary();

~playSampleBuffer = { |filePath|
	var buf = ~previewBuffers[filePath];

	switch(buf.numChannels,
		1, { Synth(\bufPreviewerMono, [buf: buf]); },
		2, { Synth(\bufPreviewerStereo, [buf: buf]); },
		{ Error("Soundfile has " ++ buf.numChannels ++ " channels.").throw; }
	);
};

~sampleBuffer = { |filePath|
	if (~previewBuffers[filePath].isNil.not, {
		~playSampleBuffer.value(filePath);
	}, {
		~previewBuffers.put(
			filePath,
			Buffer.read(s, filePath, action: { ~playSampleBuffer.value(filePath) })
		);
	});
};

("Platform.systemExtensionDir:" + Platform.systemExtensionDir).postln;
