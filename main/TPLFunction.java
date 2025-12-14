package main;

import tree.Statement;

public class TPLFunction implements ITPLFunction {

    public final String NAME;
    public final String[] ARGSNAMES;
    private Statement seq;
    
    public TPLFunction(String name, String[] argsNames, Statement seq) {
        this.NAME = name;
        this.ARGSNAMES = argsNames;
        this.seq = seq;
    }

    @Override
    public Object eval(Object[] args) throws Exception {
        return eval(seq.PROGRAM.enterScope(false), args);
    }

    @Override
    public Object eval(TPLScope scope, Object[] args) throws Exception {
        if (ARGSNAMES.length != args.length) {
            throw utils.TPLError.invalidNargsException(NAME, ARGSNAMES.length, args.length);
        }
        int nargs = args.length;
        var program = seq.PROGRAM;

        assert(scope.isRootScope());

        // Set argument values
        for (int i = 0; i < nargs; ++i) {
            scope.declVar(ARGSNAMES[i], args[i]);
        }

        // Evaluate function
        var res = seq.eval();

        // Reset scope
        program.leaveScope();

        return res;
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", NAME, ARGSNAMES.length);
    }
}
