package util;

public class DomainUtils {
    public static String extractDomain(String userAtDomain) {
        return userAtDomain.substring(userAtDomain.indexOf('@') + 1);
    }

    public static boolean isDistantDomain(String target, String localDomain) {
        String targetDomain = target.contains("@")
                ? extractDomain(target)
                : target;
        String senderDomain = localDomain.contains("@")
                ? extractDomain(localDomain)
                : localDomain;
        return !targetDomain.equals(senderDomain);
    }
}
