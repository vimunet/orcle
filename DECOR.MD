To throw an exception when a byte array contains invalid characters that don't align with the expected charset, you can use `CharsetDecoder`. The `CharsetDecoder` class provides a way to attempt decoding the byte array, and if the byte sequence includes invalid characters (based on the specified charset), it will throw a `CharacterCodingException`. This lets you handle invalid characters directly through exception handling.

Here's how to do it:

```java
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class ByteArrayValidationExample {

    public static void main(String[] args) {
        byte[] byteArray = {72, 101, 108, 108, 111}; // Example byte array representing "Hello" in UTF-8

        try {
            ensureAllBytesAreCharacters(byteArray, StandardCharsets.UTF_8);
            System.out.println("The byte array is valid and contains only characters.");
        } catch (CharacterCodingException e) {
            System.out.println("Invalid byte sequence detected: " + e.getMessage());
        }
    }

    // Method to check if all bytes in the array are valid characters in the specified charset
    public static void ensureAllBytesAreCharacters(byte[] byteArray, Charset charset) throws CharacterCodingException {
        // Create a CharsetDecoder to check the validity of characters
        CharsetDecoder decoder = charset.newDecoder();

        // Set the decoder to throw an exception on an invalid character sequence
        decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);

        // Attempt to decode; an exception is thrown if there's an invalid character
        decoder.decode(ByteBuffer.wrap(byteArray));
    }
}
```

### Explanation

1. **CharsetDecoder Setup**:
   - `CharsetDecoder` is initialized using the specified `Charset`, such as `UTF-8` or `ISO-8859-1`.
   - `onMalformedInput(CodingErrorAction.REPORT)` and `onUnmappableCharacter(CodingErrorAction.REPORT)` configure the decoder to throw an exception (`CharacterCodingException`) if there’s any invalid or unmappable character in the byte array.

2. **Exception Handling**:
   - If `decoder.decode()` encounters any invalid sequences, it throws `CharacterCodingException`, which you can handle to log, notify, or propagate further in your application.

3. **Usage**:
   - When calling `ensureAllBytesAreCharacters`, if the byte array contains valid characters for the given charset, the method completes successfully. Otherwise, a `CharacterCodingException` is thrown, allowing you to handle invalid data immediately.

This approach ensures that only byte arrays with valid characters pass through, while invalid sequences are caught and handled.
