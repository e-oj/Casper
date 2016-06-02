package CSS_Parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmanuel Olaojo
 * @since 5/30/16
 */
public class CSS {
    Map<String, String> cssData = new HashMap<>();
    private String fileName;
    private String identifier = "";

    public CSS(String fileName, String identifier){
        this.fileName = fileName;
        this.identifier = identifier;
    }

    public CSS(String fileName){
        this.fileName = fileName;
    }

    public CSS(CSS css, String identifier){
        this.fileName = css.fileName;
        this.identifier = identifier;
        this.cssData = new HashMap<>(css.cssData);
    }

    public void addField(String prop, String val){
        this.cssData.put(prop, val);
    }

    public String getFileName(){
        return this.fileName;
    }

    public String toString(){
        StringBuilder cssString = new StringBuilder(this.identifier);

        cssString.append("{\n");

        cssData.forEach((prop, val) -> cssString.append("  ")
                .append(prop)
                .append(": ")
                .append(val)
                .append(";\n")
        );

        cssString.append("}");

        return cssString.toString();
    }

    public static void main(String[] args) {
        CSS style = new CSS("style2.css", ".test");

        style.addField("position", "relative");
        style.addField("height", "20px");
        style.addField("width", "20px");
        style.addField("color", "#F8F8F8");

        System.out.println(style);
    }
}
