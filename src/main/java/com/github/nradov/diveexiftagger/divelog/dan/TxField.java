package com.github.nradov.diveexiftagger.divelog.dan;

class TxField extends Field<Tx> implements Tx {

	TxField(final Segment parent, final String value) {
		super(parent, value);
	}

	class Repetition extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Tx> implements Tx {

		final String value;

		Repetition(final String value) {
			this.value = value;
		}

		@Override
		TxField getParent() {
			return TxField.this;
		}

		@Override
		public String toString() {
			return value;
		}

	}

	@Override
	protected Repetition createRepetition(final String value) {
		return new Repetition(value);
	}

}
