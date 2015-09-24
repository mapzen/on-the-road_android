package com.mapzen.helpers;

import com.mapzen.valhalla.Router.DistanceUnits;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formats a distance in meters according to the following rules:
 * <ul>
 *     <li>Distances over 0.1 miles return distance in miles with a max of one decimal place.</li>
 *     <li>Distances under 0.1 miles return distance in feet rounded down to the nearest 10.</li>
 *     <li>Distances under 10 feet return the actual value in feet in normal (list) mode.</li>
 *     <li>Distances under 10 feet return "now" in real-time (navigation) mode.</li>
 * </ul>
 */
public final class DistanceFormatter {
    public static final double METERS_IN_ONE_MILE = 1609.0;
    public static final double METERS_IN_ONE_FOOT = 0.3048;
    public static final double FEET_IN_ONE_MILE = 5280;

    private static DecimalFormat decimalFormat;

    private DistanceFormatter() {
    }

    /**
     * Convenience wrapper for {@link #format(int, boolean)} with {@code real-time} equal to
     * {@code false}.
     *
     * @param distanceInMeters the actual distance in meters.
     * @return distance string formatted according to the rules of hte formatter.
     */
    public static String format(int distanceInMeters) {
        return format(distanceInMeters, false);
    }

    /**
     * Format a distance for display in either normal (list) or real-time (navigation) mode.
     *
     * @param distanceInMeters the actual distance in meters.
     * @param realTime boolean flag for navigation vs. list view.
     * @return distance string formatted according to the rules of the formatter.
     */
    public static String format(int distanceInMeters, boolean realTime) {
        Locale locale = Locale.getDefault();
        return format(distanceInMeters, realTime, locale);
    }

    /**
     * Format distance for display using specified distance units.
     *
     * @param distanceInMeters the actual distance in meters.
     * @param realTime boolean flag for navigation vs. list view.
     * @param units miles or kilometers.
     * @return distance string formatted according to the rules of the formatter.
     */
    public static String format(int distanceInMeters, boolean realTime, DistanceUnits units) {
        return format(distanceInMeters, realTime, Locale.getDefault(), units);
    }

    /**
     * Format distance for display using specified locale.
     *
     * @param distanceInMeters the actual distance in meters.
     * @param realTime boolean flag for navigation vs. list view.
     * @param locale Locale that defines the number format for displaying distance.
     * @return distance string formatted according to the rules of the formatter.
     */
    public static String format(int distanceInMeters, boolean realTime, Locale locale) {
        if (useMiles(locale)) {
            return format(distanceInMeters, realTime, locale, DistanceUnits.MILES);
        } else {
            return format(distanceInMeters, realTime, locale, DistanceUnits.KILOMETERS);
        }
    }

    /**
     * Format distance for display using specified locale and units.
     *
     * @param distanceInMeters the actual distance in meters.
     * @param realTime boolean flag for navigation vs. list view.
     * @param locale Locale that defines the number format for displaying distance.
     * @param units miles or kilometers.
     * @return distance string formatted according to the rules of the formatter.
     */
    public static String format(int distanceInMeters, boolean realTime, Locale locale,
            DistanceUnits units) {

        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern("#.#");

        if (distanceInMeters == 0) {
            return "";
        }

        switch (units) {
            case MILES:
                return formatMiles(distanceInMeters, realTime);
            case KILOMETERS:
                return formatKilometers(distanceInMeters, realTime);
            default:
                return "";
        }
    }

    private static String formatMiles(int distanceInMeters, boolean realTime) {
        double distanceInFeet = distanceInMeters / METERS_IN_ONE_FOOT;
        if (distanceInFeet < 10) {
            return formatDistanceLessThanTenFeet(distanceInFeet, realTime);
        } else if (distanceInFeet < FEET_IN_ONE_MILE / 10) {
            return formatDistanceOverTenFeet(distanceInFeet);
        } else {
            return formatDistanceInMiles(distanceInMeters);
        }
    }

    private static String formatKilometers(int distanceInMeters, boolean realTime) {
        if (distanceInMeters >= 100) {
            return formatDistanceInKilometers(distanceInMeters);
        } else if (distanceInMeters > 10) {
            return formatDistanceOverTenMeters(distanceInMeters);
        } else {
            return formatShortMeters(distanceInMeters, realTime);
        }
    }

    private static boolean useMiles(Locale locale) {
        return locale.equals(Locale.US) || locale.equals(Locale.UK);
    }

    private static String formatDistanceOverTenMeters(int distanceInMeters) {
        return String.format(Locale.getDefault(), "%s m", distanceInMeters);
    }

    private static String formatShortMeters(int distanceInMeters, boolean realTime) {
        if (realTime) {
            return "now";
        } else {
            return formatDistanceOverTenMeters(distanceInMeters);
        }
    }

    private static String formatDistanceInKilometers(int distanceInMeters) {
        String value = decimalFormat.format((float) distanceInMeters / 1000);
        return String.format(Locale.getDefault(), "%s km", value);
    }

    private static String formatDistanceLessThanTenFeet(double distanceInFeet, boolean realTime) {
        if (realTime) {
            return "now";
        } else {
            return String.format(Locale.getDefault(), "%d ft", (int) Math.floor(distanceInFeet));
        }
    }

    private static String formatDistanceOverTenFeet(double distanceInFeet) {
        int roundedDistanceInFeet = roundDownToNearestTen(distanceInFeet);
        return String.format(Locale.getDefault(), "%d ft", roundedDistanceInFeet);
    }

    private static String formatDistanceInMiles(int distanceInMeters) {
        return String.format(Locale.getDefault(), "%s mi",
                decimalFormat.format(distanceInMeters / METERS_IN_ONE_MILE));
    }

    private static int roundDownToNearestTen(double distance) {
        return (int) Math.floor(distance / 10) * 10;
    }
}
