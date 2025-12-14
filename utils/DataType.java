package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import scanner.Token;
import utils.Operation.Binary;
import utils.Operation.Unary;

public abstract class DataType {

    @SuppressWarnings("unchecked")
    public static final Class<Object[]> ARRAY_CLASS = (Class<Object[]>) new Object[]{}.getClass();

    private static HashMap<Class<?>, HashMap<Class<?>, TypeCast<?,?>>> TYPECASTS = new HashMap<>();
    private static HashMap<Token.Type, HashMap<Class<?>, HashMap<Class<?>, Binary<?,?,?>>>> BINARY_OPS = new HashMap<>();
    private static HashMap<Token.Type, HashMap<Class<?>, Unary<?,?>>> UNARY_OPS = new HashMap<>();

    public static void init() {

        defTypeCast(Boolean.class, Boolean.class, x -> x);
        defTypeCast(Boolean.class, Double.class, x -> x?1.:0.);
        defTypeCast(Boolean.class, String.class, x -> x?"true":"false");

        defTypeCast(Double.class, Boolean.class, x -> x!=0.);
        defTypeCast(Double.class, Double.class, x -> x);
        defTypeCast(Double.class, String.class, x -> ""+x);

        defTypeCast(String.class, Boolean.class, x -> x.length()>0);
        defTypeCast(String.class, Double.class, x -> (double)x.length());
        defTypeCast(String.class, String.class, x -> x);
        defTypeCast(String.class, ARRAY_CLASS, x -> x.split("(?!^)"));

        defTypeCast(NullObj.class, String.class, x -> x.toString());
        defTypeCast(ReturnValue.class, String.class, x -> x.toString());

        defTypeCast(ARRAY_CLASS, String.class, x -> Arrays.toString(x));

        // Array indexing
        defOp(Token.Type.IDX, ARRAY_CLASS, Double.class, Object.class, (x,y) -> x[(y>=0)? y.intValue() : x.length-y.intValue()]);
        defOp(Token.Type.IDX, String.class, Double.class, String.class, (x,y) -> ""+x.charAt((y>=0)? y.intValue() : x.length()-y.intValue()));

        // Logical operations
        defOp(Token.Type.NOT, Boolean.class, Boolean.class, x->!x);
        defSymmetricOp(Token.Type.AND, Boolean.class, Boolean.class, Boolean.class, (x,y) -> x&&y);
        defSymmetricOp(Token.Type.OR, Boolean.class, Boolean.class, Boolean.class, (x,y) -> x||y);
        defOp(Token.Type.LESS, Double.class, Double.class, Boolean.class, (x,y) -> x<y);
        defOp(Token.Type.LEQ, Double.class, Double.class, Boolean.class, (x,y) -> x<=y);
        defOp(Token.Type.GREATER, Double.class, Double.class, Boolean.class, (x,y) -> x>y);
        defOp(Token.Type.GEQ, Double.class, Double.class, Boolean.class, (x,y) -> x>=y);
        defSymmetricOp(Token.Type.EQ, Double.class, Double.class, Boolean.class, (x,y) -> x.doubleValue()==y.doubleValue());

        // Numeric, arithmetic operations
        defOp(Token.Type.MINUS, Double.class, Double.class, x -> -x);
        defSymmetricOp(Token.Type.PLUS, Double.class, Double.class, Double.class, (x,y) -> x+y);
        defOp(Token.Type.MINUS, Double.class, Double.class, Double.class, (x,y) -> x-y);
        defSymmetricOp(Token.Type.TIMES, Double.class, Double.class, Double.class, (x,y) -> x*y);
        defOp(Token.Type.DIV, Double.class, Double.class, Double.class, (x,y) -> x/y);
        defOp(Token.Type.POW, Double.class, Double.class, Double.class, (x,y) -> Math.pow(x,y));

        // String Number operations
        defOp(Token.Type.MINUS, String.class, String.class, x -> new StringBuilder(x).reverse().toString());
        defOp(Token.Type.MINUS, String.class, Double.class, String.class, (x,y) -> x.substring(0, x.length()-y.intValue()));
        defOp(Token.Type.PLUS, Double.class, String.class, String.class, (x,y) -> x+y);
        defOp(Token.Type.PLUS, String.class, Double.class, String.class, (x,y) -> x+y);
        defSymmetricOp(Token.Type.TIMES, String.class, Double.class, String.class, (x,y) -> Utils.repeatString(x, y));

        // String operations
        defOp(Token.Type.PLUS, String.class, String.class, String.class, (x,y) -> x+y);
        defOp(Token.Type.MINUS, String.class, String.class, String.class, (x,y) -> x.replaceAll(y, ""));
    
        // Apply operations to each element of array
        defSymmetricOp(Token.Type.PLUS, ARRAY_CLASS, Double.class, ARRAY_CLASS, (x,y) -> Arrays.stream(x)
        .map(i->DataType.apply2(Token.Type.PLUS, i, y)).toArray());
    }
    
    public static <A,B> void defTypeCast(Class<A> T1, Class<B> T2, Function<A, B> cast) {
        TYPECASTS.putIfAbsent(T1, new HashMap<>());
        TYPECASTS.get(T1).putIfAbsent(T2, new TypeCast<>(T1, T2, cast));
    }

    public static <A,B> void defOp(Token.Type type, Class<A> I, Class<B> O, Function<A, B> f) {
        UNARY_OPS.putIfAbsent(type, new HashMap<>());
        UNARY_OPS.get(type).putIfAbsent(I, new Unary<>(type, I, O, f));
    }

    public static <A,B,C> void defOp(Token.Type type, Class<A> I1, Class<B> I2, Class<C> O, BiFunction<A,B,C> f) {
        BINARY_OPS.putIfAbsent(type, new HashMap<>());
        BINARY_OPS.get(type).putIfAbsent(I1, new HashMap<>());
        BINARY_OPS.get(type).get(I1).putIfAbsent(I2, new Binary<>(type, I1, I2, O, f));
    }

    public static <A,B,C> void defSymmetricOp(Token.Type type, Class<A> I1, Class<B> I2, Class<C> O, BiFunction<A,B,C> f) {
        defOp(type, I1, I2, O, f);
        defOp(type, I2, I1, O, (x,y) -> f.apply(y,x));
    }

    /**
     * Transforms a given value into the specified class if a corresponding
     * typecast has been defined.
     * @param <A> Type of from
     * @param <B> Type of to
     * @param from Some Object
     * @param to Class to cast to
     * @return Castes Object
     */
    @SuppressWarnings("unchecked")
    public static <A,B> B cast(A from, Class<B> to) throws ClassCastException {
        if (from.getClass() == to) {return (B)from;}
        var tc1 = TYPECASTS.getOrDefault(from.getClass(), null);
        if (tc1 != null) {
            var tc = (TypeCast<A,B>)tc1.getOrDefault(to, null);
            if (tc != null) {
                return tc.apply(from);
            }
        }
        throw utils.TPLError.undefinedCastException(from.getClass(), to);
    }

    /**
     * Applies a given unary operator to an object and returns the result.
     * @param <A> Type of lhs
     * @param <B> Type of result
     * @param type Operator
     * @param v Object
     * @return result
     * @throws UnsupportedOperationException
     */
    public static <A,B> B apply1(Token.Type type, A v) throws UnsupportedOperationException {
        var t = UNARY_OPS.getOrDefault(type, null);
        if (t != null) {
            @SuppressWarnings("unchecked")
            var u = (Unary<A,B>)t.getOrDefault(v.getClass(), null);
            if (u != null) {
                return u.apply(v);
            }
        }
        throw utils.TPLError.undefinedUnaryException(type.name(), v.getClass());
    }

    /**
     * Applies a given binary operator to two object and returns the result.
     * @param <A> Type of lhs
     * @param <B> Type of rhs
     * @param <C> Type of result
     * @param type Operator
     * @param lhs Left hand side
     * @param rhs Right hand side
     * @return
     * @throws UnsupportedOperationException
     */
    @SuppressWarnings("unchecked")
    public static <A,B,C> C apply2(Token.Type type, A lhs, B rhs) throws UnsupportedOperationException {
        var t1 = BINARY_OPS.getOrDefault(type, null);
        if (t1 != null) {
            var t2 = BINARY_OPS.get(type).getOrDefault(lhs.getClass(), null);
            if (t2 != null) {
                var b = (Binary<A,B,C>)t2.getOrDefault(rhs.getClass(), null);
                if (b != null) {
                    return b.apply(lhs, rhs);
                }
            }
        }
        throw utils.TPLError.undefinedBinaryException(type.name(), lhs.getClass(), rhs.getClass());
    }
}
