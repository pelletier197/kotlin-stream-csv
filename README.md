[![CircleCI](https://circleci.com/gh/pelletier197/kotlin-stream-csv.svg?style=shield)](https://app.circleci.com/pipelines/github/pelletier197/kotlin-stream-csv) [![Coverage Status](https://coveralls.io/repos/github/pelletier197/kotlin-stream-csv/badge.svg)](https://coveralls.io/github/pelletier197/kotlin-stream-csv)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.pelletier197/csv-core)


# Kotlin Stream CSV
> We are still working on this implementation for now, so it might not be stable yet.

A pure kotlin implementation of the CSV parser. This implementation uses the simplicity and the power of Kotlin, while remaining compatible with Java source code. It is completely stream driven to maximize performance and flexibility. 

## Yet another CSV library?
This project started after facing an issue with regular CSV parsers: they throw errors midway when there is an invalid input in the CSV. This can cause frustration when you're in the situation where you want to compute all errors in the CSV and return them to the client, or even just ignore invalid inputs.

This library uses rather a lazy error handling approach. This means that if the input is not parsable, it returns a result containing the errors for the input, and you can decide what to do with that error.
## Characteristics
1. Collect errors as it goes - you can customize how you handle each specific error, instead of throwing an exception on the first one
2. Easy to configure
3. Everything is immutable
4. Extremely lightweight. 
5. Kotlin :heart: 

## Known limitations
As for now, this implementation does not yet respect all specifications of [RFC-4180](https://tools.ietf.org/html/rfc4180). 
- Field containing line breaks is not yet supported
- Escaping a double quote is not yet supported 