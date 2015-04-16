package com.gempukku.lang;

import com.gempukku.lang.parser.LastPeekingIterator;
import com.gempukku.lang.parser.Term;
import com.gempukku.lang.parser.TermBlock;

public class IllegalSyntaxException extends Exception {
    private int _line;
    private int _column;

    public IllegalSyntaxException(LastPeekingIterator<TermBlock> termIterator, String message) {
        this(getLine(termIterator), getColumn(termIterator), message);
    }

    private static int getLine(LastPeekingIterator<TermBlock> termIterator) {
        if (termIterator.hasNext()) {
            final TermBlock termBlock = termIterator.peek();
            if (termBlock.isTerm()) {
                final Term term = termBlock.getTerm();
                return term.getLine();
            } else {
                return termBlock.getBlockStartLine();
            }
        } else {
            final TermBlock lastTermBlock = termIterator.getLast();
            if (lastTermBlock.isTerm()) {
                final Term lastTerm = lastTermBlock.getTerm();
                return lastTerm.getLine();
            } else {
                return lastTermBlock.getBlockEndLine();
            }
        }
    }

    private static int getColumn(LastPeekingIterator<TermBlock> termIterator) {
        if (termIterator.hasNext()) {
            final TermBlock termBlock = termIterator.peek();
            if (termBlock.isTerm()) {
                final Term term = termBlock.getTerm();
                return term.getColumn();
            } else {
                return termBlock.getBlockStartColumn();
            }
        } else {
            final TermBlock lastTermBlock = termIterator.getLast();
            if (lastTermBlock.isTerm()) {
                final Term lastTerm = lastTermBlock.getTerm();
                return lastTerm.getColumn() + lastTerm.getValue().length();
            } else {
                return lastTermBlock.getBlockEndColumn();
            }
        }
    }

    public IllegalSyntaxException(Term term, String message) {
        this(term.getLine(), term.getColumn(), message);
    }

    public IllegalSyntaxException(int line, int column, String message) {
        super("line: " + line + ", column: " + column + ", " + message);
        _line = line;
        _column = column;
    }

    public int getColumn() {
        return _column;
    }

    public int getLine() {
        return _line;
    }
}
