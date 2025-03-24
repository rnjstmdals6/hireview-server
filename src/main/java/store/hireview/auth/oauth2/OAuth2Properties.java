package store.hireview.auth.oauth2;

public interface OAuth2Properties {
    String getClientId();
    String getClientSecret();
    String getRedirectUri();
    String getAuthorizationUri();
    String getTokenUri();
    String getUserInfoUri();
    String getUserNameAttribute();
    java.util.List<String> getScope();
}