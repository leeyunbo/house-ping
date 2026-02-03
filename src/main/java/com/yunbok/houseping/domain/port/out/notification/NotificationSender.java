package com.yunbok.houseping.domain.port.out.notification;

/**
 * 알림 발송을 위한 통합 아웃바운드 포트
 *
 * @see MessageSender 일반 메시지 발송
 * @see SubscriptionNotifier 청약 정보 알림
 * @see ErrorNotifier 에러 알림
 * @see DailyReportNotifier 일일 종합 리포트 알림
 */
public interface NotificationSender extends MessageSender, SubscriptionNotifier, ErrorNotifier, DailyReportNotifier {
    // 모든 알림 기능이 필요한 경우 이 인터페이스 사용
    // 특정 기능만 필요한 경우 개별 인터페이스 사용 권장
}
