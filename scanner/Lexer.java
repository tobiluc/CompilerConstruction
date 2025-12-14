package scanner;

import java.util.ArrayList;
import java.util.Map;

import utils.TPLError;

import static java.util.Map.entry;  

/**
 * Scanner responsible for lexical analysis to transform a raw input text into a stream of tokens.
 */
public class Lexer {
    
    private static final Map<String, Token.Type> KEYWORDS = Map.ofEntries(
        entry("true", Token.Type.TRUE),
        entry("false", Token.Type.FALSE),
        entry("var", Token.Type.VAR),
        entry("print", Token.Type.PRINT),
        entry("return", Token.Type.RETURN),
        entry("if", Token.Type.IF),
        entry("else", Token.Type.ELSE),
        entry("while", Token.Type.WHILE),
        entry("for", Token.Type.FOR),
        entry("func", Token.Type.FUNC),
        entry("class", Token.Type.CLASS),
        entry("or", Token.Type.OR),
        entry("and", Token.Type.AND)
    );

    private static final Map<Character, Token.Type> CHARS = Map.ofEntries(
        entry('{', Token.Type.LBRACE),
        entry('}', Token.Type.RBRACE),
        entry('(', Token.Type.LPAREN),
        entry(')', Token.Type.RPAREN),
        entry('[', Token.Type.LBOXBRACKET),
        entry(']', Token.Type.RBOXBRACKET),
        entry('+', Token.Type.PLUS),
        entry('-', Token.Type.MINUS),
        entry('*', Token.Type.TIMES),
        entry('^', Token.Type.POW),
        entry(';', Token.Type.SEMICOLON),
        entry(',', Token.Type.COMMA),
        entry(':', Token.Type.COLON)
    );

    private String input;
    private ArrayList<Token> tokens;
    private int line;
    private int index;

    public Lexer() {
        this("");
    }

    public Lexer(String text) {
        reset(text);
    }

    public Lexer reset(String text) {
        this.input = text;
        this.tokens = new ArrayList<>();
        this.line = 0;
        this.index = 0;
        return this;
    }
    
    public ArrayList<Token> tokenize() throws Exception {

        // get all tokens
        while (!isAtEnd()) {
            int start = index;
            char c = advance();

            // Whitespace
            if (c == '\n') {line++; continue;}
            if (isBlank(c)) continue;

            // Single Character Symbols
            if (CHARS.containsKey(c)) {
                addToken(CHARS.get(c), c, null);
                continue;
            }

            // Ambigouities
            switch (c) {
                case '<': if (match('=')) {addToken(Token.Type.LEQ, "<=", null);} else {addToken(Token.Type.LESS, '<', null);} continue;
                case '>': if (match('=')) {addToken(Token.Type.GEQ, ">=", null);} else {addToken(Token.Type.GREATER, '>', null);} continue;
                case '=': if (match('=')) {addToken(Token.Type.EQ, "==", null);} else {addToken(Token.Type.ASSIGN, '=', null);} continue;
                case '!': if (match('=')) {addToken(Token.Type.NEQ, "!=", null);} else {addToken(Token.Type.NOT, '!', null);} continue;
                
                case '/': if (match('/')) {while(peek()!='\n'&&!isAtEnd()) advance();} else {addToken(Token.Type.DIV, '/', null);} continue;

                default: break;
            }

            // Strings
            if (c == '\"') {scanString(start); continue;}

            // Keywords or Identifiers
            if (isAlpha(c)) {scanAlphaNumeric(start); continue;}

            // Numbers (Ints and Decimals)
            if (isDigit(c)) {scanNumber(start); continue;}

            throw utils.TPLError.unexpectedTokenException(c, line);
        }

        addToken(Token.Type.EOF, '\0', null);
		return tokens;
    }

    private void scanString(int start) throws Exception {
        assert(input.charAt(start) == '\"');
        int startLine = line;
        while (peek() != '\"' && !isAtEnd()) {advance();}
        if (isAtEnd()) {
            throw utils.TPLError.undeterminatedStringException(input.substring(start+1), startLine);
        }
        advance();
        String str = input.substring(start+1, index-1);
        addToken(Token.Type.STR, str, str);
    }

    private void scanAlphaNumeric(int start) {
        assert(isAlpha(input.charAt(start)));
        while (isAlpha(peek()) || isDigit(peek())) {advance();}
        String word = input.substring(start, index);
        Token.Type type = KEYWORDS.getOrDefault(word, Token.Type.ID);

        if (type != Token.Type.ID) {
            // keyword
            addToken(type, word, null);
        } else {
            // identifier
            addToken(type, word, null);
        }
    }

    private void scanNumber(int start) {
        assert(isDigit(input.charAt(start)));
        while (isDigit(peek())) {advance();}
        String s;
        if (match('.')) {
            while (isDigit(peek())) {advance();}
            s = input.substring(start, index);
            addToken(Token.Type.NUM, s, Double.parseDouble(s));
            return;
        }
        s = input.substring(start, index);
        addToken(Token.Type.NUM, s, (double)Integer.parseInt(s));
    }

    private boolean isAlpha(char c) {
        return (c>='a'&&c<='z')||(c>='A'&&c<='Z');
    }

    private boolean isDigit(char c) {
        return (c>='0'&&c<='9');
    }

    private boolean isBlank(char c) {
        return Character.isWhitespace(c);
    }

    private char advance() {
        if (isAtEnd()) return '\0';
        return input.charAt(index++);
    }

    private boolean match(char expected) {
        if (isAtEnd() || peek() != expected) return false;
        index++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(index);
    }

    private boolean isAtEnd() {
        return index >= input.length();
    }

    private void addToken(Token.Type type, char lexeme, Object literal) {
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private void addToken(Token.Type type, String lexeme, Object literal) {
        tokens.add(new Token(type, lexeme, literal, line));
    }
}
