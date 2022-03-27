PercussionSchedulerSample {
	classvar server, <>defaultAmp = 0.25, <>defaultOutbus = 0;
	var <path, <name, <>amp, <>schedulingOffset, <>outbus;
	var <buffer, <ready = false;

	*initClass {
		server = Server.default;
	}

	*new { |path, name, amp, schedulingOffset, outbus|
		^super
  		.newCopyArgs(path, name, amp, schedulingOffset, outbus)
  		.init;
	}

  *sampleNameFromPath { |path|
		var basename = path.basename;
		var parts = basename.split($.);
		var sampleName = parts.first;
		var fileExt = parts[1];

  	if ((fileExt == "aiff").not && (fileExt == "wav").not, {
			Error("File not a sample" + path).throw;
  	});

		^sampleName;
  }

	printOn { |stream|
		stream << "A PercussionSchedulerSample(name="
		<< name << ", outbus=" << outbus << ")";
	}

	init {
		if (outbus.isNil) {
			outbus = defaultOutbus;
		};

		if (amp.isNil) {
			amp = defaultAmp;
		};

		if (name.isNil) {
			name = this.class.sampleNameFromPath(path);
		};
		name = name.asSymbol;

		buffer = Buffer.read(server, path, action: { ready = true; });
		^this;
	}
}

PercussionScheduler {
	classvar server;
	var <busses;
  var <clock, <measures, playMetronome;
  var <samples, measures, measuresSeq;
	var <percGroup;

  *new { |busses|
    ^super.newCopyArgs(busses).init;
  }

	*initClass {
		server = Server.default;
		server.waitForBoot({
			SynthDef(\PercussionSchedulerMetronomeSineStereo, {
				arg outbus, amp=0.2;
				var out, env;
				env = EnvGen.kr(Env.perc(0.001, 0.05, amp, -4), doneAction: Done.freeSelf);
				out = SinOsc.ar(1800, 0, env);
				Out.ar(outbus, out ! 2);
			}).send(server);

			SynthDef(\PercussionSchedulerPlayBuf, {
				|outbus, buf, amp=0.2|
				var sig;

				sig = PlayBuf.ar(1, buf, BufRateScale.ir(buf), doneAction: Done.freeSelf);

				Out.ar(outbus, sig * amp);
			}).send(server);
		});
	}

  init {
		clock = TempoClock.default;
		samples = IdentityDictionary();
		busses = busses ? IdentityDictionary();

		^this;
  }

  start {
		if (measures.isNil, {
			Error("Measures have not been set.").throw;
		});

		percGroup = Group(server);
		measuresSeq = Pseq(measures, inf).asStream;

		clock.beats = 2;

		// nil plays ASAP
		[nil, clock.beatDur].do { |bundleTimestamp|
			server.makeBundle(bundleTimestamp, {
				Synth(\PercussionSchedulerMetronomeSineStereo,
					[outbus: 0],
					percGroup);
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
				if (val.class == Symbol && samples.includesKey(val).not, {
					Error(val ++ " not found in samples").throw;
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
					var sample = samples[val];

					bundleList.add(
						Synth
						  .basicNew(\PercussionSchedulerPlayBuf, server)
  						.newMsg(percGroup, [
  							outbus: sample.outbus,
  							buf: sample.buffer,
  							amp: sample.amp
  						])
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

	addSample { |sample|
		samples.put(sample.name, sample);
		^this;
	}

  loadSamplesAtDir { |samplesDirPath|
		var samplePaths;
		var sample;

		if (samplesDirPath.endsWith("/*"), {
			samplePaths = samplesDirPath.pathMatch;
		}, {
			samplePaths = (samplesDirPath ++ "/*").pathMatch;
		});

		samplePaths.do { |path|
			if (path.endsWith(".yml")) {
				this.loadSampleFromYaml(path);
			};
		};

		samplePaths.do { |path|
			var basename = path.basename;
			var parts = basename.split($.);
			var name = parts.first.asSymbol;
			var fileExt = parts.last;
			var isAudioFile = (fileExt == "aiff") || (fileExt == "wav");
			var notLoaded = samples.keys.includes(name).not;

			if (isAudioFile && notLoaded, {
				sample = PercussionSchedulerSample(path, name);
				samples.put(sample.name, sample);
			});
		};
  }

	/*
	 Expects a dictionary of the following form:
		path: String
		name?: defaults to path basename as a symbol
		amp?: 0
		schedulingOffset?: 0
		outbus?: 0
	*/
	loadSampleFromYaml { |yamlPath|
		var specDict = yamlPath.parseYAMLFile;
		var path = specDict["path"] ?? { yamlPath.replace(".yml", ".wav"); };
		var name = specDict["name"] ?? { PercussionSchedulerSample.sampleNameFromPath(path); };
		var amp = specDict["amp"];
		var outbusName = specDict["outbus"];
		var outbus = outbusName !? (this.findOrCreateBus(_)) ?? { 0 };
		var schedulingOffset = specDict["schedulingOffset"] ?? 0;
		var sample;

		if (path.isNil) {
			Error("PercussionSchedulerSample must have a path:" + specDict.asString).throw;
		};

		sample = PercussionSchedulerSample(path, name.asSymbol, amp, schedulingOffset, outbus);
		samples.put(sample.name, sample);
		^this;
	}

	findOrCreateBus { |busName|
		var bus;

		busName = busName.asSymbol;

		if (busses.keys.includes(busName), {
			bus = busses[busName];
		}, {
			bus = Bus.audio(server, 1);
			busses.put(busName, bus);
		});

		^bus;
	}

  printSamples {
		samples.keys.asArray.sort.do { |bufferName|
			bufferName.postln;
		};
  }
}
