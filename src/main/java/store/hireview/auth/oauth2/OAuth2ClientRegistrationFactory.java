package store.hireview.auth.oauth2;


import store.hireview.common.util.ScopeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

import static io.jsonwebtoken.lang.Strings.capitalize;

@Component
@RequiredArgsConstructor
public class OAuth2ClientRegistrationFactory {
    public ClientRegistration create(String registrationId, OAuth2Properties properties) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .redirectUri(properties.getRedirectUri())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(ScopeUtil.resolve(properties.getScope()))
                .authorizationUri(properties.getAuthorizationUri())
                .tokenUri(properties.getTokenUri())
                .userInfoUri(properties.getUserInfoUri())
                .userNameAttributeName(properties.getUserNameAttribute())
                .clientName(capitalize(registrationId))
                .build();
    }

}
