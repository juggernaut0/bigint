#BigInt

Kotlin Multiplatform implementation of arbitrary-precision 
integers. For use in cross-platform application without need of 
`expect`/`actual` declarations.

##Maven Dependency

Coming Soon

##Usage

Extension functions are provided for easy conversion.

```kotlin
// From Int
1234567.toBigInteger()
// From Long
123456789012345678L.toBigInteger()
// From String
"123456789012345678901234567890".toBigInteger()
```

##TODOs

 * Improve performance, especially of `String.toBigInteger()`
 * Support pow, exp, log, and other operations
