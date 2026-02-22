package com.yunbok.houseping.support.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BlogContentResult {

    private final LocalDate generatedDate;
    private final String title;
    private final String blogText;
    private final List<BlogCardEntry> entries;

    @Getter
    @Builder
    public static class BlogCardEntry {
        private final Long subscriptionId;
        private final String houseName;
        private final int rank;
        private final String narrativeText;
        private final byte[] cardImage;
    }
}
