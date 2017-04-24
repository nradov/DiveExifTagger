package com.github.nradov.diveexiftagger.image.jpeg.exif;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Directory entry field type.
 *
 * @author Nick Radov
 */
abstract class DataType {

    abstract int getLength();

    @Nonnull
    static String toString(@Nonnull final List<? extends DataType> l) {
        if (l.isEmpty()) {
            throw new IllegalArgumentException("empty collection");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (final DataType o : l) {
            sb.append(o);
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 2).concat("]");
    }

}
