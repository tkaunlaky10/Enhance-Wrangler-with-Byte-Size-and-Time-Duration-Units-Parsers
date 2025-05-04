/*
 * Copyright © 2023 Cask Data, Inc.
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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Tests for the {@link TimeDuration} class.
 */
public class TimeDurationTest {

  /**
   * Test parsing of basic time durations
   */
  @Test
  public void testBasicParsing() {
    TimeDuration millis = new TimeDuration("100ms");
    Assert.assertEquals(100L, millis.getMilliseconds());
    Assert.assertEquals("100ms", millis.value());
    Assert.assertEquals(TokenType.TIME_DURATION, millis.type());
  }

  /**
   * Test parsing of milliseconds
   */
  @Test
  public void testParseMilliseconds() {
    TimeDuration millis = new TimeDuration("500ms");
    Assert.assertEquals(500L, millis.getMilliseconds());
    Assert.assertEquals(0.5, millis.getSeconds(), 0.001);
  }

  /**
   * Test parsing of seconds
   */
  @Test
  public void testParseSeconds() {
    TimeDuration seconds = new TimeDuration("5s");
    Assert.assertEquals(5000L, seconds.getMilliseconds());
    Assert.assertEquals(5.0, seconds.getSeconds(), 0.001);
  }

  /**
   * Test parsing of minutes
   */
  @Test
  public void testParseMinutes() {
    TimeDuration minutes = new TimeDuration("2m");
    Assert.assertEquals(2 * 60 * 1000L, minutes.getMilliseconds());
    Assert.assertEquals(2.0, minutes.getMinutes(), 0.001);
  }

  /**
   * Test parsing of hours
   */
  @Test
  public void testParseHours() {
    TimeDuration hours = new TimeDuration("1h");
    Assert.assertEquals(60 * 60 * 1000L, hours.getMilliseconds());
    Assert.assertEquals(1.0, hours.getHours(), 0.001);
  }

  /**
   * Test parsing of days
   */
  @Test
  public void testParseDays() {
    TimeDuration days = new TimeDuration("1d");
    Assert.assertEquals(24 * 60 * 60 * 1000L, days.getMilliseconds());
    Assert.assertEquals(1.0, days.getDays(), 0.001);
  }

  /**
   * Test parsing of weeks
   */
  @Test
  public void testParseWeeks() {
    TimeDuration weeks = new TimeDuration("1w");
    Assert.assertEquals(7 * 24 * 60 * 60 * 1000L, weeks.getMilliseconds());
    Assert.assertEquals(1.0, weeks.getWeeks(), 0.001);
  }

  /**
   * Test full unit names
   */
  @Test
  public void testFullUnitNames() {
    TimeDuration seconds = new TimeDuration("5second");
    Assert.assertEquals(5000L, seconds.getMilliseconds());
    
    TimeDuration milliseconds = new TimeDuration("500milliseconds");
    Assert.assertEquals(500L, milliseconds.getMilliseconds());
    
    TimeDuration minutes = new TimeDuration("2minute");
    Assert.assertEquals(2 * 60 * 1000L, minutes.getMilliseconds());
    
    TimeDuration hours = new TimeDuration("1hour");
    Assert.assertEquals(60 * 60 * 1000L, hours.getMilliseconds());
  }

  /**
   * Test decimal values
   */
  @Test
  public void testDecimalValues() {
    // Our fixed implementation now handles decimal values correctly
    TimeDuration decimalSeconds = new TimeDuration("1.5s");
    // Expected: (long)(1.5 * 1000) = 1500ms
    Assert.assertEquals(1500L, decimalSeconds.getMilliseconds());
    Assert.assertEquals(1.5, decimalSeconds.getSeconds(), 0.001);
    
    // Test decimal minutes
    TimeDuration decimalMinutes = new TimeDuration("0.5m");
    // Expected: (long)(0.5 * 60 * 1000) = 30000ms
    Assert.assertEquals(30000L, decimalMinutes.getMilliseconds());
    Assert.assertEquals(30.0, decimalMinutes.getSeconds(), 0.001);
    
    // Test decimal hours
    TimeDuration decimalHours = new TimeDuration("1.25h");
    // Expected: (long)(1.25 * 60 * 60 * 1000) = 4500000ms
    Assert.assertEquals(4500000L, decimalHours.getMilliseconds());
    Assert.assertEquals(75.0, decimalHours.getMinutes(), 0.001);
  }

  /**
   * Test conversions between different units
   */
  @Test
  public void testConversions() {
    TimeDuration minute = new TimeDuration("1m");
    Assert.assertEquals(60000L, minute.getMilliseconds());
    Assert.assertEquals(60.0, minute.getSeconds(), 0.001);
    Assert.assertEquals(1.0, minute.getMinutes(), 0.001);
    Assert.assertEquals(1.0 / 60.0, minute.getHours(), 0.001);
  }

  /**
   * Test TimeUnit conversion
   */
  @Test
  public void testTimeUnitConversion() {
    TimeDuration seconds = new TimeDuration("30s");
    Assert.assertEquals(30, seconds.toTimeUnit(TimeUnit.SECONDS));
    Assert.assertEquals(30000, seconds.toTimeUnit(TimeUnit.MILLISECONDS));
    Assert.assertEquals(30 * 1000 * 1000, seconds.toTimeUnit(TimeUnit.MICROSECONDS));
  }

  /**
   * Test equality and hashcode
   */
  @Test
  public void testEquality() {
    TimeDuration time1 = new TimeDuration("60s");
    TimeDuration time2 = new TimeDuration("1m");
    TimeDuration time3 = new TimeDuration("60s");

    Assert.assertEquals(time1, time3);
    Assert.assertEquals(time1.hashCode(), time3.hashCode());
    
    // 60s and 1m should be equal as they represent the same time amount
    Assert.assertEquals(time1, time2);
  }

  /**
   * Test add method
   */
  @Test
  public void testAddition() {
    TimeDuration time1 = new TimeDuration("30s");
    TimeDuration time2 = new TimeDuration("45s");
    TimeDuration sum = time1.add(time2);
    
    Assert.assertEquals(75000L, sum.getMilliseconds());
    Assert.assertEquals(75.0, sum.getSeconds(), 0.001);
  }

  /**
   * Test multiply method
   */
  @Test
  public void testMultiplication() {
    TimeDuration time = new TimeDuration("30s");
    TimeDuration doubled = time.multiply(2.0);
    
    Assert.assertEquals(60000L, doubled.getMilliseconds());
    Assert.assertEquals(60.0, doubled.getSeconds(), 0.001);
  }

  /**
   * Test invalid formats
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("invalid");
  }

  /**
   * Test invalid unit
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new TimeDuration("10x");
  }

  /**
   * Test negative value
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new TimeDuration("-10s");
  }

  /**
   * Test JSON serialization
   */
  @Test
  public void testJsonSerialization() {
    TimeDuration time = new TimeDuration("10s");
    Assert.assertNotNull(time.toJson());
    Assert.assertTrue(time.toJson().toString().contains("TIME_DURATION"));
    Assert.assertTrue(time.toJson().toString().contains("10s"));
  }
} 