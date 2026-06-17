package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException
    {
        if(args.length != 1)
        {
            System.err.println("Usage : generate_ast <output_directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr",
                Arrays.asList(
                        "Literal : Object value, Type type",
                        "Multistring : List<Expr> strings",
                        "Unary : Token operator, Expr right",
                        "Logical : Expr left, Token operator, Expr right, Type type",
                        "This : Token keyword",
                        "Assign : Token name, Expr value",
                        "Add : Token keyword, Expr left, Expr right",
                        "Remove : Token keyword, Expr left, Expr right",
                        "Multiply : Token keyword, Expr left, Expr right",
                        "Divide : Token keyword, Expr left, Expr right",
                        "Compare : Expr left, ComparisonType type, Expr right",
                        "Variable : Token name",
                        "Call : Expr callee, Token paren, List<Expr> arguments",
                        "Cast : Type targetType, Expr right",
                        "Get      : Expr object, Token name",
                        "Set      : Expr object, Token name, Expr value",
                        "Error : Token t"

                ));

        defineAst(outputDir, "Stmt",
                Arrays.asList(
                        "Block : List<Stmt> statements",
                        "Program : Token name, List<Stmt.Subprogram> methods, List<Stmt> statements",
                        "Expression : Expr expression",
                        "Print : List<List<Expr>> expressions",
                        "Var : Token name, Type declaredType, Expr initializer",
                        "Return : Token keyword, Expr value",
                        "If : Expr condition, Stmt thenBranch," + "Stmt elseBranch",
                        "While : Expr condition, Stmt body",
                        "Break : Token keyword",
                        "Subprogram : Token name, List<Token> params," + " List<Stmt> body",
                        "Error: Token token, String message"
                ));
    }




    private static void defineAst(
            String outputdir, String baseName, List<String> types)
            throws IOException
    {
            String path = outputdir + "/" + baseName + ".java";
            PrintWriter writer = new PrintWriter(path, "UTF-8");

            writer.println("package pulp;");
            writer.println("import java.util.List;");
            writer.println("import pulp.ComparisonType;");
            writer.println("import pulp.Expr;");
            writer.println();
            writer.println("abstract class " + baseName + "{");

            defineVisitor(writer,baseName,types);

            for(String type : types)
            {
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer,baseName,className,fields);
            }

            writer.println("    abstract <R> R accept(Visitor<R> visitor);");
            writer.println("}");
            writer.close();

    }


    private static void defineType(
            PrintWriter writer, String baseName, String className, String fieldList)
    {

        writer.println(" static class " + className + " extends " + baseName + " {");

        writer.println("    " + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(",");
        for(String field : fields)
        {
            field = field.trim();
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }
        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("    return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        writer.println();
        for (String field : fields) {
            field = field.trim();
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }

    private static void defineVisitor(
     PrintWriter writer, String baseName, List <String> types
    )
    {
        writer.println("    interface Visitor<R> {");
        for(String type : types)
        {
            type = type.trim();
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit"+ typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println(" }");
    }


}
