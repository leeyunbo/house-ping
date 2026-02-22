package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.domain.SubscriptionStatus;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.support.dto.PriceBadge;
import com.yunbok.houseping.support.dto.SubscriptionAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogCardImageGenerator {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final int MARGIN = 40;

    private static final Color BRAND_BLUE = new Color(0x31, 0x82, 0xF6);
    private static final Color BG_WHITE = new Color(0xFF, 0xFF, 0xFF);
    private static final Color TEXT_DARK = new Color(0x1E, 0x29, 0x3B);
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    private static final Color TEXT_LIGHT = new Color(0x94, 0xA3, 0xB8);
    private static final Color PRICE_BOX_BG = new Color(0xF8, 0xFA, 0xFC);
    private static final Color PRICE_BOX_BORDER = new Color(0xE2, 0xE8, 0xF0);
    private static final Color BADGE_CHEAP_BG = new Color(0xDC, 0xFC, 0xE7);
    private static final Color BADGE_CHEAP_FG = new Color(0x16, 0x65, 0x34);
    private static final Color BADGE_EXPENSIVE_BG = new Color(0xFE, 0xE2, 0xE2);
    private static final Color BADGE_EXPENSIVE_FG = new Color(0x99, 0x1B, 0x1B);
    private static final Color PROFIT_POSITIVE = new Color(0x16, 0xA3, 0x4A);
    private static final Color PROFIT_NEGATIVE = new Color(0xDC, 0x26, 0x26);
    private static final Color DIVIDER = new Color(0xE2, 0xE8, 0xF0);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d");

    private final BlogFontProvider fontProvider;

    public byte[] generateCardImage(SubscriptionAnalysisResult analysis, PriceBadge badge) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        try {
            Subscription sub = analysis.getSubscription();
            HouseTypeComparison rep = analysis.getRepresentativeComparison();
            boolean hasPrice = rep != null && rep.hasMarketData();

            // Background
            g.setColor(BG_WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // Brand stripe (top 6px)
            g.setColor(BRAND_BLUE);
            g.fillRect(0, 0, WIDTH, 6);

            // -- Badges (y=34) --
            int badgeX = drawStatusBadge(g, sub, MARGIN, 34);
            if (badge != null && badge != PriceBadge.UNKNOWN) {
                drawPriceBadge(g, badge, badgeX + 8, 34);
            }

            // -- House name (y=90) --
            g.setFont(fontProvider.bold(26));
            g.setColor(TEXT_DARK);
            String houseName = sub.getHouseName() != null ? sub.getHouseName() : "";
            g.drawString(houseName, MARGIN, 90);

            // -- Area + supply count (y=118) --
            g.setFont(fontProvider.regular(16));
            g.setColor(TEXT_MUTED);
            g.drawString(buildSubInfo(sub), MARGIN, 118);

            if (hasPrice) {
                drawWithPriceBox(g, sub, rep, analysis);
            } else {
                drawWithoutPriceBox(g, sub);
            }

            // Disclaimer
            g.setFont(fontProvider.regular(11));
            g.setColor(TEXT_LIGHT);
            g.drawString("※ 공공데이터 기반 추정치이며, 실제와 다를 수 있습니다. 투자 판단의 책임은 본인에게 있습니다.", MARGIN, HEIGHT - 48);

            // Footer: house-ping.com
            g.setFont(fontProvider.bold(14));
            g.setColor(BRAND_BLUE);
            g.drawString("house-ping.com", MARGIN, HEIGHT - 28);

        } catch (Exception e) {
            log.error("카드 이미지 생성 중 오류", e);
            drawErrorFallback(g);
        }

        g.dispose();
        return toPng(image);
    }

    /**
     * 시세 비교 데이터가 있는 경우의 레이아웃
     */
    private void drawWithPriceBox(Graphics2D g, Subscription sub, HouseTypeComparison rep,
                                  SubscriptionAnalysisResult analysis) {
        int boxY = 148;
        int boxW = WIDTH - MARGIN * 2;
        int boxH = 160;
        int col1 = MARGIN + 30;
        int col2 = MARGIN + 390;

        // Price box background
        g.setColor(PRICE_BOX_BG);
        g.fillRoundRect(MARGIN, boxY, boxW, boxH, 12, 12);
        g.setColor(PRICE_BOX_BORDER);
        g.drawRoundRect(MARGIN, boxY, boxW, boxH, 12, 12);

        // Labels
        g.setFont(fontProvider.regular(14));
        g.setColor(TEXT_MUTED);
        String supplyLabel = rep.getHouseType() != null ? "분양가 (" + rep.getHouseType() + ")" : "분양가";
        g.drawString(supplyLabel, col1, boxY + 30);
        g.drawString("시세", col2, boxY + 30);

        // Price values
        g.setFont(fontProvider.bold(24));
        g.setColor(TEXT_DARK);
        g.drawString(safeFormat(rep.getSupplyPriceFormatted()), col1, boxY + 62);

        // Arrow (Unicode →)
        g.setFont(fontProvider.regular(22));
        g.setColor(TEXT_LIGHT);
        int arrowX = col2 - 60;
        g.drawString("\u2192", arrowX, boxY + 62);

        g.setFont(fontProvider.bold(24));
        g.setColor(TEXT_DARK);
        g.drawString(safeFormat(rep.getMarketPriceFormatted()), col2, boxY + 62);

        // Estimated profit
        if (rep.getEstimatedProfit() != null) {
            g.setFont(fontProvider.bold(18));
            boolean positive = rep.getEstimatedProfit() > 0;
            g.setColor(positive ? PROFIT_POSITIVE : PROFIT_NEGATIVE);
            g.drawString("예상 차익: " + rep.getEstimatedProfitFormatted(), col1, boxY + 106);
        }

        // Transaction info
        String txInfo = rep.getTransactionInfo();
        if (txInfo != null) {
            g.setFont(fontProvider.regular(12));
            g.setColor(TEXT_LIGHT);
            g.drawString(txInfo, col1, boxY + 135);
        }

        // Schedule line below box
        int scheduleY = boxY + boxH + 34;
        drawScheduleAndDivider(g, sub, scheduleY);
    }

    /**
     * 시세 비교 데이터가 없는 경우의 레이아웃
     */
    private void drawWithoutPriceBox(Graphics2D g, Subscription sub) {
        int boxY = 148;
        int boxW = WIDTH - MARGIN * 2;
        int boxH = 80;

        // Info box
        g.setColor(PRICE_BOX_BG);
        g.fillRoundRect(MARGIN, boxY, boxW, boxH, 12, 12);
        g.setColor(PRICE_BOX_BORDER);
        g.drawRoundRect(MARGIN, boxY, boxW, boxH, 12, 12);

        g.setFont(fontProvider.regular(16));
        g.setColor(TEXT_MUTED);
        g.drawString("주변 시세 비교 데이터가 부족합니다", MARGIN + 30, boxY + 46);

        int scheduleY = boxY + boxH + 34;
        drawScheduleAndDivider(g, sub, scheduleY);
    }

    /**
     * 접수 일정 + 구분선
     */
    private void drawScheduleAndDivider(Graphics2D g, Subscription sub, int y) {
        g.setFont(fontProvider.regular(14));
        g.setColor(TEXT_MUTED);
        g.drawString(buildSchedule(sub), MARGIN, y);

        // Divider
        g.setColor(DIVIDER);
        g.fillRect(MARGIN, y + 20, WIDTH - MARGIN * 2, 1);
    }

    private int drawStatusBadge(Graphics2D g, Subscription sub, int x, int y) {
        String label = sub.getStatus() == SubscriptionStatus.ACTIVE ? "접수중" : "접수예정";
        Color bg = sub.getStatus() == SubscriptionStatus.ACTIVE ? BRAND_BLUE : TEXT_MUTED;
        return drawBadge(g, label, x, y, bg, Color.WHITE);
    }

    private void drawPriceBadge(Graphics2D g, PriceBadge badge, int x, int y) {
        Color bg = badge == PriceBadge.CHEAP ? BADGE_CHEAP_BG : BADGE_EXPENSIVE_BG;
        Color fg = badge == PriceBadge.CHEAP ? BADGE_CHEAP_FG : BADGE_EXPENSIVE_FG;
        drawBadge(g, badge.getLabel(), x, y, bg, fg);
    }

    private int drawBadge(Graphics2D g, String text, int x, int y, Color bg, Color fg) {
        g.setFont(fontProvider.bold(13));
        FontMetrics fm = g.getFontMetrics();
        int padX = 10, padY = 4;
        int badgeW = fm.stringWidth(text) + padX * 2;
        int badgeH = fm.getHeight() + padY * 2;

        g.setColor(bg);
        g.fillRoundRect(x, y, badgeW, badgeH, 8, 8);
        g.setColor(fg);
        g.drawString(text, x + padX, y + padY + fm.getAscent());

        return x + badgeW;
    }

    private String buildSubInfo(Subscription sub) {
        StringBuilder sb = new StringBuilder();
        if (sub.getArea() != null) {
            sb.append(sub.getArea());
        }
        if (sub.getTotalSupplyCount() != null) {
            if (!sb.isEmpty()) sb.append(" \u00B7 ");
            sb.append(String.format("%,d", sub.getTotalSupplyCount())).append("세대");
        }
        return sb.toString();
    }

    private String buildSchedule(Subscription sub) {
        StringBuilder sb = new StringBuilder();
        if (sub.getReceiptStartDate() != null) {
            sb.append("접수: ").append(sub.getReceiptStartDate().format(DATE_FMT));
            if (sub.getReceiptEndDate() != null) {
                sb.append(" ~ ").append(sub.getReceiptEndDate().format(DATE_FMT));
            }
        }
        if (sub.getWinnerAnnounceDate() != null) {
            if (!sb.isEmpty()) sb.append(" \u00B7 ");
            sb.append("당첨발표: ").append(sub.getWinnerAnnounceDate().format(DATE_FMT));
        }
        return sb.toString();
    }

    private void drawErrorFallback(Graphics2D g) {
        g.setColor(BG_WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(BRAND_BLUE);
        g.fillRect(0, 0, WIDTH, 6);
        g.setFont(fontProvider.regular(16));
        g.setColor(TEXT_MUTED);
        g.drawString("카드 이미지를 생성할 수 없습니다", MARGIN, HEIGHT / 2);
        g.setFont(fontProvider.bold(14));
        g.setColor(BRAND_BLUE);
        g.drawString("house-ping.com", MARGIN, HEIGHT - 30);
    }

    private String safeFormat(String value) {
        return value != null ? value : "-";
    }

    private byte[] toPng(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("PNG 변환 실패", e);
            return new byte[0];
        }
    }
}
