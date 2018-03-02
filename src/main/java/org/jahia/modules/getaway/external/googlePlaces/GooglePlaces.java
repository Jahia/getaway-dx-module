package org.jahia.modules.getaway.external.googlePlaces;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jahia.modules.getaway.external.LandmarksProvider;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GooglePlaces implements LandmarksProvider {

    private static final Logger logger = LoggerFactory.getLogger(GooglePlaces.class);
    //////////////// GOOGLE API PLACES ////////////////
    private static final String API_URL = "https://maps.googleapis.com/maps/api/place/";
    private static final String API_URL_FT = "%s%s/json?%s";
    private static final String KEY_RESULTS = "results";
    private static final String KEY_GEOMETRY = "geometry";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LATITUDE = "lat";
    private static final String KEY_LONGITUDE = "lng";
    private static final String STRING_PLACE_ID = "place_id";
    private static final String STRING_NAME = "name";
    // METHODS
    private static final String NEARBY = "nearbysearch";
    private static final String TEXTSEARCH = "textsearch";
    private static final String RADIUS = "500";

    private Cache cache;
    private EhCacheProvider ehCacheProvider;
    private static final String CACHE_NAME = "Google-places-cache";
    private long cacheTti = 24L * 3600L; // 1 day;
    private static final String CACHE_KEY_SEPARATOR = "@@@";

    private static LandmarksProvider ourInstance = new GooglePlaces();

    private String api_key;

    private GooglePlaces() {
    }

    public static LandmarksProvider getInstance() {
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
    }

    private void stop() {
        if (cache != null) cache.flush();
    }

    @Override
    public Map<String, String> getLandmarks(String destination, String countryCode, Locale locale) {
        if (destination == null || countryCode == null) throw new IllegalArgumentException("The destination and the country code must be defined both");
        
        final String cacheKey = getCacheKey(destination, countryCode, locale);
        final Element cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null) {
            return (Map<String, String>) cacheEntry.getObjectValue();
        }

        final Map<String, String> landmarks = getLandmarksFromGoogle(destination, countryCode, locale);
        cache.put(new Element(cacheKey, landmarks));
        return landmarks;
    }

    private String getCacheKey(String destination, String countryCode, Locale locale) {
        return destination.trim().toLowerCase() + CACHE_KEY_SEPARATOR + countryCode.trim().toLowerCase() + CACHE_KEY_SEPARATOR + LanguageCodeConverters.localeToLanguageTag(locale);
    }

    /**
     * Add Extra Param to the URL
     *
     * @param base
     * @param extraParams
     * @return
     */
    private String addExtraParams(String base, HashMap<String, String> extraParams) {
        for (Map.Entry<String, String> param : extraParams.entrySet())
            base += "&" + param.getKey() + (param.getValue() != null ? "=" + param.getValue() : "");
        return base;
    }

    /**
     * Build the URL with extraParms
     *
     * @param method
     * @param locale
     * @param params
     * @param extraParams
     * @return
     */
    private String buildUrl(String method, Locale locale, String params, HashMap<String, String> extraParams) {
        String url = String.format(locale, API_URL_FT, API_URL, method, params);      // TODO why locale here?
        url = addExtraParams(url, extraParams);
        url = url.replace(' ', '+');
        return url;
    }

    /**
     * Build the URL with no extraParams
     *
     * @param method
     * @param locale
     * @param params
     * @return
     */
    private String buildUrl(String method, Locale locale, String params) {
        String url = String.format(locale, API_URL_FT, API_URL, method, params);
        url = url.replace(' ', '+');
        return url;
    }

    /**
     * Get All LandMarks from a city a country, and the current local
     *
     * @param city
     * @param country
     * @param locale
     * @return all LandMarks in a Map
     * @throws Exception
     */
    private Map<String, String> getLandmarksFromGoogle(String city, String country, Locale locale) {
        final HashMap<String, String> extraParams = new HashMap<String, String>();
        extraParams.put("key", api_key);
        final Map<String, String> landmarks = new HashMap<>();
        final String cityInCountry = city + " in " + country;

        final Map<String, Double> geoCoordinates = getGeoCoordinates(cityInCountry, locale, extraParams);
        if (!geoCoordinates.isEmpty()) {
            extraParams.put("radius", RADIUS);
            final String paramLandmarks = "location=" + geoCoordinates.get("lat") + "," + geoCoordinates.get("lng");
            final String URILandmarks = buildUrl(NEARBY, locale, paramLandmarks, extraParams);

            try {
                final JSONObject cityJson = getJson(URILandmarks);
                final JSONArray results = cityJson.getJSONArray(KEY_RESULTS);
                for (int i = 0; i < results.length(); i++) {
                    final JSONObject result = results.getJSONObject(i);
                    final String placeId = result.getString(STRING_PLACE_ID);
                    final String name = result.optString(STRING_NAME);
                    landmarks.put(placeId, name);
                }

            } catch (Exception e) {
                logger.info("An error occur getting the landmarks", e);
            }
        }
        return landmarks;
    }

    /**
     * Get localisation from a city and a country in full text search  "City in country"
     *
     * @param city
     * @param locale
     * @param extraParams
     * @return
     */
    private Map<String, Double> getGeoCoordinates(String city, Locale locale, HashMap<String, String> extraParams) {
        Map<String, Double> latLng = new HashMap<String, Double>();
        String paramCity = "input=" + city;
        String URICity = buildUrl(TEXTSEARCH, locale, paramCity, extraParams);

        try {
            JSONObject cityJson = getJson(URICity);
            JSONObject result = cityJson.getJSONArray(KEY_RESULTS).getJSONObject(0);
            JSONObject location = result.getJSONObject(KEY_GEOMETRY).getJSONObject(KEY_LOCATION);
            Double lat = location.getDouble(KEY_LATITUDE);
            Double lng = location.getDouble(KEY_LONGITUDE);
            latLng.put("lat", lat);
            latLng.put("lng", lng);
            logger.debug("GetLocalisation for the city : " + city + " lat : " + lat + " lng : " + lng);
        } catch (Exception e) {
            logger.debug("An error occur getting the localistion : " + e.toString());
            e.printStackTrace();
        }

        return latLng;
    }

    /**
     * Make the call to the API and return the JSON
     *
     * @param URL
     * @return
     * @throws Exception
     */
    private JSONObject getJson(String URL) throws Exception {
        URL obj = new URL(URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        con.setConnectTimeout(1000);

        int responseCode = con.getResponseCode();
        logger.debug("\nSending 'GET' request to URL : " + URL);
        logger.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonObject = new JSONObject(response.toString());

        //print result
        logger.debug(response.toString());

        return jsonObject;
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
