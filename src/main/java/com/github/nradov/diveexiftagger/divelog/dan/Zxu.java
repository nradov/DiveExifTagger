package com.github.nradov.diveexiftagger.divelog.dan;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Zxu extends Message {

    private final Fsh fsh;
    private final Zrh zrh;
    private final Zar zar;
    final List<? extends DiveGroupZxu> diveGroup = new ArrayList<>();

    public Zxu(final Path path) {
        throw new UnsupportedOperationException();
    }

    int getDiveGroupSize() {
        return diveGroup.size();
    }

    DiveGroupZxu getDiveGroup(final int index) {
        return diveGroup.get(index);
    }

    public Fsh getFsh() {
        return fsh;
    }

    public Zar getZar() {
        return zar;
    }

    public Zrh getZrh() {
        return zrh;
    }

    @Override
    St getFieldSeparator() {
        return fsh.getFieldSeparator();
    }

    @Override
    St getEncodingCharacters() {
        return fsh.getEncodingCharacters();
    }

}
