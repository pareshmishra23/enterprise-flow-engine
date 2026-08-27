package com.efe.traderecon.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "efe.security")
public class EfeSecurityProperties {
    private boolean enabled = false;
    private String issuerUri = "";
    private String audience = "efe-api";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getIssuerUri() { return issuerUri; }
    public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri == null ? "" : issuerUri; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
}
