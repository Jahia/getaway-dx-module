package org.jahia.modules.getaway.content.nodetypes.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PopulationDatesInitializer implements ModuleChoiceListInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PopulationDatesInitializer.class);

    private String key;

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {

        final Calendar now = new GregorianCalendar();

        final List<ChoiceListValue> choiceListValues = new ArrayList<ChoiceListValue>();
        final int nowYear = now.get(Calendar.YEAR);
        for (int i = nowYear; i >= 1900; i--) {
            final String yearAsString = Integer.toString(i);
            final ChoiceListValue value = new ChoiceListValue(yearAsString, yearAsString);
            if (i == nowYear) value.addProperty("defaultProperty", true);
            choiceListValues.add(value);
        }

        return choiceListValues;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }
}
