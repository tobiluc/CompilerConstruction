package utils;

public class TPLError {

    public static Exception unexpectedTokenException(char symbol, int line) {
        return new Exception(String.format("Line %d: Unexpected Token \"%s\"", line, symbol));
    }

    public static Exception undeterminatedStringException(String str, int line) {
        return new Exception(String.format("Line %d: Undterminated String \"%s...", line, str));
    }
    
    public static IllegalArgumentException invalidNargsException(String funcName, int expected, int actual) {
        return new IllegalArgumentException(String.format("Invalid number of arguments for function %s, expected %d but got %d", funcName, expected, actual));
    }

    public static IllegalArgumentException invalidArgTypeException(String funcName, Class<?> expected, Class<?> actual) {
        return new IllegalArgumentException(String.format("Invalid argument type for function %s, expected %s but got %s", funcName, expected.getSimpleName(), actual.getSimpleName()));
    }

    public static UnsupportedOperationException undefinedBinaryException(String opName, Class<?> l, Class<?> r) {
        return new UnsupportedOperationException(String.format("Binary operation %s is undefined for types %s and %s", opName, l.getSimpleName(), r.getSimpleName()));
    }

    public static UnsupportedOperationException undefinedUnaryException(String opName, Class<?> t) {
        return new UnsupportedOperationException(String.format("Unary operation %s is undefined for type %s", opName, t.getSimpleName()));
    }

    public static ClassCastException undefinedCastException(Class<?> from, Class<?> to) {
        return new ClassCastException(String.format("No cast defined from %s to %s", from.getSimpleName(), to.getSimpleName()));
    }
}
