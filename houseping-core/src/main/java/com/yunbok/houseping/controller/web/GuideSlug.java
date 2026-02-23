package com.yunbok.houseping.controller.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GuideSlug {

    SPECIAL_SUPPLY("special-supply"),
    POINT_VS_LOTTERY("point-vs-lottery"),
    HOMELESS_CRITERIA("homeless-criteria"),
    SUBSCRIPTION_ACCOUNT("subscription-account"),
    PRIVATE_VS_PUBLIC_HOUSING("private-vs-public-housing");

    private final String slug;

    public static boolean isValid(String slug) {
        return Arrays.stream(values()).anyMatch(g -> g.slug.equals(slug));
    }
}
