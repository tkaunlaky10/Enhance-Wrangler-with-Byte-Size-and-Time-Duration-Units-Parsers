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

/**
 * Tests for the {@link ByteSize} class.
 */
public class ByteSizeTest {

  /**
   * Test parsing of basic byte sizes
   */
  @Test
  public void testBasicParsing() {
    ByteSize bytes = new ByteSize("100B");
    Assert.assertEquals(100L, bytes.getBytes());
    Assert.assertEquals("100B", bytes.value());
    Assert.assertEquals(TokenType.BYTE_SIZE, bytes.type());
  }

  /**
   * Test parsing of kilobytes
   */
  @Test
  public void testParseKilobytes() {
    ByteSize kb = new ByteSize("10KB");
    Assert.assertEquals(10 * 1000L, kb.getBytes());
    Assert.assertEquals(10.0, kb.getKilobytes(), 0.001);
  }

  /**
   * Test parsing of megabytes
   */
  @Test
  public void testParseMegabytes() {
    ByteSize mb = new ByteSize("5MB");
    Assert.assertEquals(5 * 1000 * 1000L, mb.getBytes());
    Assert.assertEquals(5.0, mb.getMegabytes(), 0.001);
  }

  /**
   * Test parsing of gigabytes
   */
  @Test
  public void testParseGigabytes() {
    ByteSize gb = new ByteSize("2GB");
    Assert.assertEquals(2 * 1000 * 1000 * 1000L, gb.getBytes());
    Assert.assertEquals(2.0, gb.getGigabytes(), 0.001);
  }

  /**
   * Test binary units (KiB, MiB, GiB)
   */
  @Test
  public void testBinaryUnits() {
    ByteSize kib = new ByteSize("10KiB");
    Assert.assertEquals(10 * 1024L, kib.getBytes());
    Assert.assertEquals(10.0, kib.getKibibytes(), 0.001);

    ByteSize mib = new ByteSize("5MiB");
    Assert.assertEquals(5 * 1024 * 1024L, mib.getBytes());
    Assert.assertEquals(5.0, mib.getMebibytes(), 0.001);

    ByteSize gib = new ByteSize("2GiB");
    Assert.assertEquals(2 * 1024 * 1024 * 1024L, gib.getBytes());
    Assert.assertEquals(2.0, gib.getGibibytes(), 0.001);
  }

  /**
   * Test decimal values
   */
  @Test
  public void testDecimalValues() {
    ByteSize decimalMB = new ByteSize("1.5MB");
    Assert.assertEquals((long) (1.5 * 1000 * 1000), decimalMB.getBytes());
    Assert.assertEquals(1.5, decimalMB.getMegabytes(), 0.001);
  }

  /**
   * Test conversions between different units
   */
  @Test
  public void testConversions() {
    ByteSize mb = new ByteSize("1MB");
    Assert.assertEquals(1000.0, mb.getKilobytes(), 0.001);
    Assert.assertEquals(1.0, mb.getMegabytes(), 0.001);
    Assert.assertEquals(0.001, mb.getGigabytes(), 0.001);

    ByteSize kb = new ByteSize("1024KB");
    Assert.assertEquals(1024000L, kb.getBytes());
    Assert.assertEquals(1.024, kb.getMegabytes(), 0.001);
  }

  /**
   * Test equality and hashcode
   */
  @Test
  public void testEquality() {
    ByteSize size1 = new ByteSize("1000KB");
    ByteSize size2 = new ByteSize("1MB");
    ByteSize size3 = new ByteSize("1000KB");

    Assert.assertEquals(size1, size3);
    Assert.assertEquals(size1.hashCode(), size3.hashCode());
    
    // 1000KB and 1MB should be equal as they represent the same byte amount
    Assert.assertEquals(size1, size2);
  }

  /**
   * Test invalid formats
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("invalid");
  }

  /**
   * Test invalid unit
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("10XB");
  }

  /**
   * Test negative value
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new ByteSize("-10MB");
  }

  /**
   * Test JSON serialization
   */
  @Test
  public void testJsonSerialization() {
    ByteSize size = new ByteSize("10MB");
    Assert.assertNotNull(size.toJson());
    Assert.assertTrue(size.toJson().toString().contains("BYTE_SIZE"));
    Assert.assertTrue(size.toJson().toString().contains("10MB"));
  }
} 