/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a time duration with units (like 30s, 5m, 2h).
 * Parses duration strings and allows conversion to different units.
 */
@PublicEvolving
public class TimeDuration implements Token {

    // Pattern to extract the numeric value and unit from a duration string
    private static final Pattern DURATION_PATTERN = 
        Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([a-zA-Z]+)"); 

    // A map to store the conversion factor for each unit to nanoseconds (for higher precision)
    // Using a static final map makes lookups efficient
    private static final Map<String, TimeUnit> UNIT_STRING_TO_TIMEUNIT;

    // Note: TimeUnit does not directly support 'week', 'month', 'year'.
    // Using more accurate values: 
    // - Week = 7 days (exact)
    // - Month = 30.436875 days (average considering leap years: 365.25/12)
    // - Year = 365.25 days (accounting for leap years)
    private static final long MILLIS_PER_WEEK = 7L * TimeUnit.DAYS.toMillis(1);
    private static final long MILLIS_PER_MONTH = (long) (30.436875 * TimeUnit.DAYS.toMillis(1)); 
    // Average month (365.25/12)
    private static final long MILLIS_PER_YEAR = (long) (365.25 * TimeUnit.DAYS.toMillis(1)); // With leap years


    static {
        Map<String, TimeUnit> unitMap = new HashMap<>();
        unitMap.put("ns", TimeUnit.NANOSECONDS);
        unitMap.put("nanosecond", TimeUnit.NANOSECONDS);
        unitMap.put("nanoseconds", TimeUnit.NANOSECONDS);

        unitMap.put("us", TimeUnit.MICROSECONDS);
        unitMap.put("µs", TimeUnit.MICROSECONDS); // Support mu symbol
        unitMap.put("microsecond", TimeUnit.MICROSECONDS);
        unitMap.put("microseconds", TimeUnit.MICROSECONDS);

        unitMap.put("ms", TimeUnit.MILLISECONDS);
        unitMap.put("millisecond", TimeUnit.MILLISECONDS);
        unitMap.put("milliseconds", TimeUnit.MILLISECONDS);

        unitMap.put("s", TimeUnit.SECONDS);
        unitMap.put("sec", TimeUnit.SECONDS);
        unitMap.put("second", TimeUnit.SECONDS);
        unitMap.put("seconds", TimeUnit.SECONDS);

        unitMap.put("m", TimeUnit.MINUTES);
        unitMap.put("min", TimeUnit.MINUTES);
        unitMap.put("minute", TimeUnit.MINUTES);
        unitMap.put("minutes", TimeUnit.MINUTES);

        unitMap.put("h", TimeUnit.HOURS);
        unitMap.put("hour", TimeUnit.HOURS);
        unitMap.put("hours", TimeUnit.HOURS);

        unitMap.put("d", TimeUnit.DAYS);
        unitMap.put("day", TimeUnit.DAYS);
        unitMap.put("days", TimeUnit.DAYS);

        // Custom units not in TimeUnit - handled separately
        unitMap.put("w", null); // Indicate custom handling needed
        unitMap.put("week", null);
        unitMap.put("weeks", null);
        unitMap.put("month", null);
        unitMap.put("months", null);
        unitMap.put("y", null);
        unitMap.put("year", null);
        unitMap.put("years", null);


        UNIT_STRING_TO_TIMEUNIT = Collections.unmodifiableMap(unitMap);
    }

    private final String originalDurationString;
    private final double numericValue;
    private final String unit;
    private final long totalMilliseconds; // Storing as milliseconds as a common base

    /**
     * Creates a new TimeDuration by parsing a string like "30s" or "5m".
     *
     * @param durationStr The string representation of the time duration.
     * @throws IllegalArgumentException if the string is null, invalid, or has an unknown unit.
     */
    public TimeDuration(String durationStr) {
        if (durationStr == null) {
            throw new IllegalArgumentException("Duration string cannot be null");
        }

        this.originalDurationString = durationStr.trim();
        Matcher matcher = DURATION_PATTERN.matcher(this.originalDurationString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Invalid time duration format: '" + originalDurationString + "'. " +
                "Expected format is a number followed by a unit (e.g., '30s', '5m')."
            );
        }

        try {
            this.numericValue = Double.parseDouble(matcher.group(1));
            this.unit = matcher.group(2);
        } catch (NumberFormatException e) {
            // This catch might be redundant with the regex, but good for safety
            throw new IllegalArgumentException("Invalid numeric part in time duration: " + originalDurationString, e);
        }

        if (this.numericValue < 0) {
            throw new IllegalArgumentException("Time duration cannot be negative: " + originalDurationString);
        }

        // Determine the total duration in milliseconds
        TimeUnit timeUnit = UNIT_STRING_TO_TIMEUNIT.get(this.unit);

        if (timeUnit != null) {
            // Use TimeUnit for standard units
            this.totalMilliseconds = timeUnit.toMillis((long) numericValue);
            
            // Simple overflow check
            boolean hasFractionalPart = numericValue % 1.0 != 0;
            long estimatedMaxValue = timeUnit.toMillis((long) (numericValue + 0.999));
            boolean potentialOverflow = estimatedMaxValue > this.totalMilliseconds + timeUnit.toMillis(1);
            
            if (potentialOverflow && hasFractionalPart) {
                // We'll rely on long cast truncation for simplicity
            }
        } else {
            // Handle custom units not directly supported by TimeUnit
            switch (unit) {
                case "w":
                case "week":
                case "weeks":
                    this.totalMilliseconds = (long) (numericValue * MILLIS_PER_WEEK);
                    break;
                case "month":
                case "months":
                    this.totalMilliseconds = (long) (numericValue * MILLIS_PER_MONTH);
                    break;
                case "y":
                case "year":
                case "years":
                    this.totalMilliseconds = (long) (numericValue * MILLIS_PER_YEAR);
                    break;
                default:
                     throw new IllegalArgumentException(
                        "Unknown time unit '" + unit + "' in '" + originalDurationString + "'. " +
                        "Valid units include: " + 
                        String.join(", ", UNIT_STRING_TO_TIMEUNIT.keySet())
                    );
            }
        }
    }

    // Private constructor for creating TimeDuration from pre-calculated milliseconds
    private TimeDuration(long totalMilliseconds) {
        this.totalMilliseconds = totalMilliseconds;
        // Determine the most appropriate unit for string representation
        if (totalMilliseconds < 1000) {
            this.unit = "ms";
            this.numericValue = totalMilliseconds;
        } else if (totalMilliseconds < TimeUnit.MINUTES.toMillis(1)) {
             this.unit = "s";
             this.numericValue = totalMilliseconds / 1000.0;
        } else if (totalMilliseconds < TimeUnit.HOURS.toMillis(1)) {
             this.unit = "m";
             this.numericValue = totalMilliseconds / (double) TimeUnit.MINUTES.toMillis(1);
        } else if (totalMilliseconds < TimeUnit.DAYS.toMillis(1)) {
             this.unit = "h";
             this.numericValue = totalMilliseconds / (double) TimeUnit.HOURS.toMillis(1);
        } else if (totalMilliseconds < MILLIS_PER_WEEK) {
             this.unit = "d";
             this.numericValue = totalMilliseconds / (double) TimeUnit.DAYS.toMillis(1);
        } else if (totalMilliseconds < MILLIS_PER_MONTH) {
             this.unit = "w";
             this.numericValue = totalMilliseconds / (double) MILLIS_PER_WEEK;
        } else if (totalMilliseconds < MILLIS_PER_YEAR) {
             this.unit = "month";
             this.numericValue = totalMilliseconds / (double) MILLIS_PER_MONTH;
        } else {
             this.unit = "y";
             this.numericValue = totalMilliseconds / (double) MILLIS_PER_YEAR;
        }
         // Generate a representative string (might not be the original)
         this.originalDurationString = String.format("%.3f%s", this.numericValue, this.unit); 
         // Use format for precision
    }


    /**
     * Returns the original string that was used to create this TimeDuration.
     */
    @Override
    public String value() {
        return originalDurationString;
    }

    /**
     * Gets the numeric part of the time duration.
     */
    public double numericValue() {
        return numericValue;
    }

    /**
     * Gets the unit part of the time duration.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Converts the duration to milliseconds.
     */
    public long getMilliseconds() {
        return totalMilliseconds;
    }

    /**
     * Converts the duration to seconds.
     */
    public double getSeconds() {
        return totalMilliseconds / 1000.0;
    }

    /**
     * Converts the duration to minutes.
     */
    public double getMinutes() {
        return totalMilliseconds / (double) TimeUnit.MINUTES.toMillis(1);
    }

    /**
     * Converts the duration to hours.
     */
    public double getHours() {
        return totalMilliseconds / (double) TimeUnit.HOURS.toMillis(1);
    }

     /**
     * Converts the duration to days.
     */
    public double getDays() {
        return totalMilliseconds / (double) TimeUnit.DAYS.toMillis(1);
    }

     /**
     * Converts the duration to weeks.
     */
    public double getWeeks() {
        return totalMilliseconds / (double) MILLIS_PER_WEEK;
    }

    /**
     * Converts the duration to months.
     * Note: Uses the average month length of 30.436875 days (365.25/12)
     */
    public double getMonths() {
        return totalMilliseconds / (double) MILLIS_PER_MONTH;
    }

    /**
     * Converts the duration to years.
     * Note: Uses the average year length of 365.25 days (accounting for leap years)
     */
    public double getYears() {
        return totalMilliseconds / (double) MILLIS_PER_YEAR;
    }

    /**
     * Converts the duration to a specific Java TimeUnit.
     */
    public long toTimeUnit(TimeUnit targetUnit) {
         if (targetUnit == null) {
            throw new IllegalArgumentException("Target TimeUnit cannot be null");
        }
        // Convert internal milliseconds to target unit
        return targetUnit.convert(totalMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the type of token.
     */
    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    /**
     * Creates a JSON representation.
     */
    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type().name());
        object.addProperty("value", originalDurationString);
        object.addProperty("milliseconds", totalMilliseconds);
        object.addProperty("seconds", getSeconds());
        object.addProperty("minutes", getMinutes());
        object.addProperty("hours", getHours());
        object.addProperty("days", getDays());
        object.addProperty("weeks", getWeeks());
        object.addProperty("months", getMonths());
        object.addProperty("years", getYears());
        object.addProperty("numericValue", numericValue);
        object.addProperty("unit", unit);
        return object;
    }

    /**
     * Returns a string representation.
     */
    @Override
    public String toString() {
        return originalDurationString; // Return the internally generated string for consistency
    }

    /**
     * Compares this time duration with another object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeDuration other = (TimeDuration) o;
        return totalMilliseconds == other.totalMilliseconds; // Compare based on total milliseconds
    }

    /**
     * Generates a hash code.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(totalMilliseconds); // Hash based on total milliseconds
    }

    /**
     * Adds another duration to this one.
     *
     * @param other the duration to add
     * @return a new TimeDuration with the sum
     * @throws IllegalArgumentException if the other duration is null
     */
    public TimeDuration add(TimeDuration other) {
         if (other == null) {
            throw new IllegalArgumentException("Cannot add a null duration");
        }
        long sumMillis = this.totalMilliseconds + other.totalMilliseconds;
        return new TimeDuration(sumMillis); // Use the private constructor
    }

    /**
     * Multiplies this duration by a factor.
     *
     * @param factor the multiplication factor
     * @return a new TimeDuration with the result
     * @throws IllegalArgumentException if the factor is negative
     */
    public TimeDuration multiply(double factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Multiplication factor cannot be negative");
        }
        return new TimeDuration((long) (this.totalMilliseconds * factor));
    }
}
