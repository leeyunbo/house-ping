package com.yunbok.houseping.domain.model;

/**
 * 데이터 동기화 결과를 나타내는 불변 Value Object
 */
public record SyncResult(int inserted, int updated, int skipped) {

    /**
     * 빈 결과 생성
     */
    public static SyncResult empty() {
        return new SyncResult(0, 0, 0);
    }

    /**
     * 전체 처리 건수
     */
    public int total() {
        return inserted + updated + skipped;
    }

    /**
     * 변경사항이 있는지 확인
     */
    public boolean hasChanges() {
        return inserted > 0 || updated > 0;
    }

    /**
     * 다른 결과와 병합
     */
    public SyncResult merge(SyncResult other) {
        return new SyncResult(
            this.inserted + other.inserted,
            this.updated + other.updated,
            this.skipped + other.skipped
        );
    }
}
