package com.github.nradov.diveexiftagger.image.jpeg;

import java.nio.ByteOrder;

/**
 * Functional interface to data type constructors. This makes it easy to create
 * a map from numeric data type values to the corresponding data type
 * constructors.
 *
 * @author Nick Radov
 */
@FunctionalInterface
interface DataTypeSupplier {

    /**
     * Constructor which creates a new data type value for a metadata field.
     *
     * @param bytes
     *            raw data from the image file (TIFF or any other format such as
     *            JPEG/Exif which uses the TIFF metadata format)
     * @param offset
     *            position of the value with {@code bytes}
     * @param byteOrder
     *            little- or big-endian (will be ignored for data types shorter
     *            than 32 bits)
     * @return a new instance of a subclass
     */
    DataType construct(byte[] bytes, int offset, ByteOrder byteOrder);

}
