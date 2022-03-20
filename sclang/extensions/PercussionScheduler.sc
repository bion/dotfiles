PercussionScheduler {
  var <clock, <measures, playMetronome;
  var buffers, measures, measuresSeq, server;

  init {
	server = Server.default;
	clock = TempoClock.default;
	buffers = IdentityDictionary();

	server.waitForBoot({
	  SynthDef(\PercussionSchedulerMetronomeSineStereo, {
		arg outbus, amp=0.2;
		var out, env;
		env = EnvGen.kr(Env.perc(0.001, 0.05, amp, -4), doneAction: Done.freeSelf);
		out = SinOsc.ar(1800, 0, env);
		Out.ar(outbus, out ! 2);
	  }).send(server);

	  SynthDef(\PercussionSchedulerPlayBuf, {
		|outbus, buf, amp=0.2, pan=0|
		var sig;

		sig = PlayBuf.ar(1, buf, BufRateScale.ir(buf), doneAction: Done.freeSelf);

		Out.ar(outbus, Pan2.ar(sig * amp, pan));
	  }).send(server);
	});

	^this;
  }

  start {
	if (measures.isNil, {
	  Error("Measures have not been set.").throw;
	});

	measuresSeq = Pseq(measures, inf).asStream;

	clock.beats = 2;

	// nil plays ASAP
	[nil, clock.beatDur].do { |bundleTimestamp|
	  server.makeBundle(bundleTimestamp, {
		Synth(\PercussionSchedulerMetronomeSineStereo, [outbus: 0]);
	  });
	};

	clock.sched(1, {
	  this.scheduleMeasure(measuresSeq.next);
	  thisThread.clock.beatsPerBar;
	});
  }

  setTempoBpm { |tempo|
	clock.tempo_(tempo / 60);
  }

  stop {
	clock.clear;
  }

  setMeasures { |measuresArg|
	measuresArg.do { |measure|
	  measure.do { |val|
		if (val.class == Symbol && buffers.includesKey(val).not, {
		  Error(val ++ " not found in buffers").throw;
		});
	  };
	};

	measures = measuresArg;
  }

  scheduleMeasure { |measure|
    var beatDur = thisThread.clock.beatDur;
	var bundleList = List();
	var beat = nil;

	if (measure.isNil, {
	  Error("Measure is nil.").throw;
	});

	if (measure.first.isNumber.not, {
	  Error("Measures must start with a beat number: " ++ measure.asString).throw;
	});

	measure.do({ |val|
	  switch(val.class,
		Array, {
		  this.scheduleMeasure(val);
		},
		Function, {
		  this.scheduleMeasure(val.value(beat));
		},
		Symbol, {
		  bundleList.add(
			Synth.basicNew(\PercussionSchedulerPlayBuf).newMsg(server, [buf: buffers[val]])
		  );
		},
		{
		  if (beat.notNil, {
			server.listSendBundle(beatDur * beat, bundleList);
			bundleList = List();
		  });

		  beat = val;
		}
	  );
	});

	server.listSendBundle(beatDur * beat, bundleList);
  }

  loadSamplesAtDir { |samplesDirPath|
	samplesDirPath.pathMatch.do { |path|
	  var basename = path.basename;
	  var parts = basename.split($.);
	  var bufName = parts.first;
	  var fileExt = parts[1];

	  if ((fileExt == "aiff").not && (fileExt == "wav").not, {
		Error("Unable to load sample:" + path).throw;
	  });

	  buffers.put(bufName.asSymbol, Buffer.read(server, path));
	};
  }

  printSamples {
	buffers.keys.asArray.sort.do { |bufferName|
	  bufferName.postln;
	};
  }
}
