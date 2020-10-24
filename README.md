[![CircleCI](https://circleci.com/gh/pelletier197/kotlin-stream-csv.svg?style=shield)](https://app.circleci.com/pipelines/github/pelletier197/kotlin-stream-csv) [![Coverage Status](https://coveralls.io/repos/github/pelletier197/kotlin-stream-csv/badge.svg)](https://coveralls.io/github/pelletier197/kotlin-stream-csv)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.pelletier197/csv-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.pelletier197/csv-core)


# Kotlin Stream CSV
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

## Usage 
Three types of parsers are available:
- Typed CSV parser will read your CSV file directly into a data class
- Header CSV parser will return a `Map<String, String>` for each row, where the map's key is the header's value
- Raw CSV reader will return a `List<String>` for every line of the CSV

For advanced configuration examples of all three types of CSV, see [example project](./examples/src/main/kotlin/io/github/pelletier197/csv)

### Typed CSV parser
Probably the most useful implementation of all three of CSV parser for most use-cases. 

#### Basic usage
```kotlin
    data class CsvPerson(
        // Csv property allows to specify what is the header name in the CSV, while naming you class field how you wish
        @CsvProperty("first_name")
        val firstName: String,
        @CsvProperty("last_name")
        val lastName: String,
        @CsvProperty("phone_number")
        val phoneNumber: String,
        val emails: Set<String>
    )

    val reader = CsvReader()
        .readerForType<CsvPerson>()
    val people = reader.read(csv).map { it.getResultOrThrow() }.toList()

    println(people.joinToString(separator = "\n"))
    // Output:
    // CsvPerson(firstName=John, lastName= Doe, phoneNumber= 1+342-534-2342, emails=[john.doe.1@test.com, john.doe.2@test.com])
    // CsvPerson(firstName=Alice, lastName= Doe, phoneNumber= 1+423-253-3453, emails=[ alice.doe@test.com ])
```

#### Error handling
It is fairly simple to handle errors of a CSV input

```kotlin
    // Missing emails field
    val invalidCsv =
        """
            first_name, last_name, phone_number, emails 
            John, Doe, 1+342-534-2342
        """.trimIndent()

    val reader = CsvReader()
        .readerForType<CsvPerson>()
        .withEmptyStringsAsNull(true)

    reader.read(invalidCsv).forEach { println(it) }
    // Output:
    // TypedCsvLine(result=null, line=2, errors=[CsvError(csvField=emails, classField=emails, providedValue=null, type=NON_NULLABLE_FIELD_IS_NULL, cause=null)])
```

##### Output fields description
| Field                  | Description                                                                                                     |
|------------------------|-----------------------------------------------------------------------------------------------------------------|
| result                 | always non-null if there are no errors. It means the line was parsed successfully                               |
| errors[].csvField      | The field in the CSV that is missing                                                                            |
| errors[].classField    | The field in the recipient class that is missing. Will differ from `csvField` if `@CsvProperty` is used.        |
| errors[].providedValue | The value provided in the CSV that caused this error. Will be null if the error is `NON_NULLABLE_FIELD_IS_NULL` |
| errors[].type          | The error type. See [error type descriptions](#error-type-descriptions)                                                               |
| errors[].cause         | The root exception that caused the error, if there is one                                                       |

##### Error type descriptions
| Error type                   | Description                                                                                                                   |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| NON_NULLABLE_FIELD_IS_NULL   | In a Kotlin data class, this occurs whem target field is of non-nullable type but provided value is null                      |
| NO_CONVERTER_FOUND_FOR_VALUE | When trying to convert a value to a field that has no converter. You should register a custom converter to support this field |
| CONVERSION_OF_FIELD_FAILED   | When trying to convert a field and the converter throws an exception.                                                         |

## Known limitations
As for now, this implementation does not yet respect all specifications of [RFC-4180](https://tools.ietf.org/html/rfc4180). 
- Field containing line breaks is not yet supported
- Escaping a double quote is not yet supported 
