# Kotlin CSV
> We are still working on this implementation for now, so it might not be stable yet.

A pure kotlin implementation of the CSV parser. This implementation uses the simplicity and the power of Kotlin, while remaining compatible with Java source code.

## Characteristics
1. Collect errors as it goes - you can customize how you handle each specific error, instead of throwing an exception on the first error.
2. Easy to configure
3. Kotlin :heart: - We strongly recommend the switch :wink:

## Known limitations
As for now, this implementation does not yet respect all specifications of [RFC-4180](https://tools.ietf.org/html/rfc4180). 
- Field containing line breaks is not yet supported
