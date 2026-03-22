package util;

public class DomainUtils {
    public static String extractDomain(String userAtDomain) {
        return userAtDomain.substring(userAtDomain.indexOf('@') + 1);
    }

    public static boolean isDistantDomain(String target, String localDomain) {
        String domain = target.contains("@")
                ? extractDomain(target)
                : localDomain;
        return !domain.equals(localDomain);
    }
}
