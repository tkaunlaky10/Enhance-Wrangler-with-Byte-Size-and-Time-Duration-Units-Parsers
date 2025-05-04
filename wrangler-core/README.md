# Wrangler Core Module

## Overview

The `wrangler-core` module is the central implementation of the CDAP Wrangler data preparation tool. It contains the ANTLR grammar, parser implementations, directive implementations, and execution logic. This module is responsible for transforming directives written in the Wrangler DSL into executable actions on data.

## Key Components

### Grammar System

The Wrangler grammar is defined using ANTLR4 in the `Directives.g4` file:

```
┌─────────────┐     ┌───────────────┐     ┌────────────────┐
│ Directives.g4│────►│ANTLR Generator│────►│Generated Parser│
└─────────────┘     └───────────────┘     └────────────────┘
       │                                           │
       │                                           ▼
       │                                  ┌────────────────┐
       └──────────────────────────────►  │  RecipeVisitor │
                                         └────────────────┘
```

The grammar defines:
- Lexer rules for recognizing tokens in the input
- Parser rules for organizing these tokens into meaningful structures
- Rules for directive arguments, including specialized rules for complex types

### Grammar Enhancements for ByteSize and TimeDuration

The `Directives.g4` grammar file has been enhanced to support the new ByteSize and TimeDuration token types:

```antlr
// Lexer rules for recognizing byte sizes and time durations
BYTE_SIZE : NUMBER BYTE_UNIT ;
fragment BYTE_UNIT : 'B' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' ;

TIME_DURATION : NUMBER TIME_UNIT ;
fragment TIME_UNIT : 'ms' | 's' | 'm' | 'h' ;

// Parser rules for handling these tokens as directive arguments
byteSizeArg : BYTE_SIZE ;
timeDurationArg : TIME_DURATION ;

// Integration with the existing value rule
value : ... | byteSizeArg | timeDurationArg ;
```

These enhancements allow directives to accept byte size and time duration arguments in their natural format (e.g., "10MB", "500ms").

### RecipeVisitor Implementation

The `RecipeVisitor` class is responsible for converting the ANTLR parse tree into Token objects that directives can use:

```java
public class RecipeVisitor extends DirectivesBaseVisitor<Token> {
  // Existing visitor methods...
  
  @Override
  public Token visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
    String text = ctx.getText();
    return new ByteSize(text);
  }
  
  @Override
  public Token visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
    String text = ctx.getText();
    return new TimeDuration(text);
  }
  
  // Other visitor methods...
}
```

The visitor:
1. Extracts the text from the context
2. Creates the appropriate Token implementation (ByteSize or TimeDuration)
3. Returns the token to be added to the TokenGroup for directive execution

### AggregateStats Directive

The `AggregateStats` directive demonstrates the practical use of ByteSize and TimeDuration parsers:

```java
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Categories(categories = { "aggregator"})
@Description("Aggregates byte size and time duration columns")
public class AggregateStats implements Directive {
  
  private String sizeCol;
  private String timeCol;
  private String outSizeCol;
  private String outTimeCol;
  private String sizeUnit;
  private String timeUnit;
  private AggregationType aggregationType;
  
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    builder.define("size-column", TokenType.COLUMN_NAME);
    builder.define("time-column", TokenType.COLUMN_NAME);
    builder.define("output-size", TokenType.COLUMN_NAME);
    builder.define("output-time", TokenType.COLUMN_NAME);
    builder.define("size-unit", TokenType.TEXT, Optional.of("MB"));
    builder.define("time-unit", TokenType.TEXT, Optional.of("s"));
    builder.define("aggregation-type", TokenType.TEXT, Optional.of("TOTAL"));
    return builder.build();
  }
  
  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeCol = ((ColumnName) args.value("size-column")).value();
    this.timeCol = ((ColumnName) args.value("time-column")).value();
    this.outSizeCol = ((ColumnName) args.value("output-size")).value();
    this.outTimeCol = ((ColumnName) args.value("output-time")).value();
    this.sizeUnit = ((Text) args.value("size-unit")).value();
    this.timeUnit = ((Text) args.value("time-unit")).value();
    
    String aggType = ((Text) args.value("aggregation-type")).value();
    this.aggregationType = AggregationType.valueOf(aggType.toUpperCase());
  }
  
  @Override
  public Row execute(Row row, ExecutorContext context) throws DirectiveExecutionException {
    // Implementation details for aggregation...
    // ...
    return row;
  }
  
  // Helper methods for aggregation...
}
```

#### Execution Flow

The directive implements aggregation across rows:

```
┌─────────────┐     ┌────────────────┐     ┌────────────────┐
│Extract Values│────►│ Accumulate in  │────►│Generate Output │
│from Rows     │     │TransientStore  │     │if Last Row     │
└─────────────┘     └────────────────┘     └────────────────┘
```

1. For each row, extract the byte size and time duration values
2. Convert them to canonical units (bytes and milliseconds) 
3. Accumulate in the transient store
4. On the last row, generate a single row with the results

#### Key Implementation Details

```java
@Override
public Row execute(Row row, ExecutorContext context) throws DirectiveExecutionException {
  Object sizeObj = row.getValue(sizeCol);
  Object timeObj = row.getValue(timeCol);
  
  if (sizeObj != null) {
    ByteSize size = new ByteSize(sizeObj.toString());
    double bytes = size.getBytes();
    
    // Store in transient store
    String sizeKey = "aggregate.size.bytes";
    Double currentSize = context.getTransientStore().get(sizeKey);
    double newSize = (currentSize != null ? currentSize : 0.0) + bytes;
    context.getTransientStore().set(TransientVariableScope.GLOBAL, sizeKey, newSize);
  }
  
  if (timeObj != null) {
    TimeDuration time = new TimeDuration(timeObj.toString());
    double ms = time.getMilliseconds();
    
    // Store in transient store
    String timeKey = "aggregate.time.ms";
    Double currentTime = context.getTransientStore().get(timeKey);
    double newTime = (currentTime != null ? currentTime : 0.0) + ms;
    context.getTransientStore().set(TransientVariableScope.GLOBAL, timeKey, newTime);
  }
  
  // Increment row count for average calculation if needed
  if (aggregationType == AggregationType.AVERAGE) {
    String countKey = "aggregate.count";
    Integer count = context.getTransientStore().get(countKey);
    int newCount = (count != null ? count : 0) + 1;
    context.getTransientStore().set(TransientVariableScope.GLOBAL, countKey, newCount);
  }
  
  // Check if this is the last record
  boolean isLast = isLastRecord(context);
  if (isLast) {
    // Create output row with aggregated values
    Row output = new Row();
    
    // Get total size and convert to requested unit
    Double totalBytes = context.getTransientStore().get("aggregate.size.bytes");
    if (totalBytes != null) {
      double outSizeValue = convertByteSize(totalBytes, sizeUnit);
      
      if (aggregationType == AggregationType.AVERAGE) {
        Integer count = context.getTransientStore().get("aggregate.count");
        if (count != null && count > 0) {
          outSizeValue = outSizeValue / count;
        }
      }
      
      output.add(outSizeCol, outSizeValue);
    } else {
      output.add(outSizeCol, 0.0);
    }
    
    // Get total time and convert to requested unit
    Double totalMs = context.getTransientStore().get("aggregate.time.ms");
    if (totalMs != null) {
      double outTimeValue = convertTimeDuration(totalMs, timeUnit);
      
      if (aggregationType == AggregationType.AVERAGE) {
        Integer count = context.getTransientStore().get("aggregate.count");
        if (count != null && count > 0) {
          outTimeValue = outTimeValue / count;
        }
      }
      
      output.add(outTimeCol, outTimeValue);
    } else {
      output.add(outTimeCol, 0.0);
    }
    
    return output;
  }
  
  return null; // Skip non-last rows in aggregation
}
```

## Unit Testing

The module includes comprehensive tests for the new functionality:

### ByteSize and TimeDuration Tests

Tests to verify correct parsing and value calculations:

```java
@Test
public void testByteSizeParsing() {
  // Test various byte size formats
  ByteSize size1 = new ByteSize("10KB");
  assertEquals(10240.0, size1.getBytes(), 0.001);
  
  ByteSize size2 = new ByteSize("1.5MB");
  assertEquals(1.5, size2.getMegabytes(), 0.001);
  
  ByteSize size3 = new ByteSize("0.5GB");
  assertEquals(0.5, size3.getGigabytes(), 0.001);
}

@Test
public void testTimeDurationParsing() {
  // Test various time duration formats
  TimeDuration time1 = new TimeDuration("500ms");
  assertEquals(500.0, time1.getMilliseconds(), 0.001);
  
  TimeDuration time2 = new TimeDuration("2.5s");
  assertEquals(2.5, time2.getSeconds(), 0.001);
  
  TimeDuration time3 = new TimeDuration("1.5m");
  assertEquals(1.5, time3.getMinutes(), 0.001);
}
```

### Grammar Tests

Tests to verify correct parsing of directives with byte size and time duration arguments:

```java
@Test
public void testDirectiveWithByteSizeAndTimeDuration() {
  // Test directive with byte size and time duration arguments
  String[] directives = new String[] {
    "aggregate-stats :data_size :response_time total_size_mb total_time_sec 'MB' 's'"
  };
  
  try {
    RecipeParser parser = new GrammarBasedParser(Contexts.SYSTEM, directives);
    List<Directive> steps = parser.parse();
    
    // Verify the directive was parsed correctly
    assertEquals(1, steps.size());
    assertTrue(steps.get(0) instanceof AggregateStats);
  } catch (Exception e) {
    fail("Failed to parse directive: " + e.getMessage());
  }
}
```

### AggregateStats Tests

Tests to verify the directive's aggregation functionality:

```java
@Test
public void testBasicAggregation() {
  // Create test input rows
  List<Row> rows = new ArrayList<>();
  
  // Row 1: 10MB transfer, 500ms response time
  Row row1 = new Row();
  row1.add("data_size", "10MB");
  row1.add("response_time", "500ms");
  rows.add(row1);
  
  // Row 2: 5MB transfer, 300ms response time
  Row row2 = new Row();
  row2.add("data_size", "5MB");
  row2.add("response_time", "300ms");
  rows.add(row2);
  
  // Create recipe with aggregate-stats directive
  String[] recipe = new String[] {
    "aggregate-stats :data_size :response_time total_size_mb total_time_sec 'MB' 's'"
  };
  
  // Create execution context with isLast=true flag
  ExecutorContext context = new CustomExecutorContext(true);
  
  // Execute directive
  List<Row> results = TestingRig.execute(recipe, rows, context);
  
  // Verify results
  assertEquals(1, results.size());
  assertEquals(15.0, (Double) results.get(0).getValue("total_size_mb"), 0.001);
  assertEquals(0.8, (Double) results.get(0).getValue("total_time_sec"), 0.001);
}
```

## Integration with Other Modules

The `wrangler-core` module integrates with other modules in the following ways:

1. **wrangler-api**: Uses the interfaces and token types defined in the API module.
2. **wrangler-transform**: Provides directive implementations used in pipeline execution.
3. **wrangler-service**: Provides the grammar and parser used by the service.

## Contributing

When adding new directive functionality to the `wrangler-core` module:

1. Update the ANTLR grammar if needed
2. Add visitor methods to the RecipeVisitor to handle new grammar rules
3. Implement the Directive interface for new directives
4. Add comprehensive tests for all aspects of the implementation 