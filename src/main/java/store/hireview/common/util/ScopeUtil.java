package store.hireview.common.util;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScopeUtil {

    public static String[] resolve(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return new String[0];
        }
        return scopes.toArray(new String[0]);
    }
}