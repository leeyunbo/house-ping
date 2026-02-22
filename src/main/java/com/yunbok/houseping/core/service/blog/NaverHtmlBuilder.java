package com.yunbok.houseping.core.service.blog;

import com.yunbok.houseping.support.dto.BlogContentResult;
import org.springframework.stereotype.Component;

@Component
class NaverHtmlBuilder {

    String build(BlogContentResult content, Long postId, String baseUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family: 'Pretendard', -apple-system, sans-serif; max-width: 700px; margin: 0 auto;\">\n");
        html.append("<h2 style=\"font-size: 22px; font-weight: 700; margin-bottom: 8px;\">")
                .append(escapeHtml(content.getTitle()))
                .append("</h2>\n");
        html.append("<p style=\"color: #64748b; font-size: 14px; margin-bottom: 24px;\">")
                .append("하우스핑 | house-ping.com</p>\n");
        html.append("<p style=\"font-size: 15px; line-height: 1.8; margin-bottom: 24px;\">")
                .append("안녕하세요, 하우스핑입니다.<br>")
                .append("이번 주 서울/경기 지역에서 눈여겨볼 청약을 정리해 드립니다.</p>\n");

        for (BlogContentResult.BlogCardEntry entry : content.getEntries()) {
            html.append("<div style=\"margin-bottom: 32px;\">\n");
            html.append("<h3 style=\"font-size: 18px; font-weight: 600; margin-bottom: 12px;\">")
                    .append(entry.getRank()).append(". ")
                    .append(escapeHtml(entry.getHouseName()))
                    .append("</h3>\n");
            html.append("<img src=\"").append(baseUrl).append("/home/blog/").append(postId).append("/card/")
                    .append(entry.getRank()).append(".png\" ")
                    .append("alt=\"").append(escapeHtml(entry.getHouseName())).append("\" ")
                    .append("style=\"width: 100%; max-width: 600px; border-radius: 12px; margin-bottom: 12px;\" />\n");
            html.append("<p style=\"font-size: 14px; line-height: 1.8; white-space: pre-wrap;\">")
                    .append(escapeHtml(entry.getNarrativeText().replaceAll("\\[카드 이미지\\]\n*", "")))
                    .append("</p>\n");
            html.append("</div>\n");
        }

        html.append("<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;\" />\n");
        html.append("<p style=\"font-size: 14px; line-height: 1.8;\">")
                .append("더 자세한 분석은 <a href=\"https://house-ping.com\" style=\"color: #3182f6;\">house-ping.com</a>에서 확인하세요.</p>\n");
        html.append("<p style=\"font-size: 12px; color: #94a3b8; line-height: 1.6; margin-top: 16px;\">")
                .append("※ 본 콘텐츠는 청약Home·LH·국토교통부 공공데이터를 기반으로 작성되었으며, 정보의 정확성을 보장하지 않습니다. ")
                .append("시세 및 예상 차익은 주변 실거래가를 참고한 추정치이며, 실제와 다를 수 있습니다. ")
                .append("투자 및 청약 판단의 책임은 본인에게 있습니다.</p>\n");
        html.append("<p style=\"font-size: 13px; color: #64748b; margin-top: 12px;\">")
                .append("#청약 #분양 #서울청약 #경기청약 #청약분석</p>\n");
        html.append("</div>");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
