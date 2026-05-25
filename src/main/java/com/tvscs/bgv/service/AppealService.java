package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.dto.request.RespondAppealRequest;
import com.tvscs.bgv.domain.dto.request.SubmitAppealRequest;
import com.tvscs.bgv.domain.dto.response.AppealResponse;
import com.tvscs.bgv.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppealService {
    AppealResponse submit(Long verifierId, SubmitAppealRequest req);
    Page<AppealResponse> listAll(String status, Pageable pageable);
    AppealResponse getById(String appealId);
    AppealResponse respond(String appealId, RespondAppealRequest req, UserPrincipal admin);
}
