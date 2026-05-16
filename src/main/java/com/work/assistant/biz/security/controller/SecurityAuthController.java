package com.work.assistant.biz.security.controller;

import com.work.assistant.biz.security.metadata.SecurityAuthRequest;
import com.work.assistant.common.exception.AppErrorCode;
import com.work.assistant.common.exception.WorkAssistantException;
import com.work.assistant.common.utils.AesEncryptorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
public class SecurityAuthController {

    @PostMapping
    public void getToken(@RequestBody SecurityAuthRequest securityAuthRequest) throws WorkAssistantException {

        if (!StringUtils.isNoneBlank(securityAuthRequest.getPassword(), securityAuthRequest.getUserName())) {
            throw new WorkAssistantException(AppErrorCode.LOGIN_FAILED, "Username or password cannot be empty");
        }
        if (!StringUtils.endsWithIgnoreCase(securityAuthRequest.getUserName(), "AIDAN") ||
                !StringUtils.endsWithIgnoreCase(securityAuthRequest.getPassword(), AesEncryptorUtils.decrypt("Zx1JaTnseEJouY9tmWHnSg=="))) {
            throw new WorkAssistantException(AppErrorCode.LOGIN_FAILED, "Username or password is wrong");
        }
    }
}
