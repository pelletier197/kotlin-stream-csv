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
Probably the most useful implementation of all three of CSV parser for most use-cases. All examples under are based on the followed CSV:

```csv
first_name, last_name, phone_number, emails
John, Doe, 1+342-534-2342, "john.doe.1@test.com, john.doe.2@test.com"
Alice, Doe, 1+423-253-3453, alice.doe@test.com 
```
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
| NO_CONVERTER_FOUND_FOR_VALUE | When trying to convert a value to a field class that has no converter. You should register a custom converter to support this field |
| CONVERSION_OF_FIELD_FAILED   | When trying to convert a field and the converter throws an exception.                                                         |

#### Custom converters
When you want to map the CSV field to a custom object of yours, it is possible to do so by registering a custom converter for this field.

```kotlin
class EmailConverter : Converter<String, Email> {
    override val source: Class<String> get() = String::class.java
    override val target: Class<Email> get() = Email::class.java

    override fun convert(value: String, to: Type, parameters: ConversionParameters): Email {
        return Email(value = value)
    }
}

val reader = CsvReader()
    .readerForType<CustomCsvPerson>()
    .withConverter(EmailConverter())

val people = reader.read(csv).map { it.getResultOrThrow() }.toList()
// Output:
// CustomCsvPerson(firstName=John, lastName= Doe, emails=[Email(value=john.doe.1@test.com), Email(value= john.doe.2@test.com)])
// CustomCsvPerson(firstName=Alice, lastName= Doe, emails=[Email(value= alice.doe@test.com )])
```

### Header CSV Parser
This kind of CSV parser can also be useful if you don't know exactly the input format of the CSV, or for other sorts of reason. This parser uses the first non-empty line of the CSV as the header if you don't provide one programmatically.

#### Basic usage
```kotlin
    val reader = CsvReader()
        .readerWithHeader()
        .withHeader("first_name", "last_name", "phone_number", "emails") // If you wish to provide the header yourself
    val people = reader.read(csv).map { it }.toList()

    println(people.joinToString(separator = "\n"))
    // Output:
    // HeaderCsvLine(values={first_name=John, last_name= Doe, phone_number= 1+342-534-2342, emails=john.doe.1@test.com, john.doe.2@test.com}, line=2)
    // HeaderCsvLine(values={first_name=Alice, last_name= Doe, phone_number= 1+423-253-3453, emails= alice.doe@test.com }, line=3)
```

### Raw CSV parser
This last one is the low level parser that returns the every raw line in the CSV as it comes.

#### Basic usage
```kotlin
    val reader = CsvReader().rawReader()
    val people = reader.read(csv).map { it }.toList()

    println(people.joinToString(separator = "\n"))
    // Output:
    // RawCsvLine(columns=[first_name,  last_name,  phone_number,  emails], line=1)
    // RawCsvLine(columns=[John,  Doe,  1+342-534-2342, john.doe.1@test.com, john.doe.2@test.com], line=2)
    // RawCsvLine(columns=[Alice,  Doe,  1+423-253-3453,  alice.doe@test.com ], line=3)
```

### Configuration
Configuration is extremely simple and versatile. Every configuration change will create a new immutable parser to avoid side effects. Here are the available configuration for the different parsers that are available through an explicit method name on the parser.

| Configuration         | Definition                                                                                    | Default | Typed | Header | Raw |
|-----------------------|-----------------------------------------------------------------------------------------------|---------|-------|--------|-----|
| Separator             | The separator to use for the columns. For now, a single character can be used as a separator. |   ','   |   :heavy_check_mark:   |    :heavy_check_mark:   |  :heavy_check_mark:  |
| Delimiter             | The quoted column delimiter, when you want to use the separator inside a column.              |   '"'   |   :heavy_check_mark:   |    :heavy_check_mark:   |  :heavy_check_mark:  |
| Trim entries          | Either to trim entries or not when parsing this input.                                        |  false  |   :heavy_check_mark:   |    :heavy_check_mark:   |  :heavy_check_mark:  |
| Skip empty lines      | Either to skip the empty lines or not.                                                        |   true  |   :heavy_check_mark:   |    :heavy_check_mark:   |  :heavy_check_mark:  |
| Empty strings as null | Either to treat empty strings as null when parsing the columns.                               |  false  |   :heavy_check_mark:   |    :heavy_check_mark:   |  :heavy_check_mark:  |
| Header                | Allows settings the header of the parser. When not configured, first non-empty line is used.  |   null  |   :heavy_check_mark:   |    :heavy_check_mark:   |     |
| List separator        | The character to use when converting a string to a collection (list, set)                     |   ','   |   :heavy_check_mark:   |        |     |


## Known limitations
As for now, this implementation does not yet respect all specifications of [RFC-4180](https://tools.ietf.org/html/rfc4180). 
- Field containing line breaks is not yet supported
- Escaping a double quote is not yet supported 
