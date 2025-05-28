package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PhotoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Photo getPhotoSample1() {
        return new Photo().id(1L).title("title1").description("description1").location("location1").keywords("keywords1");
    }

    public static Photo getPhotoSample2() {
        return new Photo().id(2L).title("title2").description("description2").location("location2").keywords("keywords2");
    }

    public static Photo getPhotoRandomSampleGenerator() {
        return new Photo()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .location(UUID.randomUUID().toString())
            .keywords(UUID.randomUUID().toString());
    }
}
