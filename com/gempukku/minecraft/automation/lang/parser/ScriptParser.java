package com.gempukku.minecraft.automation.lang.parser;

import com.gempukku.minecraft.automation.lang.*;
import com.gempukku.minecraft.automation.lang.statement.*;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScriptParser {
    public ScriptExecutable parseScript(Reader reader) throws IllegalSyntaxException, IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        ScriptExecutable result = new ScriptExecutable();
        List<Term> terms = parseToTerms(bufferedReader);

//        for (Term term : terms)
//            System.out.println(term.getType() + "(" + term.getLine() + "):" + term.getValue());

        TermBlock termBlockStructure = constructBlocks(terms);

        System.out.println("Printing program structure");
        printTerms(0, termBlockStructure);

        List<ExecutableStatement> statements = seekStatementsInBlock(termBlockStructure);
        result.setStatement(new BlockStatement(statements, false, true));

        return result;
    }

    private List<ExecutableStatement> seekStatementsInBlock(TermBlock termBlock) throws IllegalSyntaxException {
        if (termBlock.isTerm()) {
            throw new IllegalStateException("Should not get here");
        } else {
            List<ExecutableStatement> result = new LinkedList<ExecutableStatement>();
            List<TermBlock> blocks = termBlock.getTermBlocks();
            PeekingIterator<TermBlock> termBlockIter = Iterators.peekingIterator(blocks.iterator());
            while (termBlockIter.hasNext()) {
                final ExecutableStatement resultStatement = produceStatementFromIterator(termBlockIter);
                result.add(resultStatement);
                if (resultStatement.requiresSemicolon())
                    consumeSemicolon(termBlockIter);
            }
            return result;
        }
    }

    private ExecutableStatement produceStatementFromIterator(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        TermBlock firstTermBlock = termIterator.peek();
        if (firstTermBlock.isTerm()) {
            Term firstTerm = firstTermBlock.getTerm();
            if (firstTerm.getType() == Term.Type.STRING) {
                // We do not allow at the moment any statements starting with constant
                throw new IllegalSyntaxException("Illegal start of statement");
            } else {
                // It's a program term
                String literal = getFirstLiteral(firstTerm.getValue());
                if (literal.equals("return")) {
                    return produceReturnStatement(termIterator);
                } else if (literal.equals("var")) {
                    return produceVarStatement(termIterator);
                } else if (literal.equals("function")) {
                    return produceDefineFunctionStatement(termIterator);
                } else if (literal.equals("if")) {
                    return produceIfStatement(termIterator);
                } else {
                    return produceValueReturningStatementFromIterator(termIterator);
                }
            }
        } else {
            return new BlockStatement(seekStatementsInBlock(firstTermBlock), true, false);
        }
    }

    private ExecutableStatement produceIfStatement(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        consumeCharactersFromTerm(termIterator, 2);

        if (!isNextTermStartingWith(termIterator, "("))
            throw new IllegalSyntaxException("( expected");
        consumeCharactersFromTerm(termIterator, 1);

        ExecutableStatement condition = produceValueReturningStatementFromIterator(termIterator);

        if (!isNextTermStartingWith(termIterator, ")"))
            throw new IllegalSyntaxException(") expected");
        consumeCharactersFromTerm(termIterator, 1);

        final TermBlock ifExecute = termIterator.peek();
        if (ifExecute.isTerm()) {
            final ExecutableStatement statement = produceStatementFromIterator(termIterator);
            consumeSemicolon(termIterator);
            return new IfStatement(condition, statement);
        } else {
            termIterator.next();
            final List<ExecutableStatement> statements = seekStatementsInBlock(ifExecute);
            return new IfStatement(condition, new BlockStatement(statements, true, false));
        }
    }

    private ExecutableStatement produceDefineFunctionStatement(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        consumeCharactersFromTerm(termIterator, 8);
        Term functionDefTerm = peekNextProgramTermSafely(termIterator);

        String functionName = getFirstLiteral(functionDefTerm.getValue());
        consumeCharactersFromTerm(termIterator, functionName.length());

        if (!isNextTermStartingWith(termIterator, "("))
            throw new IllegalSyntaxException("( expected");
        consumeCharactersFromTerm(termIterator, 1);

        List<String> parameterNames = new ArrayList<String>();
        while (!isNextTermStartingWith(termIterator, ")")) {
            String parameterName = getFirstLiteral(functionDefTerm.getValue());
            consumeCharactersFromTerm(termIterator, parameterName.length());
            parameterNames.add(parameterName);
            if (isNextTermStartingWith(termIterator, ","))
                consumeCharactersFromTerm(termIterator, 1);
        }
        consumeCharactersFromTerm(termIterator, 1);

        final TermBlock functionBodyBlock = termIterator.next();
        if (functionBodyBlock.isTerm())
            throw new IllegalSyntaxException("{ expected");

        final List<ExecutableStatement> functionBody = seekStatementsInBlock(functionBodyBlock);
        return new DefineFunctionStatement(functionName, parameterNames, functionBody);
    }

    private ExecutableStatement produceVarStatement(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        consumeCharactersFromTerm(termIterator, 3);
        final Term variableTerm = peekNextProgramTermSafely(termIterator);
        String variableName = getFirstLiteral(variableTerm.getValue());
        consumeCharactersFromTerm(termIterator, variableName.length());

        if (isNextTermStartingWithSemicolon(termIterator))
            return new DefineStatement(variableName);

        if (!isNextTermStartingWith(termIterator, "="))
            throw new IllegalSyntaxException("Expected =");

        consumeCharactersFromTerm(termIterator, 1);

        final ExecutableStatement value = produceValueReturningStatementFromIterator(termIterator);
        return new BlockStatement(Arrays.asList(new DefineStatement(variableName), new AssignStatement(new ConstantStatement(new Variable(variableName)), value)),
                false, false);
    }

    private ExecutableStatement produceReturnStatement(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        consumeCharactersFromTerm(termIterator, 6);
        if (isNextTermStartingWithSemicolon(termIterator))
            return new ReturnStatement(new ConstantStatement(new Variable(null)));
        return new ReturnStatement(produceValueReturningStatementFromIterator(termIterator));
    }

    private void consumeCharactersFromTerm(PeekingIterator<TermBlock> termIterator, int charCount) {
        final Term term = termIterator.peek().getTerm();
        String termText = term.getValue();
        String termRemainder = termText.substring(charCount).trim();
        if (termRemainder.length() > 0)
            term.setValue(termRemainder);
        else
            termIterator.next();
    }

    private void consumeSemicolon(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        Term term = peekNextProgramTermSafely(termIterator);
        String value = term.getValue();
        if (!value.startsWith(";"))
            throw new IllegalSyntaxException("; expected");
        consumeCharactersFromTerm(termIterator, 1);
    }

    private ExecutableStatement produceValueReturningStatementFromIterator(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        TermBlock termBlock = peekNextTermBlockSafely(termIterator);
        if (termBlock.isTerm()) {
            Term term = termBlock.getTerm();
            if (term.getType() == Term.Type.STRING) {
                String value = term.getValue();
                ExecutableStatement statement = new ConstantStatement(new Variable(value));
                // Consume the String
                termIterator.next();

                return withStatement(termIterator, statement);
            } else {
                // PROGRAM term
                ExecutableStatement statement;
                String termValue = term.getValue();
                if (Character.isDigit(termValue.charAt(0))) {
                    String numberInStr = getNumber(termValue);
                    consumeCharactersFromTerm(termIterator, numberInStr.length());
                    statement = new ConstantStatement(new Variable(Float.parseFloat(numberInStr)));
                } else {
                    String literal = getFirstLiteral(termValue);

                    consumeCharactersFromTerm(termIterator, literal.length());

                    if (literal.equals("true"))
                        statement = new ConstantStatement(new Variable(true));
                    else if (literal.equals("false"))
                        statement = new ConstantStatement(new Variable(false));
                    else if (literal.equals("null"))
                        statement = new ConstantStatement(new Variable(null));
                    else
                        statement = new VariableStatement(literal);
                }

                return withStatement(termIterator, statement);
            }
        } else {
            // TODO
            return null;
        }
    }

    private String getNumber(String termValue) {
        StringBuilder result = new StringBuilder();
        boolean hasDot = false;
        final char[] chars = termValue.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i]))
                result.append(chars[i]);
            else if (chars[i] == '.' && !hasDot) {
                hasDot = true;
                result.append('.');
            } else
                return result.toString();
        }
        return result.toString();
    }

    private ExecutableStatement withStatement(PeekingIterator<TermBlock> termIterator, ExecutableStatement statement) throws IllegalSyntaxException {
        final Term term = peekNextProgramTermSafely(termIterator);
        String termValue = term.getValue();
        final Operator operation = getArithmeticOperation(termValue);
        if (operation == null)
            return statement;
        if (operation == Operator.ASSIGNMENT) {
            consumeCharactersFromTerm(termIterator, 1);
            return new AssignStatement(statement, produceValueReturningStatementFromIterator(termIterator));
        } else if (operation == Operator.FUNCTION_CALL) {
            consumeCharactersFromTerm(termIterator, 1);

            List<ExecutableStatement> parameters = new ArrayList<ExecutableStatement>();
            while (!isNextTermStartingWith(termIterator, ")"))
                parameters.add(produceValueReturningStatementFromIterator(termIterator));
            consumeCharactersFromTerm(termIterator, 1);

            return new FunctionCallStatement(statement, parameters);
        }
        return statement;
    }

    private Operator getArithmeticOperation(String termValue) {
        if (termValue.startsWith("="))
            return Operator.ASSIGNMENT;
        else if (termValue.startsWith("("))
            return Operator.FUNCTION_CALL;
        return null;
    }

    private String getFirstLiteral(String text) throws IllegalSyntaxException {
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        if (!Character.isLetter(chars[0]))
            throw new IllegalSyntaxException("Expected expression");
        for (char c : chars) {
            if (Character.isLetterOrDigit(c))
                sb.append(c);
            else
                return sb.toString();
        }
        return sb.toString();
    }

    private Term peekNextProgramTermSafely(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        if (termIterator.hasNext()) {
            TermBlock termBlock = termIterator.peek();
            if (!termBlock.isTerm())
                throw new IllegalSyntaxException("Expression expected");

            Term term = termBlock.getTerm();
            if (term.getType() != Term.Type.PROGRAM)
                throw new IllegalSyntaxException("Expression expected");
            return term;
        } else
            throw new IllegalSyntaxException("Expression expected");
    }

    private TermBlock peekNextTermBlockSafely(PeekingIterator<TermBlock> termIterator) throws IllegalSyntaxException {
        if (termIterator.hasNext()) {
            return termIterator.peek();
        } else
            throw new IllegalSyntaxException("Expression expected");
    }

    private boolean isNextTermStartingWithSemicolon(PeekingIterator<TermBlock> termIterator) {
        return isNextTermStartingWith(termIterator, ";");
    }

    private boolean isNextTermStartingWith(PeekingIterator<TermBlock> termIterator, String text) {
        if (termIterator.hasNext()) {
            final TermBlock termBlock = termIterator.peek();
            if (termBlock.isTerm()) {
                final Term term = termBlock.getTerm();
                if (term.getType() == Term.Type.PROGRAM) {
                    return term.getValue().startsWith(text);
                }
            }
        }
        return false;
    }

    private void printTerms(int indent, TermBlock block) {
        if (block.isTerm()) {
            for (int i = 0; i < indent; i++)
                System.out.print("  ");
            Term term = block.getTerm();
            System.out.println(term.getType() + "(" + term.getLine() + "):" + term.getValue());
        } else {
            List<TermBlock> childBlocks = block.getTermBlocks();
            for (TermBlock childBlock : childBlocks) {
                printTerms(indent + 1, childBlock);
            }
        }
    }

    private TermBlock constructBlocks(List<Term> terms) throws IllegalSyntaxException {
        LinkedList<TermBlock> termBlocksStack = new LinkedList<TermBlock>();

        TermBlock result = new TermBlock();
        TermBlock currentBlock = result;

        for (Term term : terms) {
            if (term.getType() == Term.Type.PROGRAM) {
                String value = term.getValue().trim();

                while (value.length() > 0) {
                    int open = value.indexOf('{');
                    int close = value.indexOf('}');
                    if (open > -1 && (close < 0 || open < close)) {
                        String before = value.substring(0, open);
                        String after = value.substring(open + 1);
                        if (before.length() > 0)
                            appendProgramTerm(currentBlock, before.trim(), term.getLine());
                        termBlocksStack.add(currentBlock);
                        TermBlock childBlock = new TermBlock();
                        currentBlock.addTermBlock(childBlock);
                        currentBlock = childBlock;
                        value = after;
                    } else if (close > -1 && (open < 0 || close < open)) {
                        String before = value.substring(0, close);
                        String after = value.substring(close + 1);
                        if (before.length() > 0)
                            appendProgramTerm(currentBlock, before.trim(), term.getLine());
                        if (termBlocksStack.size() == 0)
                            throw new IllegalSyntaxException("Found closing bracket for no block");
                        currentBlock = termBlocksStack.removeLast();
                        value = after;
                    } else {
                        appendProgramTerm(currentBlock, value.trim(), term.getLine());
                        value = "";
                    }
                }
            } else if (term.getType() == Term.Type.STRING) {
                currentBlock.addTermBlock(term);
            }
        }

        if (termBlocksStack.size() > 0)
            throw new IllegalStateException("Unclosed bracket - }");

        return result;
    }

    private void appendProgramTerm(TermBlock currentBlock, String text, int line) {
        currentBlock.addTermBlock(new Term(Term.Type.PROGRAM, text, line));
    }

    private List<Term> parseToTerms(BufferedReader bufferedReader) throws IOException, IllegalSyntaxException {
        int lineNumber = 1;
        List<Term> terms = new ArrayList<Term>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            termAndValidateLine(line, lineNumber, terms);
            lineNumber++;
        }
        return terms;
    }

    private void termAndValidateLine(String line, int lineNumber, List<Term> resultTerms) throws IllegalSyntaxException {
        // Remove all not needed white-space characters
        line = line.trim();
        Term.Type type = Term.Type.PROGRAM;
        StringBuilder valueSoFar = new StringBuilder();
        char[] lineChars = line.toCharArray();
        for (int i = 0; i < lineChars.length; i++) {
            if (type == Term.Type.PROGRAM) {
                if (lineChars[i] == '\"') {
                    if (valueSoFar.length() > 0)
                        resultTerms.add(new Term(type, valueSoFar.toString().trim(), lineNumber));
                    type = Term.Type.STRING;
                    valueSoFar = new StringBuilder();
                } else if (lineChars[i] == '/' && i + 1 < lineChars.length && lineChars[i + 1] == '/') {
                    if (valueSoFar.length() > 0)
                        resultTerms.add(new Term(type, valueSoFar.toString().trim(), lineNumber));
                    type = Term.Type.COMMENT;
                    valueSoFar = new StringBuilder();
                } else {
                    valueSoFar.append(lineChars[i]);
                }
            } else if (type == Term.Type.STRING) {
                if (lineChars[i] == '\"') {
                    resultTerms.add(new Term(type, valueSoFar.toString(), lineNumber));
                    type = Term.Type.PROGRAM;
                    valueSoFar = new StringBuilder();
                } else if (lineChars[i] == '\\') {
                    i++;
                    if (i < lineChars.length) {
                        if (lineChars[i] == '\"' || lineChars[i] == '\\')
                            valueSoFar.append(lineChars[i]);
                        else
                            throw new IllegalSyntaxException("Illegal escape sequence in String \\" + lineChars[i]);
                    } else {
                        throw new IllegalSyntaxException("Unfinished escape sequence in String");
                    }
                } else {
                    valueSoFar.append(lineChars[i]);
                }
            } else {
                valueSoFar.append(lineChars[i]);
            }
        }

        if (valueSoFar.length() > 0)
            resultTerms.add(new Term(type, (type == Term.Type.PROGRAM) ? valueSoFar.toString().trim() : valueSoFar.toString(), lineNumber));
    }
}
