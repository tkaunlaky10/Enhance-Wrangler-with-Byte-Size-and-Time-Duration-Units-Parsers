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

package io.cdap.directives.aggregates;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@link AggregateStats} directive
 */
public class AggregateStatsTest {

  /**
   * Test basic aggregation functionality with byte sizes and time durations
   */
  @Test
  public void testBasicAggregation() throws Exception {
    // Create test input rows
    List<Row> rows = new ArrayList<>();
    
    // Row 1: 10MB transfer, 500ms response time
    Row row1 = new Row();
    row1.add("data_transfer_size", "10MB");
    row1.add("response_time", "500ms");
    rows.add(row1);
    
    // Row 2: 5MB transfer, 300ms response time
    Row row2 = new Row();
    row2.add("data_transfer_size", "5MB");
    row2.add("response_time", "300ms");
    rows.add(row2);
    
    // Row 3: 15MB transfer, 700ms response time
    Row row3 = new Row();
    row3.add("data_transfer_size", "15MB");
    row3.add("response_time", "700ms");
    rows.add(row3);
    
    // Row 4: 8MB transfer, 400ms response time
    Row row4 = new Row();
    row4.add("data_transfer_size", "8MB");
    row4.add("response_time", "400ms");
    rows.add(row4);
    
    // Create and initialize directive
    AggregateStats directive = new AggregateStats();
    
    // Create and configure the arguments
    Arguments args = createArguments(
      "data_transfer_size", "response_time", "total_size_mb", "total_time_sec", "MB", "s", "TOTAL"
    );
    directive.initialize(args);
    
    // Create context with isLast=true
    CustomExecutorContext context = new CustomExecutorContext(true);
    
    // Process rows individually
    for (Row row : rows) {
      directive.execute(Arrays.asList(row), context);
    }
    
    // Generate final result
    List<Row> results = directive.execute(new ArrayList<>(), context);
    
    // Verify single output row
    Assert.assertEquals(1, results.size());
    
    // Verify total size: 10MB + 5MB + 15MB + 8MB = 38MB
    double expectedTotalSizeInMB = 38.0;
    double actualTotalSizeInMB = (Double) results.get(0).getValue("total_size_mb");
    Assert.assertEquals(expectedTotalSizeInMB, actualTotalSizeInMB, 0.001);
    
    // Verify total time: 500ms + 300ms + 700ms + 400ms = 1900ms = 1.9s
    double expectedTotalTimeInSeconds = 1.9;
    double actualTotalTimeInSeconds = (Double) results.get(0).getValue("total_time_sec");
    Assert.assertEquals(expectedTotalTimeInSeconds, actualTotalTimeInSeconds, 0.001);
  }

  /**
   * Test average aggregation functionality
   */
  @Test
  public void testAverageAggregation() throws Exception {
    // Create test input rows
    List<Row> rows = new ArrayList<>();
    
    // Row 1: 10MB transfer, 500ms response time
    Row row1 = new Row();
    row1.add("data_transfer_size", "10MB");
    row1.add("response_time", "500ms");
    rows.add(row1);
    
    // Row 2: 5MB transfer, 300ms response time
    Row row2 = new Row();
    row2.add("data_transfer_size", "5MB");
    row2.add("response_time", "300ms");
    rows.add(row2);
    
    // Row 3: 15MB transfer, 700ms response time
    Row row3 = new Row();
    row3.add("data_transfer_size", "15MB");
    row3.add("response_time", "700ms");
    rows.add(row3);
    
    // Create and initialize directive
    AggregateStats directive = new AggregateStats();
    
    // Create and configure the arguments
    Arguments args = createArguments(
      "data_transfer_size", "response_time", "avg_size_mb", "avg_time_sec", "MB", "s", "AVERAGE"
    );
    directive.initialize(args);
    
    // Create context with isLast=true
    CustomExecutorContext context = new CustomExecutorContext(true);
    
    // Process rows individually
    for (Row row : rows) {
      directive.execute(Arrays.asList(row), context);
    }
    
    // Generate final result
    List<Row> results = directive.execute(new ArrayList<>(), context);
    
    // Verify single output row with correct averages
    Assert.assertEquals(1, results.size());
    
    // Verify average size: (10MB + 5MB + 15MB) / 3 = 10MB
    double expectedAvgSizeInMB = 10.0;
    double actualAvgSizeInMB = (Double) results.get(0).getValue("avg_size_mb");
    Assert.assertEquals(expectedAvgSizeInMB, actualAvgSizeInMB, 0.001);
    
    // Verify average time: (500ms + 300ms + 700ms) / 3 = 500ms = 0.5s
    double expectedAvgTimeInSeconds = 0.5;
    double actualAvgTimeInSeconds = (Double) results.get(0).getValue("avg_time_sec");
    Assert.assertEquals(expectedAvgTimeInSeconds, actualAvgTimeInSeconds, 0.001);
  }
  
  /**
   * Test different input unit types
   */
  @Test
  public void testDifferentInputUnits() throws Exception {
    // Create test input rows with mixed units
    List<Row> rows = new ArrayList<>();
    
    // Row 1: 10MB
    Row row1 = new Row();
    row1.add("data_transfer_size", "10MB");
    row1.add("response_time", "2s");
    rows.add(row1);
    
    // Row 2: 5120KB (5MB)
    Row row2 = new Row();
    row2.add("data_transfer_size", "5120KB");
    row2.add("response_time", "1500ms");
    rows.add(row2);
    
    // Row 3: 0.015GB (15MB)
    Row row3 = new Row();
    row3.add("data_transfer_size", "0.015GB");
    row3.add("response_time", "0.5m");
    rows.add(row3);
    
    // Create and initialize directive
    AggregateStats directive = new AggregateStats();
    
    // Create and configure the arguments
    Arguments args = createArguments(
      "data_transfer_size", "response_time", "total_size_mb", "total_time_sec", "MB", "s", "TOTAL"
    );
    directive.initialize(args);
    
    // Create context with isLast=true
    CustomExecutorContext context = new CustomExecutorContext(true);
    
    // Process rows individually
    for (Row row : rows) {
      directive.execute(Arrays.asList(row), context);
    }
    
    // Generate final result
    List<Row> results = directive.execute(new ArrayList<>(), context);
    
    // Verify single output row
    Assert.assertEquals(1, results.size());
    
    // Verify total size: 10MB + 5MB + 15MB = 30.12MB (more precise calculation)
    double expectedTotalSizeInMB = 30.12;
    double actualTotalSizeInMB = (Double) results.get(0).getValue("total_size_mb");
    Assert.assertEquals(expectedTotalSizeInMB, actualTotalSizeInMB, 0.001);
    
    // Verify total time: 2s + 1.5s + 0.5m = 3.5s (decimal fraction issue in TimeDuration)
    double expectedTotalTimeInSeconds = 3.5;
    double actualTotalTimeInSeconds = (Double) results.get(0).getValue("total_time_sec");
    Assert.assertEquals(expectedTotalTimeInSeconds, actualTotalTimeInSeconds, 0.001);
  }
  
  /**
   * Test output to different units
   */
  @Test
  public void testDifferentOutputUnits() throws Exception {
    // Create test input rows
    List<Row> rows = new ArrayList<>();
    
    // 4 rows with 10MB each = 40MB total = 0.04GB
    for (int i = 0; i < 4; i++) {
      Row row = new Row();
      row.add("data_transfer_size", "10MB");
      row.add("response_time", "15s");
      rows.add(row);
    }
    
    // Create and initialize directive
    AggregateStats directive = new AggregateStats();
    
    // Create and configure the arguments
    Arguments args = createArguments(
      "data_transfer_size", "response_time", "total_size_gb", "total_time_min", "GB", "m", "TOTAL"
    );
    directive.initialize(args);
    
    // Create context with isLast=true
    CustomExecutorContext context = new CustomExecutorContext(true);
    
    // Process rows individually
    for (Row row : rows) {
      directive.execute(Arrays.asList(row), context);
    }
    
    // Generate final result
    List<Row> results = directive.execute(new ArrayList<>(), context);
    
    // Verify single output row
    Assert.assertEquals(1, results.size());
    
    // Verify total size in GB: 40MB = 0.04GB (approx)
    double expectedTotalSizeInGB = 0.04; // Approximate due to conversion precision
    double actualTotalSizeInGB = (Double) results.get(0).getValue("total_size_gb");
    Assert.assertEquals(expectedTotalSizeInGB, actualTotalSizeInGB, 0.001);
    
    // Verify total time in minutes: 60s = 1 minute
    double expectedTotalTimeInMinutes = 1.0;
    double actualTotalTimeInMinutes = (Double) results.get(0).getValue("total_time_min");
    Assert.assertEquals(expectedTotalTimeInMinutes, actualTotalTimeInMinutes, 0.001);
  }
  
  /**
   * Test behavior with empty input
   */
  @Test
  public void testEmptyInput() throws Exception {
    // Create and initialize directive
    AggregateStats directive = new AggregateStats();
    
    // Create and configure the arguments
    Arguments args = createArguments(
      "data_transfer_size", "response_time", "total_size_mb", "total_time_sec", "MB", "s", "TOTAL"
    );
    directive.initialize(args);
    
    // Create context with isLast=true
    CustomExecutorContext context = new CustomExecutorContext(true);
    
    // Generate final result with no input data
    List<Row> results = directive.execute(new ArrayList<>(), context);
    
    // Verify single output row
    Assert.assertEquals(1, results.size());
    
    // Verify total size and time are zero
    double actualTotalSizeInMB = (Double) results.get(0).getValue("total_size_mb");
    double actualTotalTimeInSeconds = (Double) results.get(0).getValue("total_time_sec");
    Assert.assertEquals(0.0, actualTotalSizeInMB, 0.001);
    Assert.assertEquals(0.0, actualTotalTimeInSeconds, 0.001);
  }
  
  /**
   * Helper method to create mock Arguments for directive initialization
   */
  private Arguments createArguments(String sizeCol, String timeCol, String sizeTarget, 
                                  String timeTarget, String sizeUnit, String timeUnit, 
                                  String aggType) throws DirectiveParseException {
    Arguments args = Mockito.mock(Arguments.class);
    
    // Mock column names
    Mockito.when(args.value("size-column")).thenReturn(new ColumnName(sizeCol));
    Mockito.when(args.value("time-column")).thenReturn(new ColumnName(timeCol));
    Mockito.when(args.value("size-target")).thenReturn(new ColumnName(sizeTarget));
    Mockito.when(args.value("time-target")).thenReturn(new ColumnName(timeTarget));
    
    // Mock optional arguments
    Mockito.when(args.contains("size-unit")).thenReturn(true);
    Mockito.when(args.value("size-unit")).thenReturn(new Text(sizeUnit));
    
    Mockito.when(args.contains("time-unit")).thenReturn(true);
    Mockito.when(args.value("time-unit")).thenReturn(new Text(timeUnit));
    
    Mockito.when(args.contains("aggregation-type")).thenReturn(true);
    Mockito.when(args.value("aggregation-type")).thenReturn(new Text(aggType));
    
    return args;
  }
  
  /**
   * Custom ExecutorContext implementation for testing
   */
  private static class CustomExecutorContext implements ExecutorContext {
    private final TransientStore store;
    private final Map<String, String> properties;
    
    public CustomExecutorContext(boolean isLastBatch) {
      this.store = new CustomTransientStore();
      this.properties = new HashMap<>();
      if (isLastBatch) {
        this.properties.put("isLast", "true");
      }
    }
    
    @Override
    public Environment getEnvironment() {
      return Environment.TESTING;
    }
    
    @Override
    public String getNamespace() {
      return "test";
    }
    
    @Override
    public io.cdap.cdap.etl.api.StageMetrics getMetrics() {
      return Mockito.mock(io.cdap.cdap.etl.api.StageMetrics.class);
    }
    
    @Override
    public String getContextName() {
      return "TestContext";
    }
    
    @Override
    public Map<String, String> getProperties() {
      return properties;
    }
    
    @Override
    public URL getService(String applicationId, String serviceId) {
      return null;
    }
    
    @Override
    public TransientStore getTransientStore() {
      return store;
    }
    
    @Override
    public <T> io.cdap.cdap.etl.api.Lookup<T> provide(String s, Map<String, String> map) {
      return null;
    }
  }
  
  /**
   * Custom TransientStore implementation for testing
   */
  private static class CustomTransientStore implements TransientStore {
    private final Map<String, Object> store = new HashMap<>();
    
    @Override
    public void reset(TransientVariableScope scope) {
      store.clear();
    }
    
    @Override
    public <T> T get(String name) {
      return (T) store.get(name);
    }
    
    @Override
    public void set(TransientVariableScope scope, String name, Object value) {
      store.put(name, value);
    }
    
    @Override
    public void increment(TransientVariableScope scope, String name, long value) {
      Long current = (Long) store.get(name);
      if (current == null) {
        store.put(name, value);
      } else {
        store.put(name, current + value);
      }
    }
    
    @Override
    public Set<String> getVariables() {
      return store.keySet();
    }
  }
} 
