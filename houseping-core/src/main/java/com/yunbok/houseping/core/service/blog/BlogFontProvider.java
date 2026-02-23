package com.yunbok.houseping.core.service.blog;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;

@Slf4j
@Component
public class BlogFontProvider {

    private Font regularFont;
    private Font boldFont;

    @PostConstruct
    void init() {
        regularFont = loadFont("fonts/Pretendard-Regular.otf");
        boldFont = loadFont("fonts/Pretendard-Bold.otf");

        if (regularFont == null) {
            log.warn("Pretendard-Regular 로드 실패, 시스템 폰트로 대체");
            regularFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        }
        if (boldFont == null) {
            log.warn("Pretendard-Bold 로드 실패, 시스템 폰트로 대체");
            boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        }
    }

    public Font regular(float size) {
        return regularFont.deriveFont(Font.PLAIN, size);
    }

    public Font bold(float size) {
        return boldFont.deriveFont(Font.BOLD, size);
    }

    private Font loadFont(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            log.info("폰트 로드 성공: {}", path);
            return font;
        } catch (Exception e) {
            log.error("폰트 로드 실패: {}", path, e);
            return null;
        }
    }
}
