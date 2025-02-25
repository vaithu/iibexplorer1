package com.myapp.iib.my;

import com.holonplatform.core.Context;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.i18n.LocalizationContext;
import com.holonplatform.core.i18n.TemporalFormat;
import com.holonplatform.core.property.*;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.vaadin.flow.components.Input;
import com.holonplatform.vaadin.flow.components.PropertyListing;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

public class MyUtils {

    public static final String CHANGES_UPDATED_SUCCESS = "Changes updated successfully";
    public static final String DELETE_CONFIRMATIN = "Are you sure want to delete?";

    public static void write(Object text) {
        System.out.println(text);
    }

    public static Datastore getDatastore() {

        Datastore datastore = Context.get().resource(Datastore.class)
                .orElseThrow(() -> new IllegalStateException("Cannot retrieve Datastore from Context."));
        return datastore;
    }

    public static Object getClassFromContext(Class<? extends Object> c) {
        return Context.get().resource(c).orElseThrow(() -> new IllegalStateException("Cannot retrieve " + c.getName() + " from Context."));
    }

    public static QueryFilter queryFilter(Input<String> searchField, StringProperty... properties) {

        String searchFilter = searchField.getValue();

        if (searchFilter != null && !searchFilter.isEmpty()) {

            for (StringProperty property : properties) {
                return property.containsIgnoreCase(searchField.getValue());
            }
        }

        return null;

    }

    public static boolean addFilter(Input<String> searchField, PropertyBox propertyBox, StringProperty... properties) {

       return searchField.getValue() == null ||  Arrays.stream(properties).
               anyMatch(stringProperty ->
               StringUtils.containsIgnoreCase(propertyBox.getValue(stringProperty), searchField.getValue()));

    }


    public static PropertyBox copyPropertyBox(PropertySet<?> properties, PropertyBox propertyBox) {
        return PropertyBox.builder(properties).copyValues(propertyBox).build();
    }

    public static PropertySet<?> getPropertySet(Property... properties) {
        return PropertySet.of(properties);
    }

    public static Double getColumnSumDouble(Datastore datastore, DataTarget<?> target, NumericProperty<Double> property) {
        return datastore.query(target)
                .findOne(property.sum())
                .orElse(0.00);
    }

    public static String getColumnSumInt(Datastore datastore, DataTarget<?> target, NumericProperty<Integer> property) {
        return String.valueOf(datastore.query(target)
                .findOne(property.sum())
                .orElse(0))
                ;
    }

    public static String format(Number number) {
        return NumberFormat.getInstance().format(number);
    }

    @SneakyThrows
    public static Double reformat(String number) {
        return NumberFormat.getInstance().parse(number).doubleValue();
//        LocalizationContext context = LocalizationContext.builder().build();
//        return Double.parseDouble(context.format(NumberUtils.createNumber(number), NumberFormatFeature.DISABLE_GROUPING));
    }

    public static void updateColumnSum(PropertyListing listing, NumericProperty property, String value) {
        listing.getFooter().ifPresent(items -> {
            items.getFirstRow().filter(o -> o.getCell(property).isPresent())
                    .ifPresent(o -> o.getCell(property).get().setText(value));
        });
    }

    public static String getCurrencySymbol() {
        return NumberFormat.getCurrencyInstance().getCurrency().getSymbol();
        //new Locale("en", "IN")
    }


    public static String dateToLocalDateTime(Date date) {
        LocalizationContext context = LocalizationContext.builder()
                .withInitialSystemLocale()
                .withDefaultDateTemporalFormat(TemporalFormat.FULL)
                .withDefaultTimeTemporalFormat(TemporalFormat.SHORT)
                .build();

        return context.format(date, TemporalType.DATE_TIME);
        /*return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();*/
    }

    public static String formatObjectEnumeration(Enumeration paramEnumeration) {
        String str;
        if (paramEnumeration != null) {
            StringBuffer localStringBuffer = new StringBuffer("[");
            int i = 0;
            while (paramEnumeration.hasMoreElements()) {
                if (i++ != 0) {
                    localStringBuffer.append(", ");
                }
                localStringBuffer.append(paramEnumeration.nextElement());
            }
            localStringBuffer.append("]");
            str = localStringBuffer.toString();
        } else {
            str = "null";
        }
        return str;
    }

    public static String formatObjectMap(
            Map<? extends Object, ? extends Object> paramMap) {
        String str;
        if (paramMap != null) {
            StringBuffer localStringBuffer = new StringBuffer("[");
            int i = 1;
            for (Map.Entry localEntry : paramMap.entrySet()) {
                if (i == 0) {
                    localStringBuffer.append(",\n");
                } else {
                    localStringBuffer.append("\n");
                }
                i = 0;
                localStringBuffer.append("" + localEntry.getKey() + "="
                        + localEntry.getValue());
            }
            if (i == 0) {
                localStringBuffer.append("\n");
            }
            localStringBuffer.append("]");
            str = localStringBuffer.toString();
        } else {
            str = "null";
        }
        return str;
    }

    public static String formatKey(String key) {
        return " ---------------------> " + key + " : <---------------------";
    }
}
