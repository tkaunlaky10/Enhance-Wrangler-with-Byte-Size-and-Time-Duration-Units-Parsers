# Data Prep

![cm-available](https://cdap-users.herokuapp.com/assets/cm-available.svg)
![cdap-transform](https://cdap-users.herokuapp.com/assets/cdap-transform.svg)
[![Build Status](https://travis-ci.org/cdapio/hydrator-plugins.svg?branch=develop)](https://travis-ci.org/cdapio/hydrator-plugins)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/11434/badge.svg)](https://scan.coverity.com/projects/hydrator-wrangler-transform)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cdap.wrangler/wrangler-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cdap.wrangler/wrangler-core)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/io.cdap.wrangler/wrangler-core/badge.svg)](http://www.javadoc.io/doc/io.cdap.wrangler/wrangler-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Join CDAP community](https://cdap-users.herokuapp.com/badge.svg?t=wrangler)](https://cdap-users.herokuapp.com?t=1)

A collection of libraries, a pipeline plugin, and a CDAP service for performing data
cleansing, transformation, and filtering using a set of data manipulation instructions
(directives). These instructions are either generated using an interative visual tool or
are manually created.

  * Data Prep defines few concepts that might be useful if you are just getting started with it. Learn about them [here](wrangler-docs/concepts.md)
  * The Data Prep Transform is [separately documented](wrangler-transform/wrangler-docs/data-prep-transform.md).
  * [Data Prep Cheatsheet](wrangler-docs/cheatsheet.md)

## ByteSize and TimeDuration Parsers

Wrangler now includes support for byte size and time duration units, making it easy to work with columns that contain data sizes (KB, MB, GB) and time intervals (ms, s, m, h).

### ByteSize Parser

The ByteSize parser makes it easy to handle data sizes with their units. It supports:

- Various byte units: B, KB, MB, GB, TB, PB, etc.
- Binary units: KiB, MiB, GiB, TiB, PiB, etc.
- Automatic conversion to canonical bytes for calculations

**Examples:**
```
10MB   - 10 megabytes
5KB    - 5 kilobytes
1.5GB  - 1.5 gigabytes
```

### TimeDuration Parser

The TimeDuration parser handles time durations with their units. It supports:

- Time units: ns, ms, s, m, h, d, w
- Full names: nanoseconds, milliseconds, seconds, minutes, hours, days, weeks
- Automatic conversion to canonical milliseconds for calculations

**Examples:**
```
500ms     - 500 milliseconds
2s        - 2 seconds
5m        - 5 minutes
1.5h      - 1.5 hours
```

### Aggregate-Stats Directive

The `aggregate-stats` directive demonstrates the use of these new parsers. It allows you to aggregate byte sizes and time durations across multiple records:

```
aggregate-stats :size_column :time_column target_size target_time [size_unit] [time_unit] [aggregation_type]
```

**Arguments:**
- `size_column`: Source column containing byte sizes
- `time_column`: Source column containing time durations
- `target_size`: Target column name for aggregated size
- `target_time`: Target column name for aggregated time
- `size_unit` (optional): Output unit for size (e.g., 'MB', 'GB')
- `time_unit` (optional): Output unit for time (e.g., 's', 'm')
- `aggregation_type` (optional): Aggregation type ('TOTAL' or 'AVERAGE')

**Example Recipe:**
```
aggregate-stats :data_transfer_size :response_time total_size_mb avg_time_sec 'MB' 's' 'AVERAGE'
```

## New Features

More [here](wrangler-docs/upcoming-features.md) on upcoming features.

  * **User Defined Directives, also known as UDD**, allow you to create custom functions to transform records within CDAP DataPrep or a.k.a Wrangler. CDAP comes with a comprehensive library of functions. There are however some omissions, and some specific cases for which UDDs are the solution. Additional information on how you can build your custom directives [here](wrangler-docs/custom-directive.md).
    * Migrating directives from version 1.0 to version 2.0 [here](wrangler-docs/directive-migration.md)
    * Information about Grammar [here](wrangler-docs/grammar/grammar-info.md)
    * Various `TokenType` supported by system [here](../api/src/main/java/io/cdap/wrangler/api/parser/TokenType.java)
    * Custom Directive Implementation Internals [here](wrangler-docs/udd-internal.md)

  * A new capability that allows CDAP Administrators to **restrict the directives** that are accessible to their users.
More information on configuring can be found [here](wrangler-docs/exclusion-and-aliasing.md)

## Demo Videos and Recipes

Videos and Screencasts are best way to learn, so we have compiled simple, short screencasts that shows some of the features of Data Prep. Additional videos can be found [here](https://www.youtube.com/playlist?list=PLhmsf-NvXKJn-neqefOrcl4n7zU4TWmIr)

### Videos

  * [SCREENCAST] [Creating Lookup Dataset and Joining](https://www.youtube.com/watch?v=Nc1b0rsELHQ)
  * [SCREENCAST] [Restricted Directives](https://www.youtube.com/watch?v=71EcMQU714U)
  * [SCREENCAST] [Parse Excel files in CDAP](https://www.youtube.com/watch?v=su5L1noGlEk)
  * [SCREENCAST] [Parse File As AVRO File](https://www.youtube.com/watch?v=tmwAw4dKUNc)
  * [SCREENCAST] [Parsing Binary Coded AVRO Messages](https://www.youtube.com/watch?v=Ix_lPo-PDJY)
  * [SCREENCAST] [Parsing Binary Coded AVRO Messages & Protobuf messages using schema registry](https://www.youtube.com/watch?v=LVLIdWnUX1k)
  * [SCREENCAST] [Quantize a column - Digitize](https://www.youtube.com/watch?v=VczkYX5SRtY)
  * [SCREENCAST] [Data Cleansing capability with send-to-error directive](https://www.youtube.com/watch?v=aZd5H8hIjDc)
  * [SCREENCAST] [Building Data Prep from the GitHub source](https://youtu.be/pGGjKU04Y38)
  * [VOICE-OVER] [End-to-End Demo Video](https://youtu.be/AnhF0qRmn24)
  * [SCREENCAST] [Ingesting into Kudu](https://www.youtube.com/watch?v=KBW7a38vlUM)
  * [SCREENCAST] [Realtime HL7 CCDA XML from Kafka into Time Parititioned Parquet](https://youtu.be/0fqNmnOnD-0)
  * [SCREENCAST] [Parsing JSON file](https://youtu.be/vwnctcGDflE)
  * [SCREENCAST] [Flattening arrays](https://youtu.be/SemHxgBYIsY)
  * [SCREENCAST] [Data cleansing with send-to-error directive](https://www.youtube.com/watch?v=aZd5H8hIjDc)
  * [SCREENCAST] [Publishing to Kafka](https://www.youtube.com/watch?v=xdc8pvvlI48)
  * [SCREENCAST] [Fixed length to JSON](https://www.youtube.com/watch?v=3AXu4m1swuM)

### Recipes

  * [Parsing Apache Log Files](wrangler-demos/parsing-apache-log-files.md)
  * [Parsing CSV Files and Extracting Column Values](wrangler-demos/parsing-csv-extracting-column-values.md)
  * [Parsing HL7 CCDA XML Files](wrangler-demos/parsing-hl7-ccda-xml-files.md)

## Available Directives

These directives are currently available:

| Directive                                                              | Description                                                      |
| ---------------------------------------------------------------------- | ---------------------------------------------------------------- |
| **Parsers**                                                            |                                                                  |
| [JSON Path](wrangler-docs/directives/json-path.md)                              | Uses a DSL (a JSON path expression) for parsing JSON records     |
| [Parse as AVRO](wrangler-docs/directives/parse-as-avro.md)                      | Parsing an AVRO encoded message - either as binary or json       |
| [Parse as AVRO File](wrangler-docs/directives/parse-as-avro-file.md)            | Parsing an AVRO data file                                        |
| [Parse as CSV](wrangler-docs/directives/parse-as-csv.md)                        | Parsing an input record as comma-separated values                |
| [Parse as Date](wrangler-docs/directives/parse-as-date.md)                      | Parsing dates using natural language processing                  |
| [Parse as Excel](wrangler-docs/directives/parse-as-excel.md)                    | Parsing excel file.                                              |
| [Parse as Fixed Length](wrangler-docs/directives/parse-as-fixed-length.md)      | Parses as a fixed length record with specified widths            |
| [Parse as HL7](wrangler-docs/directives/parse-as-hl7.md)                        | Parsing Health Level 7 Version 2 (HL7 V2) messages               |
| [Parse as JSON](wrangler-docs/directives/parse-as-json.md)                      | Parsing a JSON object                                            |
| [Parse as Log](wrangler-docs/directives/parse-as-log.md)                        | Parses access log files as from Apache HTTPD and nginx servers   |
| [Parse as Protobuf](wrangler-docs/directives/parse-as-log.md)                   | Parses an Protobuf encoded in-memory message using descriptor    |
| [Parse as Simple Date](wrangler-docs/directives/parse-as-simple-date.md)        | Parses date strings                                              |
| [Parse XML To JSON](wrangler-docs/directives/parse-xml-to-json.md)              | Parses an XML document into a JSON structure                     |
| [Parse as Currency](wrangler-docs/directives/parse-as-currency.md)              | Parses a string representation of currency into a number.        |
| [Parse as Datetime](wrangler-docs/directives/parse-as-datetime.md)              | Parses strings with datetime values to CDAP datetime type        |
| **Output Formatters**                                                  |                                                                  |
| [Write as CSV](wrangler-docs/directives/write-as-csv.md)                        | Converts a record into CSV format                                |
| [Write as JSON](wrangler-docs/directives/write-as-json-map.md)                  | Converts the record into a JSON map                              |
| [Write JSON Object](wrangler-docs/directives/write-as-json-object.md)           | Composes a JSON object based on the fields specified.            |
| [Format as Currency](wrangler-docs/directives/format-as-currency.md)            | Formats a number as currency as specified by locale.             |
| **Transformations**                                                    |                                                                  |
| [Changing Case](wrangler-docs/directives/changing-case.md)                      | Changes the case of column values                                |
| [Cut Character](wrangler-docs/directives/cut-character.md)                      | Selects parts of a string value                                  |
| [Set Column](wrangler-docs/directives/set-column.md)                            | Sets the column value to the result of an expression execution   |
| [Find and Replace](wrangler-docs/directives/find-and-replace.md)                | Transforms string column values using a "sed"-like expression    |
| [Index Split](wrangler-docs/directives/index-split.md)                          | (_Deprecated_)                                                   |
| [Invoke HTTP](wrangler-docs/directives/invoke-http.md)                          | Invokes an HTTP Service (_Experimental_, potentially slow)       |
| [Quantization](wrangler-docs/directives/quantize.md)                            | Quantizes a column based on specified ranges                     |
| [Regex Group Extractor](wrangler-docs/directives/extract-regex-groups.md)       | Extracts the data from a regex group into its own column         |
| [Setting Character Set](wrangler-docs/directives/set-charset.md)                | Sets the encoding and then converts the data to a UTF-8 String   |
| [Setting Record Delimiter](wrangler-docs/directives/set-record-delim.md)        | Sets the record delimiter                                        |
| [Split by Separator](wrangler-docs/directives/split-by-separator.md)            | Splits a column based on a separator into two columns            |
| [Split Email Address](wrangler-docs/directives/split-email.md)                  | Splits an email ID into an account and its domain                |
| [Split URL](wrangler-docs/directives/split-url.md)                              | Splits a URL into its constituents                               |
| [Text Distance (Fuzzy String Match)](wrangler-docs/directives/text-distance.md) | Measures the difference between two sequences of characters      |
| [Text Metric (Fuzzy String Match)](wrangler-docs/directives/text-metric.md)     | Measures the difference between two sequences of characters      |
| [URL Decode](wrangler-docs/directives/url-decode.md)                            | Decodes from the `application/x-www-form-urlencoded` MIME format |
| [URL Encode](wrangler-docs/directives/url-encode.md)                            | Encodes to the `application/x-www-form-urlencoded` MIME format   |
| [Trim](wrangler-docs/directives/trim.md)                                        | Functions for trimming white spaces around string data           |
| **Encoders and Decoders**                                              |                                                                  |
| [Decode](wrangler-docs/directives/decode.md)                                    | Decodes a column value as one of `base32`, `base64`, or `hex`    |
| [Encode](wrangler-docs/directives/encode.md)                                    | Encodes a column value as one of `base32`, `base64`, or `hex`    |
| **Unique ID**                                                          |                                                                  |
| [UUID Generation](wrangler-docs/directives/generate-uuid.md)                    | Generates a universally unique identifier (UUID) .Recommended to use with Wrangler version 4.4.0 and above due to an important bug fix [CDAP-17732](https://cdap.atlassian.net/browse/CDAP-17732)             |
| **Date Transformations**                                               |                                                                  |
| [Diff Date](wrangler-docs/directives/diff-date.md)                              | Calculates the difference between two dates                      |
| [Format Date](wrangler-docs/directives/format-date.md)                          | Custom patterns for date-time formatting                         |
| [Format Unix Timestamp](wrangler-docs/directives/format-unix-timestamp.md)      | Formats a UNIX timestamp as a date                               |
| **DateTime Transformations**                                                    |                                                                  |
| [Current DateTime](wrangler-docs/directives/current-datetime.md)                | Generates the current datetime using the given zone or UTC by default|
| [Datetime To Timestamp](wrangler-docs/directives/datetime-to-timestamp.md)      | Converts a datetime value to timestamp with the given zone       |
| [Format Datetime](wrangler-docs/directives/format-datetime.md)                  | Formats a datetime value to custom date time pattern strings     |
| [Timestamp To Datetime](wrangler-docs/directives/timestamp-to-datetime.md)      | Converts a timestamp value to datetime                           |
| **Lookups**                                                            |                                                                  |
| [Catalog Lookup](wrangler-docs/directives/catalog-lookup.md)                    | Static catalog lookup of ICD-9, ICD-10-2016, ICD-10-2017 codes   |
| [Table Lookup](wrangler-docs/directives/table-lookup.md)                        | Performs lookups into Table datasets                             |
| **Hashing & Masking**                                                  |                                                                  |
| [Message Digest or Hash](wrangler-docs/directives/hash.md)                      | Generates a message digest                                       |
| [Mask Number](wrangler-docs/directives/mask-number.md)                          | Applies substitution masking on the column values                |
| [Mask Shuffle](wrangler-docs/directives/mask-shuffle.md)                        | Applies shuffle masking on the column values                     |
| **Row Operations**                                                     |                                                                  |
| [Filter Row if Matched](wrangler-docs/directives/filter-row-if-matched.md)      | Filters rows that match a pattern for a column                                         |
| [Filter Row if True](wrangler-docs/directives/filter-row-if-true.md)            | Filters rows if the condition is true.                                                  |
| [Filter Row Empty of Null](wrangler-docs/directives/filter-empty-or-null.md)    | Filters rows that are empty of null.                    |
| [Flatten](wrangler-docs/directives/flatten.md)                                  | Separates the elements in a repeated field                       |
| [Fail on condition](wrangler-docs/directives/fail.md)                           | Fails processing when the condition is evaluated to true.        |
| [Send to Error](wrangler-docs/directives/send-to-error.md)                      | Filtering of records to an error collector                       |
| [Send to Error And Continue](wrangler-docs/directives/send-to-error-and-continue.md) | Filtering of records to an error collector and continues processing                      |
| [Split to Rows](wrangler-docs/directives/split-to-rows.md)                      | Splits based on a separator into multiple records                |
| **Column Operations**                                                  |                                                                  |
| [Change Column Case](wrangler-docs/directives/change-column-case.md)            | Changes column names to either lowercase or uppercase            |
| [Changing Case](wrangler-docs/directives/changing-case.md)                      | Change the case of column values                                 |
| [Cleanse Column Names](wrangler-docs/directives/cleanse-column-names.md)        | Sanatizes column names, following specific rules                 |
| [Columns Replace](wrangler-docs/directives/columns-replace.md)                  | Alters column names in bulk                                      |
| [Copy](wrangler-docs/directives/copy.md)                                        | Copies values from a source column into a destination column     |
| [Drop Column](wrangler-docs/directives/drop.md)                                 | Drops a column in a record                                       |
| [Fill Null or Empty Columns](wrangler-docs/directives/fill-null-or-empty.md)    | Fills column value with a fixed value if null or empty           |
| [Keep Columns](wrangler-docs/directives/keep.md)                                | Keeps specified columns from the record                          |
| [Merge Columns](wrangler-docs/directives/merge.md)                              | Merges two columns by inserting a third column                   |
| [Rename Column](wrangler-docs/directives/rename.md)                             | Renames an existing column in the record                         |
| [Set Column Header](wrangler-docs/directives/set-headers.md)                     | Sets the names of columns, in the order they are specified       |
| [Split to Columns](wrangler-docs/directives/split-to-columns.md)                | Splits a column based on a separator into multiple columns       |
| [Swap Columns](wrangler-docs/directives/swap.md)                                | Swaps column names of two columns                                |
| [Set Column Data Type](wrangler-docs/directives/set-type.md)                    | Convert data type of a column                                    |
| **NLP**                                                                |                                                                  |
| [Stemming Tokenized Words](wrangler-docs/directives/stemming.md)                | Applies the Porter stemmer algorithm for English words           |
| **Transient Aggregators & Setters**                                    |                                                                  |
| [Increment Variable](wrangler-docs/directives/increment-variable.md)            | Increments a transient variable with a record of processing.     |
| [Set Variable](wrangler-docs/directives/set-variable.md)                        | Sets a transient variable with a record of processing.     |
| **Functions**                                                          |                                                                  |
| [Data Quality](wrangler-docs/functions/dq-functions.md)                         | Data quality check functions. Checks for date, time, etc.        |
| [Date Manipulations](wrangler-docs/functions/date-functions.md)                 | Functions that can manipulate date                               |
| [DDL](wrangler-docs/functions/ddl-functions.md)                                 | Functions that can manipulate definition of data                 |
| [JSON](wrangler-docs/functions/json-functions.md)                               | Functions that can be useful in transforming your data           |
| [Types](wrangler-docs/functions/type-functions.md)                              | Functions for detecting the type of data                         |

## Performance

Initial performance tests show that with a set of directives of high complexity for
transforming data, *DataPrep* is able to process at about ~106K records per second. The
rates below are specified as *records/second*. 

| Directive Complexity | Column Count |    Records |           Size | Mean Rate |
| -------------------- | :----------: | ---------: | -------------: | --------: |
| High (167 Directives) |      426      | 127,946,398 |  82,677,845,324 | 106,367.27 |
| High (167 Directives) |      426      | 511,785,592 | 330,711,381,296 | 105,768.93 |


## Contact

### Mailing Lists

CDAP User Group and Development Discussions:

* [cdap-user@googlegroups.com](https://groups.google.com/d/forum/cdap-user)

The *cdap-user* mailing list is primarily for users using the product to develop
applications or building plugins for appplications. You can expect questions from
users, release announcements, and any other discussions that we think will be helpful
to the users.

### IRC Channel

CDAP IRC Channel: [#cdap on irc.freenode.net](http://webchat.freenode.net?channels=%23cdap)

### Slack Team

CDAP Users on Slack: [cdap-users team](https://cdap-users.herokuapp.com)


## License and Trademarks

Copyright © 2016-2019 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

Cask is a trademark of Cask Data, Inc. All rights reserved.

Apache, Apache HBase, and HBase are trademarks of The Apache Software Foundation. Used with
permission. No endorsement by The Apache Software Foundation is implied by the use of these marks.

# CDAP Wrangler Enhancement: ByteSize and TimeDuration Parsers

## Overview
This repository contains an enhanced version of the CDAP Wrangler data preparation tool that adds support for parsing and manipulating byte sizes (KB, MB, GB) and time durations (ms, s, m).

These new capabilities enable users to:
- Parse data that includes size units (KB, MB, GB, etc.)
- Parse data that includes time duration units (ms, s, m, etc.)
- Perform aggregations on these values with proper unit handling
- Convert between different units seamlessly

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [New Parsers](#new-parsers)
  - [ByteSize Parser](#bytesize-parser)
  - [TimeDuration Parser](#timeduration-parser)
- [Grammar Enhancements](#grammar-enhancements)
- [Directive System](#directive-system)
- [New Directive: aggregate-stats](#new-directive-aggregate-stats)
- [Examples](#examples)
- [Implementation Details](#implementation-details)

## Architecture Overview

### Wrangler System Architecture

The CDAP Wrangler library is designed as a data preparation tool with several key components:

```
┌─────────────────────────────────────────────────────────────────┐
│                       CDAP Wrangler System                       │
├─────────────┬───────────────┬────────────────┬─────────────────┤
│ wrangler-api│ wrangler-core │ wrangler-trans.│  wrangler-serv. │
│             │               │                │                 │
│ - Token     │ - Grammar     │ - Pipeline     │ - REST API      │
│ - Directive │ - Parser      │   execution    │ - UI integration│
│ - Interfaces│ - Directives  │ - ETL plugins  │                 │
└─────────────┴───────────────┴────────────────┴─────────────────┘
```

The key components include:

1. **wrangler-api**: Contains core interfaces and abstract classes defining the Wrangler functionality, including the Token system.
2. **wrangler-core**: Contains the implementation of the grammar and directives that power the transformation engine.
3. **wrangler-transform**: CDAP ETL plugin that integrates Wrangler into data pipelines.
4. **wrangler-service**: Service and REST endpoints for interactive Wrangler usage.

### Directive Execution Flow

```
┌──────────┐    ┌────────────┐    ┌────────────┐    ┌─────────────┐
│  Recipe   │───►│ Tokenizer  │───►│   Parser   │───►│  Directives │
│(Directives)    │(Lexer/ANTLR)    │(ANTLR/Java)│    │  Execution  │
└──────────┘    └────────────┘    └────────────┘    └─────────────┘
                                        │                  ▲
                                        ▼                  │
                                  ┌──────────────┐         │
                                  │ Token Groups │─────────┘
                                  └──────────────┘
```

The directive execution flow:
1. User provides a recipe (list of directives)
2. ANTLR lexer tokenizes the directives
3. ANTLR parser processes the tokens into a parse tree
4. Java code visits the parse tree to create Token objects
5. Tokens are organized into TokenGroups and passed to directive implementations
6. Directives execute the transformations on the data

## New Parsers

### ByteSize Parser

The ByteSize parser is implemented as a new token type that can recognize and parse byte size values with units:

- Supports units: B, KB, MB, GB, TB, PB
- Handles both decimal and binary interpretations (1KB = 1000 bytes or 1024 bytes)
- Provides methods to convert between different units

#### Token Implementation

The `ByteSize` class extends the `Token` interface and provides:
- Parsing of strings like "10KB", "1.5MB", "2GB"
- Conversion to canonical bytes value
- Methods to get the value in different units

### TimeDuration Parser

The TimeDuration parser is implemented as a token type that recognizes time duration values with units:

- Supports units: ms (milliseconds), s (seconds), m (minutes), h (hours)
- Handles both integer and decimal values
- Provides methods to convert between different time units

#### Token Implementation

The `TimeDuration` class extends the `Token` interface and provides:
- Parsing of strings like "500ms", "2.5s", "10m"
- Conversion to canonical time value (milliseconds)
- Methods to get the value in different time units

## Grammar Enhancements

The Wrangler grammar (defined in `Directives.g4`) has been enhanced to recognize the new token types:

```antlr
BYTE_SIZE : NUMBER BYTE_UNIT ;
fragment BYTE_UNIT : 'B' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' ;

TIME_DURATION : NUMBER TIME_UNIT ;
fragment TIME_UNIT : 'ms' | 's' | 'm' | 'h' ;
```

New parser rules have been added:
- `byteSizeArg`: For accepting byte size arguments
- `timeDurationArg`: For accepting time duration arguments

These rules are integrated into the directive argument system, allowing directives to specify these new types as valid arguments.

## Directive System

### How Directives Work

Directives in Wrangler follow this execution process:

```
┌────────────┐    ┌─────────────┐    ┌──────────────┐
│ Directive  │───►│  Arguments  │───►│ Transformation│
│ Definition │    │ Extraction  │    │    Logic     │
└────────────┘    └─────────────┘    └──────────────┘
      │                                      │
      ▼                                      ▼
┌────────────┐                        ┌──────────────┐
│  Usage     │                        │ Modified     │
│ Definition │                        │    Data      │
└────────────┘                        └──────────────┘
```

1. Each directive is defined with a name and a set of arguments it accepts
2. The directive parser extracts arguments based on the usage definition
3. Arguments are validated and converted to appropriate types
4. The directive's transformation logic is applied to the data
5. The modified data is returned

### Token Type Integration

The new ByteSize and TimeDuration parsers are integrated as token types in the directive system:

```
┌────────────┐    ┌─────────────┐    ┌─────────────┐
│ Token Type │───►│ Token Class │───►│ Parser Rule │
│ Definition │    │Implementation│    │ Integration │
└────────────┘    └─────────────┘    └─────────────┘
      │                 │                   │
      ▼                 ▼                   ▼
┌────────────┐    ┌─────────────┐    ┌─────────────┐
│  Enum in   │    │ ByteSize.java│    │ byteSizeArg │
│TokenType.java   │TimeDuration.java  │timeDurationArg
└────────────┘    └─────────────┘    └─────────────┘
```

## New Directive: aggregate-stats

The `aggregate-stats` directive demonstrates the usage of the new parsers by performing aggregations on byte size and time duration values.

### Usage

```
aggregate-stats <byte-size-column> <time-duration-column> <output-size-column> <output-time-column> [<size-unit>] [<time-unit>] [<aggregation-type>]
```

- `byte-size-column`: Column containing byte size values (e.g., "10MB")
- `time-duration-column`: Column containing time duration values (e.g., "500ms")
- `output-size-column`: Target column for size aggregation result
- `output-time-column`: Target column for time aggregation result
- `size-unit`: (Optional) Output unit for size (default: "MB")
- `time-unit`: (Optional) Output unit for time (default: "s")
- `aggregation-type`: (Optional) Type of aggregation: "TOTAL" or "AVERAGE" (default: "TOTAL")

### Execution Flow

The directive works as an aggregator, accumulating values across rows:

```
┌─────────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐
│Input Rows│────►│Parse Values│────►│Accumulate  │────►│Convert to  │
│          │     │            │     │Running Total     │Output Units│
└─────────┘     └────────────┘     └────────────┘     └────────────┘
                                                             │
┌─────────┐                                                  │
│Output Row│◄────────────────────────────────────────────────┘
│(single)  │
└─────────┘
```

1. For each row, extract the byte size and time duration values
2. Convert them to canonical units (bytes and milliseconds)
3. Accumulate the values in the transient store
4. On the last row, generate a single output row with the aggregated values
5. Convert the aggregated values to the specified output units

## Examples

### Example 1: Total Aggregation

```
// Input data:
// Row 1: data_transfer_size=10MB, response_time=500ms
// Row 2: data_transfer_size=5MB, response_time=300ms
// Row 3: data_transfer_size=15MB, response_time=700ms

// Directive:
aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec 'MB' 's'

// Output:
// Row 1: total_size_mb=30.0, total_time_sec=1.5
```

### Example 2: Average Aggregation with Different Units

```
// Input data:
// Row 1: data_transfer_size=10MB, response_time=500ms
// Row 2: data_transfer_size=5120KB, response_time=1500ms
// Row 3: data_transfer_size=0.015GB, response_time=0.5m

// Directive:
aggregate-stats :data_transfer_size :response_time avg_size_gb avg_time_min 'GB' 'm' 'AVERAGE'

// Output:
// Row 1: avg_size_gb=0.01, avg_time_min=0.5
```

## Implementation Details

### Core Classes

1. **ByteSize.java**: Implements the Token interface to parse and manipulate byte size values
   - Location: `wrangler-api/src/main/java/io/cdap/wrangler/api/parser/ByteSize.java`
   - Key methods: `getBytes()`, `getKilobytes()`, `getMegabytes()`, etc.

2. **TimeDuration.java**: Implements the Token interface to parse and manipulate time duration values
   - Location: `wrangler-api/src/main/java/io/cdap/wrangler/api/parser/TimeDuration.java`
   - Key methods: `getMilliseconds()`, `getSeconds()`, `getMinutes()`, etc.

3. **TokenType.java**: Enum containing the token types including the new BYTE_SIZE and TIME_DURATION types
   - Location: `wrangler-api/src/main/java/io/cdap/wrangler/api/parser/TokenType.java`

4. **RecipeVisitor.java**: Extended to handle the new parser rules for byte size and time duration arguments
   - Location: `wrangler-core/src/main/java/io/cdap/wrangler/parser/RecipeVisitor.java`
   - Key methods: `visitByteSizeArg()`, `visitTimeDurationArg()`

5. **AggregateStats.java**: New directive implementing aggregation for byte sizes and time durations
   - Location: `wrangler-core/src/main/java/io/cdap/directives/aggregates/AggregateStats.java`
   - Key methods: `execute()`, `reset()`, `destroy()`

### Testing

Comprehensive tests have been added for:

1. **ByteSize parsing**: Unit tests for correct parsing of different byte size formats
2. **TimeDuration parsing**: Unit tests for correct parsing of different time duration formats
3. **Grammar parsing**: Tests to ensure directives using the new token types are parsed correctly
4. **AggregateStats directive**: Tests for different aggregation scenarios and edge cases

## Conclusion

The addition of ByteSize and TimeDuration parsers to the CDAP Wrangler library significantly enhances its capabilities for handling and manipulating data with size and time units. The implementation follows the existing architecture and patterns of the Wrangler system while extending it with new functionality.

These enhancements enable more efficient data preparation workflows, eliminating the need for complex multi-step transformations when working with data size and time duration values.

## Implementation Notes and Known Issues

### Java Compatibility

This project is configured to build with Java 8. There are known compatibility issues when trying to build with Java 9 or newer versions. If you attempt to build with Java 9+, you will likely encounter warnings and errors similar to the following:

```
[WARNING] bootstrap class path not set in conjunction with -source 8
[WARNING] source value 8 is obsolete and will be removed in a future release
[WARNING] target value 8 is obsolete and will be removed in a future release
[WARNING] To suppress warnings about obsolete options, use -Xlint:-options.
```

Additionally, various checkstyle issues may appear when implementing new features, such as:

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:2.17:check (validate) on project wrangler-api: 
Failed during checkstyle execution: There are 15 errors reported by Checkstyle 6.19 with checkstyle.xml ruleset.
```

For the most reliable build process, it is recommended to use Java 8.

### DataPrep Concepts

This implementation of Data Prep uses the following concepts:

#### Recipe
A Recipe is a collection of Directives. It consists of one or more Directives.

#### Directive
A Directive is a single data manipulation instruction, specified to either transform, filter, or pivot a single record into zero or more records. A directive can generate one or more steps to be executed by a pipeline.

#### Row
A Row is a collection of field names and field values.

#### Column
A Column is a data value of any of the supported Java types, one for each record.

#### Pipeline
A Pipeline is a collection of steps to be applied on a record. The record(s) outputed from a step are passed to the next step in the pipeline.

### Parsing Process

The ByteSize and TimeDuration parsers are integrated into the ANTLR4 parsing workflow:

1. **Lexical Analysis** - ANTLR4's lexer scans the directive text and identifies tokens based on defined patterns (e.g., recognizing "10MB" as a BYTE_SIZE token)

2. **Parsing** - The parser combines tokens into structured elements according to grammar rules

3. **Visitor Pattern** - The `RecipeVisitor` converts parse tree nodes into proper Java objects:
   - BYTE_SIZE tokens become `ByteSize` objects
   - TIME_DURATION tokens become `TimeDuration` objects

4. **Directive Execution** - The tokens are passed to directive implementations which can use their specific type semantics

This enables natural expression of sizes and durations in directives like:
```
aggregate-stats :data_transfer_size :response_time total_size_mb avg_time_sec 'MB' 's' 'AVERAGE'
```
