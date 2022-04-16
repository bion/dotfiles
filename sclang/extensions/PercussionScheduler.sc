PercussionSchedulerSample {
  classvar <>defaultAmp = 0.25, <>defaultOutbus = 0;
  var <path, <name, <>amp, <>schedulingOffset, <>outbus, <server, <nrt = false;
  var <buffer, <ready = false;
  var <duration, <sampleRate, <numFrames, <numChannels;

  *new { |path, name, amp, schedulingOffset, outbus, server, nrt|
    ^super
    .newCopyArgs(path, name, amp, schedulingOffset, outbus, server, nrt)
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
    server = server ? Server.default;

    if (schedulingOffset.isNil) {
      schedulingOffset = 0;
    };

    if (schedulingOffset > 0) {
      Error("Scheduling offset must be <= 0").throw;
    };

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

    if (nrt, {
      this.initNRT;
    }, {
      buffer = Buffer.read(server, path, action: { ready = true; });
    });

    ^this;
  }

  initNRT {
    var sf = SoundFile(path);

    if (sf.openRead.not, {
      Error("No soundfile found at" + path).throw;
    });

    numFrames = sf.numFrames;
    sampleRate = sf.sampleRate;
    numChannels = sf.numChannels;
    duration = sf.duration;
    sf.close;

    buffer = server.bufferAllocator.alloc(numChannels);
    ready = true;
  }
}

PercussionScheduler {
  var <busses, <server, <nrt;
  var <tempo = 100;
  var <clock, <measures, playMetronome;
  var <samples, measures, measuresSeq;
  var <percGroup;
  classvar metronomeSD;
  classvar playBufSD;

  *new { |busses, server, nrt = false|
    var serv = server ?? { Server.default };
    ^super.newCopyArgs(busses, serv, nrt).init;
  }

  *initClass {
    Server.default.doWhenBooted({
      metronomeSD.send(Server.default);
      playBufSD.send(Server.default);
    });

    metronomeSD = SynthDef(\PercussionSchedulerMetronomeSineStereo, {
      arg outbus, amp=0.2;
      var out, env;
      env = EnvGen.kr(Env.perc(0.001, 0.05, amp, -4), doneAction: Done.freeSelf);
      out = SinOsc.ar(1800, 0, env);
      Out.ar(outbus, out ! 2);
    });

    playBufSD = SynthDef(\PercussionSchedulerPlayBuf, {
      |outbus, buf, amp=0.2|
      var sig;

      sig = PlayBuf.ar(1, buf, BufRateScale.ir(buf), doneAction: Done.freeSelf);

      Out.ar(outbus, sig * amp);
    });
  }

  init {
    clock = TempoClock();
    this.setTempoBpm(tempo);
    samples = IdentityDictionary();
    busses = busses ? IdentityDictionary();

    ^this;
  }

  start {
    if (nrt) {
      "start doesn't work in NRT mode.".warn;
      ^nil;
    };

    if (measures.isNil, {
      "Measures have not been set.".warn;
      ^nil;
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
      this.makeMeasureBundles(
        measuresSeq.next,
        thisThread.clock.beatDur,
        { |timestamp, bundleList|
          server.listSendBundle(timestamp, bundleList);
        }
      );

      thisThread.clock.beatsPerBar;
    });
  }

  generateScore {
    var barDur = clock.beatsPerBar * clock.beatDur;
    var score = Score([
      [0.0, [\d_recv, metronomeSD.asBytes]],
      [0.0, [\d_recv, playBufSD.asBytes]]
    ]);
    var currentTime = barDur - clock.beatDur;

    if (nrt.not, {
      Error("Cannot generate score in realtime mode").throw;
    });

    samples.values.do { |sample|
      score.add(
        [0.0, [\b_allocRead, sample.buffer, sample.path, 0, 0]]
      );
    };

    measures.do { |measure|
      this.makeMeasureBundles(
        measure,
        clock.beatDur,
        { |timestamp, bundleList|
          var scheduleTime = timestamp + currentTime;

          bundleList.do { |oscMessage|
            score.add([scheduleTime, oscMessage]);
          };
        }
      );

      currentTime = currentTime + barDur;
    };

    score.add([currentTime + 2, [0]]);

    score.sort;

    ^score;
  }

  setTempoBpm { |newTempo|
    tempo = newTempo;
    clock.tempo_(newTempo / 60);
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

  makeMeasureBundles { |measure, beatDur, bundleCallback|
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
          this.makeMeasureBundles(val, beatDur, bundleCallback);
        },
        Function, {
          this.makeMeasureBundles(val.value(beat), beatDur, bundleCallback);
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
          if (bundleList.isEmpty.not, {
            bundleCallback.value(beatDur * beat, bundleList);
            bundleList = List();
          });

          beat = val;
        }
      );
    });

    bundleCallback.value(beatDur * beat, bundleList);
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
        sample = PercussionSchedulerSample(path, name, server: server, nrt: nrt);
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
    var path = specDict["path"] ?? {
      var wavPath = yamlPath.replace(".yml", ".wav");
      var aiffPath = yamlPath.replace(".yml", ".aiff");

      if (File.exists(wavPath)) {
        wavPath;
      } {
        aiffPath;
      };
    };
    var name = specDict["name"] ?? { PercussionSchedulerSample.sampleNameFromPath(path); };
    var amp = specDict["amp"];
    var outbusName = specDict["outbus"];
    var outbus = outbusName !? (this.findOrCreateBus(_)) ?? { PercussionSchedulerSample.defaultOutbus; };
    var schedulingOffset = specDict["offset"];
    var sample;

    if (path.isNil) {
      Error("PercussionSchedulerSample must have a path:" + specDict.asString).throw;
    };

    sample = PercussionSchedulerSample(path, name.asSymbol, amp, schedulingOffset, outbus, server, nrt);
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
