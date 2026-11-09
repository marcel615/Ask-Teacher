package com.github.marcel615.askteacher.domain.auth.dto.response;

public record CsrfTokenResponse(
        String token,
        String headerName
) {
    @Override
    public String toString() {
        return "CsrfTokenResponse[token=REDACTED, headerName=" + headerName + "]";
    }
}
