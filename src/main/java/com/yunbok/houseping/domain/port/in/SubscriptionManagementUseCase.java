package com.yunbok.houseping.domain.port.in;

import com.yunbok.houseping.domain.model.SyncResult;

public interface SubscriptionManagementUseCase {

    SyncResult sync();

    int cleanup();
}
