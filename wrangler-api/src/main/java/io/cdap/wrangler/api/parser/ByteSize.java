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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a data storage size with units (like 10MB, 1.5GB).
 * * Parses size strings and allows conversion to different units.
 */
@PublicEvolving
public class ByteSize implements Token {

    // Pattern to extract the numeric value and unit from a size string
    private static final Pattern SIZE_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([a-zA-Z]+)");

    // A map to store the conversion factor for each unit to bytes
    // Using a static final map makes lookups efficient
    private static final Map<String, Double> UNIT_TO_BYTES;

    static {
        Map<String, Double> unitMap = new HashMap<>();
        unitMap.put("B", 1.0);

        // Decimal units (powers of 1000)
        unitMap.put("KB", 1000.0);
        unitMap.put("MB", 1000.0 * 1000);
        unitMap.put("GB", 1000.0 * 1000 * 1000);
        unitMap.put("TB", 1000.0 * 1000 * 1000 * 1000);
        unitMap.put("PB", 1000.0 * 1000 * 1000 * 1000 * 1000);
        unitMap.put("EB", 1000.0 * 1000 * 1000 * 1000 * 1000 * 1000);
        unitMap.put("ZB", 1000.0 * 1000 * 1000 * 1000 * 1000 * 1000 * 1000);
        unitMap.put("YB", 1000.0 * 1000 * 1000 * 1000 * 1000 * 1000 * 1000 * 1000);

        // Binary units (powers of 1024)
        unitMap.put("KiB", 1024.0);
        unitMap.put("MiB", 1024.0 * 1024);
        unitMap.put("GiB", 1024.0 * 1024 * 1024);
        unitMap.put("TiB", 1024.0 * 1024 * 1024 * 1024);
        unitMap.put("PiB", 1024.0 * 1024 * 1024 * 1024 * 1024);
        unitMap.put("EiB", 1024.0 * 1024 * 1024 * 1024 * 1024 * 1024);
        unitMap.put("ZiB", 1024.0 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024);
        unitMap.put("YiB", 1024.0 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024);

        // Sub-byte units
        unitMap.put("bit", 1.0 / 8.0);
        unitMap.put("bits", 1.0 / 8.0);
        unitMap.put("nibble", 1.0 / 2.0);
        unitMap.put("nibbles", 1.0 / 2.0);

        UNIT_TO_BYTES = Collections.unmodifiableMap(unitMap);
    }

    private final String originalSizeString;
    private final double numericValue;
    private final String unit;
    private final long bytes; // Pre-calculated bytes for efficiency

    /**
     * Creates a new ByteSize by parsing a string like "10MB" or "1.5GB".
     *
     * @param sizeStr The string representation of the byte size.
     * @throws IllegalArgumentException if the string is null, invalid, or has an unknown unit.
     */
    public ByteSize(String sizeStr) {
        if (sizeStr == null) {
            throw new IllegalArgumentException("Size string cannot be null");
        }

        this.originalSizeString = sizeStr.trim();
        Matcher matcher = SIZE_PATTERN.matcher(this.originalSizeString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Invalid byte size format: '" + originalSizeString + "'. " +
                "Expected format is a number followed by a unit (e.g., '10MB', '1.5GB')."
            );
        }

        try {
            this.numericValue = Double.parseDouble(matcher.group(1));
            this.unit = matcher.group(2);
        } catch (NumberFormatException e) {
             // This catch might be redundant with the regex, but good for safety
            throw new IllegalArgumentException("Invalid numeric part in byte size: " + originalSizeString, e);
        }

        if (this.numericValue < 0) {
            throw new IllegalArgumentException("Byte size cannot be negative: " + originalSizeString);
        }

        // Look up the conversion factor from the map
        Double factor = UNIT_TO_BYTES.get(this.unit);

        if (factor == null) {
             throw new IllegalArgumentException(
                "Unknown byte unit '" + unit + "' in '" + originalSizeString + "'. " +
                "Valid units include: " + String.join(", ", UNIT_TO_BYTES.keySet())
            );
        }

        // Calculate and store the byte value immediately
        this.bytes = (long) (this.numericValue * factor);
    }

    /**
     * Returns the original string that was used to create this ByteSize.
     */
    @Override
    public String value() {
        return originalSizeString;
    }

    /**
     * Gets the numeric part of the byte size.
     */
    public double numericValue() {
        return numericValue;
    }

    /**
     * Gets the unit part of the byte size.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Converts the size to bytes.
     */
    public long getBytes() {
        return bytes; // Return the pre-calculated value
    }

    /**
     * Converts the size to kilobytes (KB).
     */
    public double getKilobytes() {
        return bytes / 1000.0;
    }

    /**
     * Converts the size to megabytes (MB).
     */
    public double getMegabytes() {
        return bytes / (1000.0 * 1000.0);
    }

    /**
     * Converts the size to gigabytes (GB).
     */
    public double getGigabytes() {
        return bytes / (1000.0 * 1000.0 * 1000.0);
    }

    /**
     * Returns the binary kilobytes (KiB).
     */
    public double getKibibytes() {
        return bytes / 1024.0;
    }

    /**
     * Returns the binary megabytes (MiB).
     */
    public double getMebibytes() {
        return bytes / (1024.0 * 1024.0);
    }

    /**
     * Returns the binary gigabytes (GiB).
     */
    public double getGibibytes() {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

     /**
     * Returns the type of token.
     */
    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    /**
     * Creates a JSON representation.
     */
    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type().name()); // Use type() method
        object.addProperty("value", originalSizeString);
        object.addProperty("bytes", bytes); // Use the pre-calculated bytes
        object.addProperty("numericValue", numericValue);
        object.addProperty("unit", unit);
        return object;
    }

    /**
     * Returns a string representation.
     */
    @Override
    public String toString() {
        return originalSizeString;
    }

    /**
     * Compares this byte size with another object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ByteSize other = (ByteSize) o;
        return bytes == other.bytes; // Compare based on pre-calculated bytes
    }

    /**
     * Generates a hash code.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(bytes); // Hash based on pre-calculated bytes
    }
}
