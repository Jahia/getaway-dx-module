package org.jahia.modules.getaway.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
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
    private static Map<String, Map<String, String>> mock;
    private static final String DESTINATION_NAME_PROPERTY = "destinationname";

    private String key;

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        logger.debug("Building the landmarks list");
        final List<ChoiceListValue> myChoiceList = new ArrayList<ChoiceListValue>();

        if (context == null) {
            return myChoiceList;
        }

        String destName = null;
        if (context.containsKey(DESTINATION_NAME_PROPERTY)) {
            destName = ((List<String>) context.get(DESTINATION_NAME_PROPERTY)).get(0);
        } else {
            try {
                final JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
                if (node != null && node.hasProperty(DESTINATION_NAME_PROPERTY)) {
                    destName = node.getPropertyAsString(DESTINATION_NAME_PROPERTY);
                }
            } catch (RepositoryException e) {
                logger.error("", e);
                return myChoiceList;
            }
        }

        if (StringUtils.isBlank(destName)) return myChoiceList;
        final Map<String, String> landmarks = getLandmarks(destName.trim().toLowerCase(), Locale.ENGLISH); // TODO crappy, the destName is i18n, we should use a normalized and not localized key
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

    private void fillMock() {
        mock = new HashMap<>();

        final Map<String, String> reykjavik = new HashMap<>();
        mock.put("reykjavik", reykjavik);
        reykjavik.put("hallgrimskirkja", "Hallgrímskirkja");
        reykjavik.put("harpa", "Harpa");
        reykjavik.put("voyageur-du-soleil", "Le Voyageur du Soleil");
        reykjavik.put("phallologique", "Musée phallologique islandais");

        final Map<String, String> geneva = new HashMap<>();
        mock.put("geneva", geneva);
        geneva.put("jet-d-eau", "Jet d'eau");
        geneva.put("mur-reformateurs", "Mur des réformateurs");
        geneva.put("cern", "CERN");
        geneva.put("onu", "Palais des Nations");

        final Map<String, String> nyc = new HashMap<>();
        mock.put("new york", nyc);
        nyc.put("central-park", "Central Park");
        nyc.put("times-square", "Times Square");
        nyc.put("empire-states", "Empire States building");
    }
}
