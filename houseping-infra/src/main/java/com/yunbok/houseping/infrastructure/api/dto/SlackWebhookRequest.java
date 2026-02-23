package com.yunbok.houseping.infrastructure.api.dto;

import java.util.List;

public record SlackWebhookRequest(
        List<Block> blocks,
        String text
) {
    public record Block(String type, BlockText text) {}

    public record BlockText(String type, String text) {}

    public static SlackWebhookRequest of(String mrkdwn, String fallback) {
        return new SlackWebhookRequest(
                List.of(new Block("section", new BlockText("mrkdwn", mrkdwn))),
                fallback
        );
    }

    public static SlackWebhookRequest of(String mrkdwn) {
        return of(mrkdwn, mrkdwn);
    }
}
