package learn;

import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: maskimko
 * Date: 11/21/13
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapEachTest {

    public LinkedHashMap<String, String>  testMap  = new LinkedHashMap<String, String>();

    public static void main(String[] args){

        MapEachTest met = new MapEachTest();
        met.testMap.put("1key", "value one");
        met.testMap.put("1key", "value two");
        met.testMap.put("2key", "value tree");
        met.testMap.put("2key", "value four");

        String res1 = met.testMap.get("1key");
        String res2 = met.testMap.get("2key");
        System.out.println("Result 1 " + res1);
        System.out.println("Result 2 " + res2);
    }
}
