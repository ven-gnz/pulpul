package pulp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;



    public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>
    {
        private final Interpreter interpreter;
        private final Stack<Map<String, Boolean>> scopes = new Stack<>();
        private FunctionType currentFunction = FunctionType.NONE;
        private ClassType currentClass = ClassType.NONE;
        private int loopDepth = 0;
        public final Map<String, Symbol> symbols = new HashMap<>();

        Resolver(Interpreter inte)
        {
            this.interpreter = inte;
        }

        private enum FunctionType {
            NONE,
            FUNCTION,
            INITIALIZER,
            METHOD
        }

        private enum ClassType
        {
            NONE, CLASS
        }

        @Override
        public Void visitBlockStmt(Stmt.Block stmt)
        {
            beginScope();
            resolve(stmt.statements);
            endScope();
            return null;
        }

        @Override
        public Void visitProgramStmt(Stmt.Program stmt) {

            ClassType enclosingClass = currentClass;
            currentClass = ClassType.CLASS;

            declare(stmt.name);
            define(stmt.name);

            beginScope();
            scopes.peek().put("this", true);

            for(Stmt.Subprogram subprogram : stmt.methods)
            {
                FunctionType declaration = FunctionType.METHOD;
                if (subprogram.name.lexeme.equals("init")) {
                    declaration = FunctionType.INITIALIZER;
                }
                resolveFunction(subprogram, declaration);
            }

            for(Stmt stmt1 : stmt.statements)
            {
                if(stmt1 instanceof Stmt.Var)
                {
                    Stmt.Var v = (Stmt.Var) stmt1;
                    visitVarStmt(v);
                }

            }
            endScope();
            currentClass = enclosingClass;
            return null;
        }

        void resolve(List<Stmt> statements)
        {
            for(Stmt statement : statements)
            {
                resolve(statement);
            }
        }

        private void resolve(Stmt stmt)
        {
            stmt.accept(this);
        }

        private void resolve(Expr expr){
            expr.accept(this);
        }

        private void beginScope()
        {
            scopes.push(new HashMap<String, Boolean>());
        }

        private void endScope()
        {
            scopes.pop();
        }

        @Override
        public Void visitVarStmt(Stmt.Var stmt)
        {

            declare(stmt.name);
            if(stmt.initializer != null)
            {
                resolve(stmt.initializer);
            }
            define(stmt.name);
            symbols.put(stmt.name.lexeme,
                    new Symbol(stmt.name.lexeme, stmt.declaredType, null, true));
            return null;
        }

        private void declare(Token name)
        {
            if(scopes.isEmpty()) return;
            Map<String, Boolean> scope = scopes.peek();
            if(scope.containsKey(name.lexeme))
            {
                Pulper.error(name, " :variable with this name already initialized in this scope! ");
            }
            scope.put(name.lexeme, false);
        }

        private void define(Token name)
        {
            if(scopes.isEmpty()) return;
            scopes.peek().put(name.lexeme, true);
        }

        @Override
        public Void visitVariableExpr(Expr.Variable expr)
        {
            if(!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE)
            {
                Pulper.error(expr.name, "Cannot read local variable until it is initialized");
            }
            resolveLocal(expr, expr.name);
            return null;
        }

        private void resolveLocal(Expr expr, Token name)
        {
            for(int i = scopes.size() - 1; i >= 0; i--)
            {
                if(scopes.get(i).containsKey(name.lexeme))
                {
                    interpreter.resolve(expr, scopes.size() - 1 - i);
                }
            }
        }

        @Override
        public Void visitAssignExpr(Expr.Assign expr)
        {
            resolve(expr.value);
            resolveLocal(expr, expr.name);
            return null;
        }

        @Override
        public Void visitAddExpr(Expr.Add expr) {
            resolve(expr.right);
            resolve(expr.left);
            return null;
        }

        @Override
        public Void visitRemoveExpr(Expr.Remove expr) {
            resolve(expr.right);
            resolve(expr.left);
            return null;
        }

        @Override
        public Void visitMultiplyExpr(Expr.Multiply expr) {
            resolve(expr.right);
            resolve(expr.left);
            return null;
        }

        @Override
        public Void visitDivideExpr(Expr.Divide expr) {
            resolve(expr.right);
            resolve(expr.left);
            return null;
        }

        @Override
        public Void visitCompareExpr(Expr.Compare expr) {
            resolve(expr.right);
            resolve(expr.left);
            return null;
        }

        @Override
        public Void visitExpressionStmt(Stmt.Expression stmt)
        {
            resolve(stmt.expression);
            return null;
        }

        @Override
        public Void visitIfStmt(Stmt.If stmt) {
            resolve(stmt.condition);
            resolve(stmt.thenBranch);
            if (stmt.elseBranch != null) resolve(stmt.elseBranch);
            return null;
        }

        @Override
        public Void visitPrintStmt(Stmt.Print stmt)
        {

            for(List<Expr> lines : stmt.expressions)
            {
                for(Expr expr : lines)
                {
                    resolve(expr);
                }
            }
            return null;
        }

        @Override
        public Void visitSubprogramStmt(Stmt.Subprogram subprogram)
        {

            declare(subprogram.name);
            define(subprogram.name);
            resolveFunction(subprogram,FunctionType.FUNCTION);
            return null;
        }

        @Override
        public Void visitInputStmt(Stmt.Input stmt) {
            resolve(stmt.prompt);
            return null;
        }

        @Override
        public Void visitErrorStmt(Stmt.Error stmt) {
            return null;
        }

        private void resolveFunction(Stmt.Subprogram subprogram, FunctionType type)
        {
            FunctionType enclosingFunction = currentFunction;
            currentFunction = type;

            beginScope();
            for(Parameter param : subprogram.params)
            {
                Symbol s = new Symbol(param.name.lexeme, param.type, null, true);
                symbols.put(s.name, s);

                declare(param.name);
                define(param.name);
                Symbol sym = getSymbol(param.name);
                sym.type = param.type;
            }
            resolve(subprogram.body);
            endScope();
            currentFunction = enclosingFunction;
        }

        @Override
        public Void visitReturnStmt(Stmt.Return stmt) {
            if(currentFunction == FunctionType.NONE)
            {
                Pulper.error(stmt.keyword, ": cannot resolve this keyword here, enclosing function missing");
            }
            if (currentFunction == FunctionType.INITIALIZER) {
                Pulper.error(stmt.keyword,
                        "Can't return a value from an initializer.");
            }
            if (stmt.value != null) {
                resolve(stmt.value);
            }

            return null;
        }

        @Override
        public Void visitWhileStmt(Stmt.While stmt) {
            resolve(stmt.condition);
            loopDepth++;
            resolve(stmt.body);
            loopDepth--;
            return null;
        }

        @Override
        public Void visitBreakStmt(Stmt.Break stmt) {
            if(loopDepth == 0)
            {
                Pulper.error(stmt.keyword,": cannot resolve 'break' outside of loop");
            }
            return null;
        }

        @Override
        public Void visitCallExpr(Expr.Call expr) {
            resolve(expr.callee);

            for (Expr argument : expr.arguments) {
                resolve(argument);
            }

            return null;
        }

        @Override
        public Void visitCastExpr(Expr.Cast expr) {
            resolve(expr.right);
            return null;
        }

        @Override
        public Void visitGetExpr(Expr.Get expr) {
            resolve(expr.object);
            return null;
        }

        @Override
        public Void visitLiteralExpr(Expr.Literal expr)
        {
            return null;
        }

        @Override
        public Void visitMultistringExpr(Expr.Multistring expr) {
            for(Expr exp : expr.strings)
            {
                resolve(exp);
            }
            return null;
        }

        @Override
        public Void visitLogicalExpr(Expr.Logical expr) {
            resolve(expr.left);
            resolve(expr.right);
            return null;
        }

        @Override
        public Void visitSetExpr(Expr.Set expr) {
            resolve(expr.value);
            resolve(expr.object);
            return null;
        }

        @Override
        public Void visitErrorExpr(Expr.Error expr) {
            System.out.println("Resolver visits error expr");
            return null;
        }

        @Override
        public Void visitThisExpr(Expr.This expr) {
            if(currentClass == ClassType.NONE)
            {
                Pulper.error(expr.keyword , " cannot use this outside of class");
            }
            resolveLocal(expr, expr.keyword);
            return null;
        }


        @Override
        public Void visitUnaryExpr(Expr.Unary expr) {
            resolve(expr.right);
            return null;
        }

        public Symbol getSymbol(Token name)
        {
           return symbols.get(name.lexeme);
        }

        public boolean updateSymbol(Symbol s)
        {
            Symbol newSymbol = s;
            return symbols.replace(s.name, symbols.get(s.name), s);
        }

    }

