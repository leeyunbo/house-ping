package com.yunbok.houseping.infrastructure.support;

import java.util.Set;

public final class LhResidentialFilter {

    private static final Set<String> NON_RESIDENTIAL_KEYWORDS = Set.of(
            "상가", "어린이집", "가스충전소", "용지", "입점자", "임차운영자"
    );

    private LhResidentialFilter() {}

    public static boolean isResidential(String projectName) {
        if (projectName == null || projectName.isBlank()) {
            return false;
        }
        return NON_RESIDENTIAL_KEYWORDS.stream().noneMatch(projectName::contains);
    }
}
