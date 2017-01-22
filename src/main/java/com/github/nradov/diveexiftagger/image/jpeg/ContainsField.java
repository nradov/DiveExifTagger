package com.github.nradov.diveexiftagger.image.jpeg;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * May contain searchable metadata fields.
 *
 * @author Nick Radov
 */
public interface ContainsField {

    // TODO: Add getter methods for the other field types.

    /**
     * Search for a metadata field with a particular tag.
     * 
     * @param tag
     *            metadata field tag
     * @return any matching field values if they exist
     */
    @Nonnull
    public Optional<List<Rational>> getFieldRational(
            @Nonnull final FieldTag tag);

}
