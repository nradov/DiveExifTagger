package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * Diver's address.
 *
 * @author Nick Radov
 */
final class Zpa extends Segment {

	private final XadField diverAddress;
	private final TnField phoneNumberHome;
	private final TnField phoneNumberBusiness;
	private final StField emailAddress;
	private final ZceField primaryLanguage;
	private final IsField citizenship;

	Zpa(final Zxl parent, final String value) {
		super(parent);
		final String[] fields = splitFields(value);
		diverAddress = fields.length >= 2 ? new XadField(this, fields[1]) : null;
		phoneNumberHome = fields.length >= 3 ? new TnField(this, fields[2]) : null;
		phoneNumberBusiness = fields.length >= 4 ? new TnField(this, fields[3]) : null;
		emailAddress = fields.length >= 5 ? new StField(this, fields[4]) : null;
		primaryLanguage = fields.length >= 6 ? new ZceField(this, fields[5]) : null;
		citizenship = fields.length >= 7 ? new IsField(this, fields[6]) : null;
	}

	/**
	 * Get the diver's address.
	 *
	 * @return diver's address
	 */
	public XadField getDiverAddress() {
		return diverAddress;
	}

	/**
	 * Get the diver's home phone number.
	 * 
	 * @return diver's home phone numer
	 */
	public TnField getPhoneNumberHome() {
		return phoneNumberHome;
	}

	/**
	 * Get the diver's business phone number.
	 * 
	 * @return diver's business phone number
	 */
	public TnField getPhoneNumberBusiness() {
		return phoneNumberBusiness;
	}

	/**
	 * Get the diver's e-mail address.
	 * 
	 * @return diver's e-mail address
	 */
	public StField getEmailAddress() {
		return emailAddress;
	}

	/**
	 * Get the diver's primary language.
	 * 
	 * @return diver's primary language
	 */
	public ZceField getPrimaryLanguage() {
		return primaryLanguage;
	}

	/**
	 * Get the diver's citizenship.
	 * 
	 * @return diver's citizenship
	 */
	public IsField getCitizenship() {
		return citizenship;
	}

}
