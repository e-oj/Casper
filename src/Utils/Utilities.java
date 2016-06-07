package Utils;

import CSS_Parser.CSS;

import java.util.List;
import java.util.Map;

/**
 * @author Emmanuel Olaojo
 * @since 6/7/16
 */
public class Utilities {
    public static void logMap(Map<String, List<CSS>> map, Logger logger){
        map.forEach((key, val) -> {
            if (val.size() > 1) {
                logger.println(key);
                logger.println("_________________________________________________________________");
                for(CSS css: val){
                    try {
                        logger.println(css);
                    } catch (NullPointerException ne){
                        System.out.println(css);
                    }
                }
                logger.println("=====================================================================");
            }
        });
    }

    public static void logMap(Map<String, List<CSS>> map){
        map.forEach((key, val) -> {
            if(val.size() > 1) {
                System.out.println(key);
                System.out.println("_________________________________________________________________");
                val.forEach(System.out::println);
                System.out.println("=====================================================================");
            }
        });
    }
}
