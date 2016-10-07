package com.github.nradov.diveexiftagger.divelog.dan;

import java.time.Instant;

interface Dt extends DataType {

    Instant toInstant();

}
