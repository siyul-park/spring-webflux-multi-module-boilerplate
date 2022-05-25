package io.github.siyual_park.presentation.filter

// class R2dbcRHSFilterParserTest : CoroutineTestHelper() {
//     private data class TestCase(
//         val query: Map<KProperty1<Person, *>, Collection<String?>>,
//         val sql: String? = null,
//         val exception: KClass<out Exception>? = null
//     )
//
//     private val objectMapper = jacksonObjectMapper()
//     private val r2dbcRHSFilterParser = R2dbcRHSFilterParser(Person::class, objectMapper)
//
//     @Test
//     fun parse() {
//         val testCases = listOf(
//             TestCase(
//                 query = mapOf(
//                     Person::name to listOf("ne:test"),
//                 ),
//                 sql = "(name != 'test')"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::name to listOf("eq:test"),
//                 ),
//                 sql = "(name = 'test')"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::name to listOf("lk:test"),
//                 ),
//                 sql = "(name LIKE 'test')"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::age to listOf("gt:0")
//                 ),
//                 sql = "(age > 0)"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::age to listOf("gte:0")
//                 ),
//                 sql = "(age >= 0)"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::age to listOf("lt:0")
//                 ),
//                 sql = "(age < 0)"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::age to listOf("lte:0")
//                 ),
//                 sql = "(age <= 0)"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::name to listOf("eq:test"),
//                     Person::age to listOf("gte:0")
//                 ),
//                 sql = "(name = 'test') AND (age >= 0)"
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::age to listOf("not_found:0")
//                 ),
//                 exception = FilterInvalidException::class
//             ),
//             TestCase(
//                 query = mapOf(
//                     Person::age to listOf("eq:invalid")
//                 ),
//                 exception = FilterInvalidException::class
//             ),
//         )
//
//         testCases.forEach {
//             if (it.sql != null) {
//                 val criteria = r2dbcRHSFilterParser.parse(it.query)
//                 assertEquals(it.sql, criteria.toString())
//             }
//             if (it.exception != null) {
//                 Assertions.assertThrows(it.exception.java) { r2dbcRHSFilterParser.parse(it.query) }
//             }
//         }
//     }
// }
