package io.cnbper.seatax.auth;

import io.seata.core.protocol.RegisterRMRequest;
import io.seata.core.protocol.RegisterTMRequest;
import io.seata.core.rpc.netty.RegisterCheckAuthHandler;

public class DefaultRegisterCheckAuthHandler implements RegisterCheckAuthHandler {

    @Override
    public boolean regTransactionManagerCheckAuth(RegisterTMRequest request) {
        // TODO AS/SK认证
        // 校验分组
        return TransactionServiceGroupCache.contains(request.getTransactionServiceGroup());
    }

    @Override
    public boolean regResourceManagerCheckAuth(RegisterRMRequest request) {
        // TODO AS/SK认证
        // 校验分组
        return TransactionServiceGroupCache.contains(request.getTransactionServiceGroup());
    }
}
