package com.github.nradov.diveexiftagger.divelog.dan;

/**
 * <p>
 * PDE Coded element.
 * </p>
 * <p>
 * Components: {@code <identifier (ST)>^<coding schemes (ST)>^<text(ST)>}
 * </p>
 * <p>
 * If the coding scheme is DL7, only identifier may be supplied. If not coded,
 * the ZCE takes format {@code ^^<text>(ST)}, or {@code ^<N>^<text>}.
 * </p>
 *
 * @author Nick Radov
 */
interface Zce extends DataType {

    /**
     * <p>
     * Identifier.
     * </p>
     * <p>
     * {@code <identifier (ST)>}: Sequence of characters (the code) that
     * uniquely identifies the item being referenced by the {@code <text>}.
     * Different coding schemes will have different elements here.
     * </p>
     */
    St getIdentifier();

    /**
     * <p>
     * Coding schemes.
     * </p>
     * <p>
     * {@code <coding schemes (ST)>}: S = DL7 Standard; A = Application specific
     * code; N = not coded, free text only.
     * </p>
     */
    St getCodingSchemes();

    /**
     * <p>
     * Text.
     * </p>
     * <p>
     * {@code <text(ST)>}: free text, not required.
     * </p>
     */
    St getText();

}
