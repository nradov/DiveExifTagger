package com.github.nradov.diveexiftagger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

public class Test {

    @org.junit.Test
    public void test() throws IOException {
        final ImageInputStream inStream = ImageIO
                .createImageInputStream(new File(
                        "C:\\Users\\nradov\\git\\DiveExifTagger\\src\test\\resources\\IMG_8687.JPG"));
        final Iterator<ImageReader> imgItr = ImageIO.getImageReaders(inStream);

        while (imgItr.hasNext()) {
            final ImageReader reader = imgItr.next();
            reader.setInput(inStream, true);
            final IIOMetadata metadata = reader.getImageMetadata(0);

            final String[] names = metadata.getMetadataFormatNames();
            final int length = names.length;
            for (int i = 0; i < length; i++) {
                System.out.println("Format name: " + names[i]);
            }
        }
    }

}
