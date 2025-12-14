package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import scanner.Lexer;
import scanner.Token;
import utils.Utils;

public class ScannerTests {

    @Test
    public void testScanner0() throws Exception {
        String raw = Utils.loadSPL("spl/expressions.spl");
        Lexer scanner = new Lexer(raw);
        ArrayList<Token> tokens = scanner.tokenize();
        int n = tokens.size();

        assertEquals(Token.Type.VAR, tokens.get(0).TYPE);
        assertEquals(Token.Type.EOF, tokens.get(n-1).TYPE);

        assertEquals(59, n);

        System.out.println(tokens.toString());
    }

    @Test
    public void testScannerSimple0() throws Exception {
        Lexer scanner = new Lexer("var b = true;");
        ArrayList<Token> tokens = scanner.tokenize();
        int n = tokens.size();
        assertEquals(6, n);
        assertEquals(Token.Type.VAR, tokens.get(0).TYPE);
        assertEquals(Token.Type.ID, tokens.get(1).TYPE);
        assertEquals(Token.Type.ASSIGN, tokens.get(2).TYPE);
        assertEquals(Token.Type.TRUE, tokens.get(3).TYPE);
        assertEquals(Token.Type.SEMICOLON, tokens.get(4).TYPE);
        assertEquals(Token.Type.EOF, tokens.get(5).TYPE);
    }
}
