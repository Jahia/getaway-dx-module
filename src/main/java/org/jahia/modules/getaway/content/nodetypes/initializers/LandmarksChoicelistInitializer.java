package org.jahia.modules.getaway.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.getaway.googlePlaces.GooglePlaces;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LandmarksChoicelistInitializer implements ModuleChoiceListInitializer {

    private static final Logger logger = LoggerFactory.getLogger(LandmarksChoicelistInitializer.class);
    private static Map<String, Map<String, String>> mock = new HashMap<>();
    private static final String DESTINATION_NAME_PROPERTY = "destinationname";
    private static final String COUNTRY_PROPERTY = "country";

    private String key;

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        logger.debug("Building the landmarks list");
        final List<ChoiceListValue> myChoiceList = new ArrayList<ChoiceListValue>();

        if (context == null) {
            return myChoiceList;
        }

        String destName =   null;
        try {
            destName = getString(DESTINATION_NAME_PROPERTY,context);
        } catch (RepositoryException e) {
            logger.error("", e);
            return myChoiceList;
        }


        String country  =   null;
        try {
            country = getString(COUNTRY_PROPERTY,context);
        } catch (RepositoryException e) {
            logger.error("", e);
            return myChoiceList;
        }

        if (StringUtils.isBlank(destName) || StringUtils.isBlank(country)) return myChoiceList;


        //TODO add the API KEY in the constructor
        GooglePlaces googlePlaces = new GooglePlaces();
        try {
            //We use City and Country to get the landMarks
            mock = googlePlaces.getPlaces(destName.trim().toLowerCase(), country.trim().toLowerCase(),mock,Locale.ENGLISH);
        } catch (Exception e) {
            logger.info("An error occure during googlePlaces.getPlaces : "+ e.toString());
        }
        String city_country = destName.trim().toLowerCase() +"_"+country.trim().toLowerCase();
        final Map<String, String> landmarks = getLandmarks(city_country, Locale.ENGLISH); // TODO crappy, the destName is i18n, we should use a normalized and not localized key
        if (landmarks == null) return myChoiceList;

        HashMap<String, Object> myPropertiesMap = null;
        for (String lmKey : landmarks.keySet()) {
            final String lmLabel = landmarks.get(lmKey);
            myPropertiesMap = new HashMap<String, Object>();
            myChoiceList.add(new ChoiceListValue(lmLabel, myPropertiesMap, new ValueImpl(lmKey, PropertyType.STRING, false)));
        }
        return myChoiceList;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    private Map<String, String> getLandmarks(String destination, Locale locale) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("Loading the landmarks for %s with the label in %s", destination, locale));
            // the locale is ignored for the mock, but should be handled when requesting the actual API
            return mock.get(destination);
    }

    /**
     * Get String from the context
     * @param property
     * @param context
     * @return
     * @throws RepositoryException
     */
    private String getString (String property, Map<String, Object> context) throws RepositoryException{
        if (context.containsKey(property)) {
            return ((List<String>) context.get(property)).get(0);
        } else {
            final JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
            if (node != null && node.hasProperty(property)) {
                return  node.getPropertyAsString(property);
            }
        }
        return null;
    }


}

