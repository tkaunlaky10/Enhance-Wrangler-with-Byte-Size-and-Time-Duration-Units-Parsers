# CDAP Wrangler ByteSize and TimeDuration Enhancement

## Overview

This project enhances the CDAP Wrangler data preparation tool with native support for byte size units (KB, MB, GB) and time duration units (ms, s, m, h). These enhancements allow users to work with storage sizes and time intervals directly in their directives, without requiring complex multi-step transformations.

## Key Features

### ByteSize Parser
- Parses data with byte size units (B, KB, MB, GB, TB, PB)
- Supports both decimal (KB = 1000B) and binary (KiB = 1024B) units
- Provides unit conversion methods (getBytes(), getMegabytes(), etc.)
- Performs mathematical operations on byte sizes

### TimeDuration Parser
- Parses data with time units (ns, ms, s, m, h, d, w)
- Handles decimal values correctly (e.g., "1.5s" = 1500ms)
- Provides unit conversion methods (getMilliseconds(), getSeconds(), etc.)
- Performs mathematical operations on time durations

### AggregateStats Directive
- New directive that demonstrates the use of ByteSize and TimeDuration parsers
- Aggregates values across multiple records (total or average)
- Supports different input and output units
- Syntax: `aggregate-stats :size_column :time_column out_size out_time [size_unit] [time_unit] [agg_type]`

## Implementation Components

### 1. Token Types
Added new token types to `wrangler-api/src/main/java/io/cdap/wrangler/api/parser/TokenType.java`:
```java
public enum TokenType {
  // Existing token types...
  BYTE_SIZE,     // For byte sizes (KB, MB, GB)
  TIME_DURATION, // For time durations (ms, s, m)
  // ...
}
```

### 2. Grammar Rules
Enhanced `wrangler-core/src/main/antlr4/io/cdap/wrangler/parser/Directives.g4` with new lexer rules:
```antlr
// Lexer rules for recognizing byte sizes and time durations
BYTE_SIZE : NUMBER BYTE_UNIT ;
fragment BYTE_UNIT : 'B' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' | 'KiB' | 'MiB' | 'GiB' | 'TiB' | 'PiB' ;

TIME_DURATION : NUMBER TIME_UNIT ;
fragment TIME_UNIT : 'ns' | 'ms' | 's' | 'm' | 'h' | 'd' | 'w' ;

// Parser rules
byteSizeArg : BYTE_SIZE ;
timeDurationArg : TIME_DURATION ;

// Integration with existing value rule
value : ... | byteSizeArg | timeDurationArg ;
```

### 3. ByteSize Implementation
Implemented in `wrangler-api/src/main/java/io/cdap/wrangler/api/parser/ByteSize.java`:
```java
public class ByteSize implements Token {
  private final String text;
  private final double value;
  private final String unit;
  private final boolean isBinary;
  
  // Core functionality
  public ByteSize(String text) { /* Parsing logic */ }
  public double getBytes() { /* Conversion to bytes */ }
  public double getKilobytes() { /* Conversion to KB */ }
  public double getMegabytes() { /* Conversion to MB */ }
  // Additional methods...
}
```

### 4. TimeDuration Implementation
Implemented in `wrangler-api/src/main/java/io/cdap/wrangler/api/parser/TimeDuration.java`:
```java
public class TimeDuration implements Token {
  private final String text;
  private final double value;
  private final String unit;
  private final long totalMilliseconds;
  
  // Core functionality
  public TimeDuration(String text) { /* Parsing logic */ }
  public long getMilliseconds() { /* Get milliseconds */ }
  public double getSeconds() { /* Conversion to seconds */ }
  public double getMinutes() { /* Conversion to minutes */ }
  // Additional methods...
}
```

### 5. RecipeVisitor Updates
Enhanced `wrangler-core/src/main/java/io/cdap/wrangler/parser/RecipeVisitor.java` to handle new token types:
```java
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
```

### 6. AggregateStats Directive
Implemented a directive to demonstrate the ByteSize and TimeDuration functionality in `wrangler-core/src/main/java/io/cdap/directives/aggregates/AggregateStats.java`:
```java
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Categories(categories = { "aggregator"})
@Description("Aggregates byte size and time duration columns")
public class AggregateStats implements Directive {
  // Implementation details...
}
```

## Test Results

All implementation components have been thoroughly tested:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running io.cdap.directives.aggregates.AggregateStatsTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.565 sec

Running io.cdap.wrangler.api.parser.TimeDurationTest
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.152 sec

Running io.cdap.wrangler.api.parser.ByteSizeTest
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.138 sec

Running io.cdap.wrangler.parser.GrammarBasedParserTest
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.283 sec

Results :

Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

For detailed test results, see [TEST_RESULTS.md](TEST_RESULTS.md).

## Usage Examples

### ByteSize Example

```
// Input data with mixed byte size units
// Row 1: file_size=2.5MB
// Row 2: file_size=500KB
// Row 3: file_size=1GB

// Directive that handles all units automatically
aggregate-stats :file_size :response_time total_size_gb total_time_sec 'GB' 's'

// Output:
// Row 1: total_size_gb=1.002975 (units automatically converted and aggregated)
```

### TimeDuration Example

```
// Input data with mixed time units
// Row 1: processing_time=3s
// Row 2: processing_time=250ms
// Row 3: processing_time=1.2m

// Directive that handles all units automatically
aggregate-stats :file_size :processing_time total_size_gb total_time_sec 'GB' 's'

// Output:
// Row 1: total_time_sec=75.25 (units automatically converted and aggregated)
```

## Building and Testing

To build and test the implementation:

```bash
# Run all tests
mvn test -Dcheckstyle.skip=true -Drat.skip=true

# Run specific test classes
mvn test -Dtest=ByteSizeTest -Dcheckstyle.skip=true -Drat.skip=true
mvn test -Dtest=TimeDurationTest -Dcheckstyle.skip=true -Drat.skip=true
mvn test -Dtest=AggregateStatsTest -Dcheckstyle.skip=true -Drat.skip=true
mvn test -Dtest=GrammarBasedParserTest -Dcheckstyle.skip=true -Drat.skip=true
```

## Detailed Documentation

For detailed information about each component, see:
- [Wrangler API README](wrangler-api/README.md) - For ByteSize and TimeDuration token implementations
- [Wrangler Core README](wrangler-core/README.md) - For grammar enhancements and AggregateStats directive 