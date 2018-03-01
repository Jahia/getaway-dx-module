package org.jahia.modules.getaway.googlePlaces;

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

public class GooglePlaces {



    private     static  final   Logger  logger           =    LoggerFactory.getLogger(GooglePlaces.class);
    //////////////// GOOGLE API PLACES ////////////////
    private     static  final   String  API_URL          =   "https://maps.googleapis.com/maps/api/place/";
    private     static  final   String  API_KEY          =   "AIzaSyCsUSJZoTQWcptM3jzZrq6256I_ieMhYzQ"; //TODO add it to the constructor  to not use the one write here
    private     static  final   String  API_URL_FT       =   "%s%s/json?%s";
    private     static final    String  KEY_RESULTS      =    "results";
    private     static final    String  KEY_GEOMETRY     =    "geometry";
    private     static final    String  KEY_LOCATION     =    "location";
    private     static final    String  KEY_LATITUDE     =    "lat";
    private     static final    String  KEY_LONGITUDE    =    "lng";
    private     static final    String  STRING_PLACE_ID  =    "place_id";
    private     static final    String  STRING_NAME      =    "name";
    // METHODS
    private     static  final   String  NEARBY           =   "nearbysearch";
    private     static  final   String  TEXTSEARCH       =   "textsearch";
    private     static  final   String  RADIUS           =   "500";

    //Constructor
    public GooglePlaces() {
        //TODO set the API_KEY with the constructor
        //API_KEY = api_key;
    }

    /**
     * Add Extra Param to the URL
     * @param base
     * @param extraParams
     * @return
     */
    private static String addExtraParams(String base, HashMap<String,String> extraParams) {
        for (Map.Entry<String,String> param : extraParams.entrySet())
            base += "&" + param.getKey() + (param.getValue() != null ? "=" + param.getValue() : "");
        return base;
    }

    /**
     * Build the URL with extraParms
     * @param method
     * @param locale
     * @param params
     * @param extraParams
     * @return
     */
    private static String buildUrl(String method, Locale locale, String params, HashMap<String,String> extraParams) {
        String url = String.format(locale, API_URL_FT, API_URL, method, params);
        url = addExtraParams(url, extraParams);
        url = url.replace(' ', '+');
        return url;
    }

    /**
     * Build the URL with no extraParams
     * @param method
     * @param locale
     * @param params
     * @return
     */
    private static String buildUrl(String method, Locale locale, String params) {
        String url = String.format(locale, API_URL_FT, API_URL, method, params);
        url = url.replace(' ', '+');
        return url;
    }

    /**
     * Get All LandMarks from a city a country, and the current local
     * @param city
     * @param country
     * @param mock
     * @param locale
     * @return all LandMarks in a Map
     * @throws Exception
     */
    public Map<String, Map<String, String>> getPlaces(String city, String country, Map<String, Map<String, String>> mock, Locale locale) throws Exception {
        HashMap<String,String> extraParams = new HashMap<String,String>();
        extraParams.put("key",API_KEY);

        Map<String, Map<String, String>> places = mock;
        if(null==places){
            places = new HashMap<>();
        }
        String city_country = city+"_"+country;
        //If the city is already in the mock we used the one in the mock
        if(null==mock.get(city_country)){
            final Map<String, String> cityLandMarks = new HashMap<>();
            String cityInCountry = city + " in " + country;


            Map<String,Double> latLng = getLocalisation(cityInCountry,locale,extraParams);
            if(!latLng.isEmpty()){
                places.put(city_country, cityLandMarks);
                String paramLandMarks = "location="+latLng.get("lat")+","+latLng.get("lng");
                extraParams.put("radius",RADIUS);
                String URILandMarks = buildUrl(NEARBY,locale,paramLandMarks, extraParams);
                extraParams.remove("radius");

                try {
                    JSONObject cityJson =   getJson(URILandMarks);
                    JSONArray results   =   cityJson.getJSONArray(KEY_RESULTS);
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String placeId = result.getString(STRING_PLACE_ID);
                        String name = result.optString(STRING_NAME);
                        cityLandMarks.put(placeId,name);
                    }

                } catch (Exception e) {
                    logger.info("An error occur getting the landMarks : " + e.toString());
                    e.printStackTrace();
                }
            }

        }
        return places;
    }

    /**
     * Get localisation from a city and a country in full text search  "City in country"
     * @param city
     * @param locale
     * @param extraParams
     * @return
     */
    private Map<String,Double> getLocalisation(String city, Locale locale, HashMap<String,String> extraParams) {
        Map<String,Double> latLng = new HashMap<String, Double>();
        String paramCity = "input="+city;
        String URICity = buildUrl(TEXTSEARCH,locale,paramCity, extraParams);

        try {
            JSONObject cityJson = getJson(URICity);
            JSONObject result =  cityJson.getJSONArray(KEY_RESULTS).getJSONObject(0);
            JSONObject location = result.getJSONObject(KEY_GEOMETRY).getJSONObject(KEY_LOCATION);
            Double lat = location.getDouble(KEY_LATITUDE);
            Double lng = location.getDouble(KEY_LONGITUDE);
            latLng.put("lat",lat);
            latLng.put("lng",lng);
            logger.debug("GetLocalisation for the city : " + city + " lat : " + lat +  " lng : " + lng );
        } catch (Exception e) {
            logger.debug("An error occur getting the localistion : " + e.toString());
            e.printStackTrace();
        }

        return latLng;
    }

    /**
     * Make the call to the API and return the JSON
     * @param URL
     * @return
     * @throws Exception
     */
    private JSONObject getJson(String URL)throws Exception{
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

}
