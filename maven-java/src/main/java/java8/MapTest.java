package java8;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MapTest {

    public static void main(String[] args) {

        Integer[] integers = new Integer[10];
        for (int i = 0; i < 10; i++) {
            integers[i] = i;
        }

        List<String> collect = Arrays.stream(integers).map(String::valueOf).collect(Collectors.toList());




    }
}
