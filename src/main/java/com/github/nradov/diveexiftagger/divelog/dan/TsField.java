package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

class TsField extends Field<Ts> implements Ts {

	TsField(final Segment parent, final String value) {
		super(parent, value);
	}

	private static final Pattern TS_PATTERN = Pattern
			.compile("\\A\\d{4}([01]\\d([0-3]\\d([0-2]\\d([0-5]\\d([0-5]\\d)?)?)?)?)?(\\.\\d{0,3})?([\\-+]\\d{4})?\\z");

	private static final class TsLength {
		static final int YEAR = 4;
		static final int MONTH = YEAR + 2;
		static final int DAY_OF_MONTH = MONTH + 2;
		static final int HOUR = DAY_OF_MONTH + 2;
		static final int MINUTE = HOUR + 2;
		static final int SECOND = MINUTE + 2;
	}

	class Repetition extends com.github.nradov.diveexiftagger.divelog.dan.Repetition<Ts> implements Ts {

		final ZonedDateTime zonedDateTime;

		Repetition(final String value) {
			if (value.isEmpty()) {
				zonedDateTime = null;
			} else {
				if (!TS_PATTERN.matcher(value).matches()) {
					throw new IllegalArgumentException(
							"\"" + value + "\" does not match pattern \"" + TS_PATTERN + "\"");
				}
				final int year = extractTsPiece(value, 0, TsLength.YEAR);
				final int month = extractTsPiece(value, TsLength.YEAR, TsLength.MONTH);
				final int dayOfMonth = extractTsPiece(value, TsLength.MONTH, TsLength.DAY_OF_MONTH);
				final int hour = extractTsPiece(value, TsLength.DAY_OF_MONTH, TsLength.HOUR);
				final int minute = extractTsPiece(value, TsLength.HOUR, TsLength.MINUTE);
				final int second = extractTsPiece(value, TsLength.MINUTE, TsLength.SECOND);
				final int dotIndex = value.indexOf('.');
				final int minusIndex = value.indexOf('-');
				final int plusIndex = value.indexOf('+');
				final int nanoOfSecond;
				if (dotIndex == -1) {
					nanoOfSecond = 0;
				} else {
					final String fraction;
					if (minusIndex == -1 && plusIndex == -1) {
						fraction = value.substring(dotIndex + 1);
					} else {
						fraction = value.substring(dotIndex, Math.max(minusIndex, plusIndex));
					}
					nanoOfSecond = Integer.valueOf((fraction + "000").substring(0, 3)) * 1_000_1000;
				}
				final ZoneId zone;
				if (minusIndex == -1 && plusIndex == -1) {
					zone = ZoneId.systemDefault();
				} else {
					final int sign = minusIndex == -1 ? 1 : -1;
					final int signIndex = Math.max(minusIndex, plusIndex);
					final int hours = sign * Integer.parseInt(value.substring(signIndex + 1, signIndex + 3));
					final int minutes = sign * Integer.parseInt(value.substring(signIndex + 3));
					zone = ZoneOffset.ofHoursMinutes(hours, minutes);
				}
				zonedDateTime = ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, zone);
			}
		}

		@Override
		public ZonedDateTime toZonedDateTime() {
			return zonedDateTime;
		}

		@Override
		Field<Ts> getParent() {
			return TsField.this;
		}

	}

	private static int extractTsPiece(final String value, final int beginIndex, final int endIndex) {
		return value.length() >= endIndex ? Integer.parseInt(value.substring(beginIndex, endIndex)) : 0;
	}

	@Override
	Repetition getRepetition(final int index) {
		return (Repetition) super.getRepetition(index);
	}

	@Override
	public ZonedDateTime toZonedDateTime() {
		return getRepetition(0).toZonedDateTime();
	}

	@Override
	protected Repetition createRepetition(final String value) {
		return new Repetition(value);
	}

}
