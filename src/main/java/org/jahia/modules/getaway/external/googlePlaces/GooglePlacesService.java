package org.jahia.modules.getaway.external.googlePlaces;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jahia.modules.getaway.external.LandmarksProvider;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GooglePlacesService implements LandmarksProvider {

    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);
    private static final String CACHE_KEY_SEPARATOR = "@@@";
    private static final String CACHE_NAME = "Google-places-cache";

    private static GooglePlacesService ourInstance = new GooglePlacesService();

    private Cache cache;
    private EhCacheProvider ehCacheProvider;
    private long cacheTti = 24L * 3600L; // 1 day;

    private String api_key;
    private GeoApiContext geoApiContext;

    private GooglePlacesService() {
    }

    public static GooglePlacesService getInstance() {
        return ourInstance;
    }

    private void start() {
        if (cache == null) {
            cache = ehCacheProvider.getCacheManager().getCache(CACHE_NAME);
            if (cache == null) {
                ehCacheProvider.getCacheManager().addCache(CACHE_NAME);
                cache = ehCacheProvider.getCacheManager().getCache(CACHE_NAME);
                cache.getCacheConfiguration().setTimeToIdleSeconds(cacheTti);
            }
        }

        final GeoApiContext.Builder builder = new GeoApiContext.Builder();
        builder.apiKey(api_key);
        geoApiContext = builder.build();
    }

    private void stop() {
        if (cache != null) cache.flush();
        geoApiContext.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getLandmarks(String destination, String countryCode, Locale locale) {
        if (destination == null || countryCode == null)
            throw new IllegalArgumentException("The destination and the country code must be defined both");

        final String cacheKey = getCacheKey(destination, countryCode, locale);
        final Element cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null) {
            return (Map<String, String>) cacheEntry.getObjectValue();
        }

        final String country = new Locale(Locale.ENGLISH.getLanguage(), countryCode).getDisplayCountry(Locale.ENGLISH);
        final Map<String, String> landmarks = getLandmarksFromGoogle(destination, country, locale);
        if (landmarks != null) cache.put(new Element(cacheKey, landmarks));
        return landmarks;
    }

    private String getCacheKey(String destination, String countryCode, Locale locale) {
        return destination.trim().toLowerCase() + CACHE_KEY_SEPARATOR + countryCode.trim().toLowerCase() + CACHE_KEY_SEPARATOR + LanguageCodeConverters.localeToLanguageTag(locale);
    }

    /**
     * Get all landmarks for a city in a specified country, using the specified locale for the name of the landmarks
     *
     * @param city
     * @param country
     * @param locale
     * @return a Map where the keys are the placeID of the landmarks, and the value the localized name. Returns null if an error occured.
     */
    private Map<String, String> getLandmarksFromGoogle(String city, String country, Locale locale) {
        final String query = String.format("things to do in %s, %s", city, country);
        TextSearchRequest request = PlacesApi.textSearchQuery(geoApiContext, query);
        String nextPageToken;
        try {
            final Map<String, String> landmarks = new HashMap<>();
            while (true) {
                final PlacesSearchResponse response = request.await();
                nextPageToken = response.nextPageToken;
                for (PlacesSearchResult result : response.results) {
                    landmarks.put(result.placeId, result.name);
                }
                if (nextPageToken == null) break;
                request = PlacesApi.textSearchNextPage(geoApiContext, nextPageToken);
                logger.debug("Let's go with one more page");
                Thread.sleep(2000L); // According to PlacesSearchResponse.nextPageToken documentation, "There is a short delay between when this response is issued, and when nextPageToken will become valid to execute."
            }
            return landmarks;
        } catch (ApiException | InterruptedException | IOException e) {
            logger.error("An error occured while retrieving the landmarks from GooglePlaces", e);
        }
        return null;
    }

    /**
     * Get the geographic coordinates localisation for a city in a specified country
     *
     * @param city
     * @param country
     * @param locale
     * @return
     */
    private LatLng getGeoCoordinates(String city, String country, Locale locale) {
        final String cityInCountry = String.format("%s, %s", city, country);
        final TextSearchRequest request = PlacesApi.textSearchQuery(geoApiContext, cityInCountry);
        request.language(locale.getLanguage());
        try {
            final PlacesSearchResponse response = request.await();
            final PlacesSearchResult result = response.results[0];
            return result.geometry.location;
        } catch (ApiException | InterruptedException | IOException e) {
            logger.error("An error occured while retrieving the geo coordinates from GooglePlaces", e);
        }
        return null;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    public void setCacheTti(long cacheTti) {
        this.cacheTti = cacheTti;
    }
}
