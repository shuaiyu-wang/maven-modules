package wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

// 批处理 word count
public class WordCount {
    public static void main(String[] args) throws Exception {
        // 创建执行环境
        ExecutionEnvironment environment = ExecutionEnvironment.getExecutionEnvironment();
        // 从文件中读取数据
        String inputPath = "D:\\project\\learn\\maven-modules-new\\maven-flink\\src\\main\\resources\\hello.txt";
        DataSet<String> inputDataSet = environment.readTextFile(inputPath);
        // 对数据集进行处理，按空格分词展开，转换成(word, 1) 二元数组进行统计
        AggregateOperator<Tuple2<String, Integer>> resultSet = inputDataSet.flatMap(new MyFlatMapper())
                .groupBy(0) // 按第一个word的位置进行分组
                .sum(1);// 将第二个位置上的数据求和
        resultSet.print();
    }

    // 自定义一个类实现FlatMapFunction接口
    public static class MyFlatMapper implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
            // 按空格分词
            String[] words = value.split(" ");
            // 遍历所有的word，包成二元组输出
            for (String word : words) {
                out.collect(new Tuple2<>(word, 1));
            }
        }
    }
}
