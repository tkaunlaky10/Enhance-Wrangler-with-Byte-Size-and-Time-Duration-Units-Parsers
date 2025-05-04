# Wrangler API Module

## Overview

The `wrangler-api` module defines the core interfaces, abstract classes, and token types used throughout the CDAP Wrangler system. This module serves as the foundation for the Wrangler data preparation tool, defining the contract between different components without implementing specific behaviors.

## Key Components

### Token System

The Token system is the backbone of Wrangler's directive parsing and execution:

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│ Token Interface│────►│ TokenType Enum│────►│Token Group    │
└───────────────┘     └───────────────┘     └───────────────┘
        │                     │                    │
        ▼                     ▼                    ▼
┌────────────────┐    ┌────────────────┐   ┌────────────────┐
│ Implementations │    │ Type Handlers  │   │ Argument Lists │
│ - Numeric       │    │ - Parsing      │   │ for Directives │
│ - String        │    │ - Validation   │   │                │
│ - ByteSize      │    │                │   │                │
│ - TimeDuration  │    │                │   │                │
└────────────────┘    └────────────────┘   └────────────────┘
```

#### Token Interface

The `Token` interface (`io.cdap.wrangler.api.parser.Token`) defines the contract for all tokens in Wrangler:

```java
public interface Token {
  /**
   * @return Type of the token.
   */
  TokenType type();

  /**
   * @return Value of the token.
   */
  Object value();
}
```

#### TokenType Enum

The `TokenType` enum defines all valid token types in the system:

```java
public enum TokenType {
  BOOLEAN,
  TEXT,
  NUMERIC,
  COLUMN_NAME,
  WHITESPACE,
  DIRECTIVE_NAME,
  BYTE_SIZE,     // New token type for byte sizes
  TIME_DURATION, // New token type for time durations
  ...
}
```

### New Token Implementations

#### ByteSize Token

The `ByteSize` class implements the `Token` interface for parsing and handling byte size values with units:

```java
public class ByteSize implements Token {
  private final String text;
  private final double value;
  private final String unit;
  
  public ByteSize(String text) {
    this.text = text;
    // Parse the numeric value and unit
    // ...
  }
  
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }
  
  @Override
  public Object value() {
    return text;
  }
  
  public double getBytes() {
    // Convert to bytes based on unit
    // ...
  }
  
  public double getKilobytes() {
    // Convert to KB
    // ...
  }
  
  // Additional unit conversion methods
  // ...
}
```

Key features:
- Parsing values like "10KB", "1.5MB", "2GB"
- Support for B, KB, MB, GB, TB, PB units
- Support for binary units (KiB, MiB, GiB, TiB)
- Methods for conversion between different units
- Mathematical operations (addition, multiplication)

#### TimeDuration Token

The `TimeDuration` class implements the `Token` interface for parsing and handling time duration values with units:

```java
public class TimeDuration implements Token {
  private final String text;
  private final double value;
  private final String unit;
  
  public TimeDuration(String text) {
    this.text = text;
    // Parse the numeric value and unit
    // ...
  }
  
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }
  
  @Override
  public Object value() {
    return text;
  }
  
  public double getMilliseconds() {
    // Convert to milliseconds based on unit
    // ...
  }
  
  public double getSeconds() {
    // Convert to seconds
    // ...
  }
  
  // Additional unit conversion methods
  // ...
}
```

Key features:
- Parsing values like "500ms", "2.5s", "10m"
- Support for ms, s, m, h, d, w units
- Methods for conversion between different time units
- Support for decimal values (e.g., "1.5s" = 1500ms)
- Mathematical operations (addition, multiplication)

### Directive System

The API module also defines the core interfaces for the directive system:

#### Directive Interface

The `Directive` interface defines the contract that all directives must implement:

```java
public interface Directive {
  /**
   * Configures the directive for execution.
   */
  void initialize(Arguments args) throws DirectiveParseException;

  /**
   * Executes the directive on the given row.
   */
  Row execute(Row row, ExecutorContext context) throws DirectiveExecutionException;

  /**
   * Returns the usage definition for this directive.
   */
  UsageDefinition define();
}
```

#### UsageDefinition

The `UsageDefinition` class defines how a directive should be used, including its name and accepted arguments:

```java
UsageDefinition.Builder builder = UsageDefinition.builder("my-directive");
builder.define("arg1", TokenType.COLUMN_NAME);
builder.define("arg2", TokenType.BYTE_SIZE);
builder.define("arg3", TokenType.TIME_DURATION);
UsageDefinition usage = builder.build();
```

The usage definition now supports the new `BYTE_SIZE` and `TIME_DURATION` token types.

## Integration with Other Modules

The `wrangler-api` module interfaces with other modules in the following ways:

1. **wrangler-core**: Implements the interfaces and uses the token types defined in the API.
2. **wrangler-transform**: Uses the directive interface for pipeline execution.
3. **wrangler-service**: Leverages the token system for parsing directives in the UI.

## Using the ByteSize and TimeDuration Classes

### ByteSize Example

```java
// Create a ByteSize token
ByteSize size = new ByteSize("15.5MB");

// Get the value in different units
double bytes = size.getBytes();         // 16252928.0
double kilobytes = size.getKilobytes(); // 15872.0
double megabytes = size.getMegabytes(); // 15.5
double gigabytes = size.getGigabytes(); // 0.015136...

// Use in a directive
builder.define("output-unit", TokenType.BYTE_SIZE);
// When parsing: new ByteSize(ctx.getText())
```

### TimeDuration Example

```java
// Create a TimeDuration token
TimeDuration duration = new TimeDuration("2.5s");

// Get the value in different units
double milliseconds = duration.getMilliseconds(); // 2500.0
double seconds = duration.getSeconds();          // 2.5
double minutes = duration.getMinutes();          // 0.041666...

// Use in a directive
builder.define("time-limit", TokenType.TIME_DURATION);
// When parsing: new TimeDuration(ctx.getText())
```

## Test Results

The ByteSize and TimeDuration implementations have been thoroughly tested with the following results:

### ByteSize Tests
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
```

Test coverage includes:
- Parsing of different byte units (B, KB, MB, GB, TB, PB)
- Support for binary units (KiB, MiB, GiB, TiB)
- Handling of decimal values (e.g., "1.5MB")
- Unit conversions between different byte units
- Mathematical operations
- Error handling for invalid formats
- JSON serialization and deserialization

### TimeDuration Tests
```
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```

Test coverage includes:
- Parsing of different time units (ns, ms, s, m, h, d, w)
- Support for full unit names (milliseconds, seconds, minutes, etc.)
- Handling of decimal values (e.g., "1.5s" = 1500ms)
- Unit conversions between different time units
- Addition and multiplication operations
- Error handling for invalid formats
- JSON serialization and deserialization

## Contributing

When adding new token types to the `wrangler-api` module:

1. Add the token type to the `TokenType` enum
2. Implement the `Token` interface
3. Update the appropriate parser visitor methods in `RecipeVisitor` to handle the new token type
4. Ensure directives can use the new token type in their usage definitions 