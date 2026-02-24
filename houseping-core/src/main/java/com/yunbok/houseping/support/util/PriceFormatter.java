package com.yunbok.houseping.support.util;

public final class PriceFormatter {

    private PriceFormatter() {
    }

    public static String format(long amount) {
        if (amount >= 10000) {
            long uk = amount / 10000;
            long rest = amount % 10000;
            if (rest == 0) {
                return uk + "억";
            }
            return uk + "억 " + String.format("%,d", rest) + "만";
        }
        return String.format("%,d", amount) + "만";
    }

    public static String formatWithWon(long amount) {
        if (amount >= 10000) {
            long uk = amount / 10000;
            long rest = amount % 10000;
            if (rest == 0) {
                return uk + "억 원";
            }
            return uk + "억 " + String.format("%,d", rest) + "만 원";
        }
        return String.format("%,d", amount) + "만 원";
    }
}
