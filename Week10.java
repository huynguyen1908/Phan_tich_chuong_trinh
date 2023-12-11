import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Week10 {

    static String packageRegex = "^package\\s+(.*);$";
    static Pattern PACKAGE_PATTERN = Pattern.compile(packageRegex, Pattern.MULTILINE);


    static String importRegex = "^import\\s+(?:static\\s+)?((?:\\w+\\.)+(\\w+));$";
    static Pattern IMPORT_PATTERN = Pattern.compile(importRegex, Pattern.MULTILINE);


    static String classRegex = "^(?:\\s{2})*(?:(?"
            + ":abstract|public|private|protected|static|final)\\s+)*"
            + "(class|interface|enum)\\s+([^<\\s]+)([^{]+)?\\s*\\{";
    static Pattern CLASS_PATTERN = Pattern.compile(classRegex, Pattern.MULTILINE);


    static String regexPattern = "^(?<!\\s{0,20}/\\*\\n)"
            + "(?:\\s{2})+(?:(?:public|private|protected)\\s+)*"
            + "static\\s+(?:final\\s+)?[\\w<>,.?\\[\\]\\s]+\\s+(\\w+)"
            + "\\s*\\(([\\w<>,.?\\[\\]\\s]*)\\)\\s*\\{";

    static Pattern STATIC_METHOD_PATTERN = Pattern.compile(regexPattern, Pattern.MULTILINE);

    private static final Map<String, String> dataTypeMap = new HashMap<>();


    private static String toFullType(String dataType) {
        if (dataTypeMap.containsKey(dataType)) {
            return dataTypeMap.get(dataType);
        } else if (dataType.matches("[A-Z]\\w+")) {
            return "java.lang." + dataType;
        } else if (dataType.contains("<")) {
            String[] parts = dataType.split("<");
            parts[0] = toFullType(parts[0]);
            parts[1] = toFullType(parts[1].replace(">", ""));

            return String.format("%s<%s>", parts[0], parts[1]);
        } else {
            return dataType;
        }
    }

    /**
     * getAllFunctions.
     *
     * @param fileContent .
     * @return .
     */

    public static List<String> getAllFunctions(String fileContent) {
        List<String> methods = new ArrayList<>();

        String packageName = "";
        Matcher matcher = PACKAGE_PATTERN.matcher(fileContent);
        if (matcher.find()) {
            packageName = matcher.group(1);
        }

        matcher = IMPORT_PATTERN.matcher(fileContent);
        while (matcher.find()) {
            String fullImport = matcher.group(1);
            String nameImport = matcher.group(2);
            dataTypeMap.put(nameImport, fullImport);
        }

        matcher = CLASS_PATTERN.matcher(fileContent);
        while (matcher.find()) {
            String className = matcher.group(2);
            dataTypeMap.put(className, packageName + "." + className);
        }

        matcher = STATIC_METHOD_PATTERN.matcher(fileContent);
        while (matcher.find()) {

            String methodName = matcher.group(1);
            String param = matcher.group(2);
            StringBuilder type = new StringBuilder();
            type.append("(");

            if (!param.isEmpty()) {
                param = param.replaceAll("\\.{3}", "");
                param = param.replace("\n", "").trim();

                String[] params = param.split(", ");
                for (int i = 0; i < params.length; i++) {
                    params[i] = toFullType(params[i].split(" ")[0]);
                }

                for (String temp : params) {
                    type.append(temp).append(",");
                }

                type.deleteCharAt(type.length() - 1);
            }

            type.append(")");
            methods.add(methodName + type.toString());
        }

        return methods;
    }
}