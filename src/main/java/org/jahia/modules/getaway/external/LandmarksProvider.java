package org.jahia.modules.getaway.external;

import java.util.Locale;
import java.util.Map;

public interface LandmarksProvider {
    Map<String, String> getLandmarks(String destination, String countryCode, Locale locale);
}
