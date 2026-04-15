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
                        "Literal : Object value",
                        "Identifier : String name",
                        "Assign : String name, Expr value",
                        "Add : Expr left, Expr right"
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
            writer.println();
            writer.println("abstract class " + baseName + "{");

            for(String type : types)
            {
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer,baseName,className,fields);
            }

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
        for (String field : fields) {
            field = field.trim();
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }


}
