package com.assetpulse.backend.market;

import com.assetpulse.backend.common.model.User;
import com.assetpulse.backend.holding.HoldingService;
import com.futu.openapi.pb.TrdCommon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRefreshService {
    private final AccountDataService accountDataService;
    private final FutuAccountRepository futuAccountRepository;

    private User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }

    public void refreshAccounts() throws Exception {
        User user = getCurrentUser();
        List<TrdCommon.TrdAcc> accList = accountDataService.getAccountList();

        for (TrdCommon.TrdAcc trdAcc : accList) {
            boolean isExist = futuAccountRepository.existsByAccIdAndUserId(trdAcc.getAccID(), user.getId());
            if (!isExist) {
                FutuAccount futuAccount = FutuAccount.builder().user(user).accId(trdAcc.getAccID()).currency("USD").build();
                futuAccountRepository.save(futuAccount);
            }
        }
    }
}
