+ SimpleNumber {
  midicpsWithOctave {arg octave;
	^(this + ((octave + 1) * 12)).midicps;
  }
}
