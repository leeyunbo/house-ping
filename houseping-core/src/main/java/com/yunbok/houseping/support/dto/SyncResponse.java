package com.yunbok.houseping.support.dto;

public record SyncResponse(
        int inserted,
        int updated,
        int skipped,
        int total
) {
    public static SyncResponse from(SyncResult result) {
        return new SyncResponse(
                result.inserted(),
                result.updated(),
                result.skipped(),
                result.total()
        );
    }
}
