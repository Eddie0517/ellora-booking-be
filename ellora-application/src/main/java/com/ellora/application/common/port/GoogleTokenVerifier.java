package com.ellora.application.common.port;

import com.ellora.application.feature.auth.dto.GoogleUserInfo;

public interface GoogleTokenVerifier {

    GoogleUserInfo verify(String credential);
}
