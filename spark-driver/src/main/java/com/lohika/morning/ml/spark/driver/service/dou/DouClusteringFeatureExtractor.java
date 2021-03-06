package com.lohika.morning.ml.spark.driver.service.dou;

import com.lohika.morning.ml.spark.distributed.library.function.map.dou.ToClusteringTrainingExample;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class DouClusteringFeatureExtractor extends Transformer implements MLWritable {

    private String uid;

    public DouClusteringFeatureExtractor(String uid) {
        this.uid = uid;
    }

    public DouClusteringFeatureExtractor() {
        this.uid = "DouClusteringFeatureExtractor" + "_" + UUID.randomUUID().toString();
    }

    @Override
    public Dataset<Row> transform(Dataset dataset) {
        dataset = dataset.select("salary", "experience", "english_level");

        if (Arrays.asList(dataset.columns()).contains("position")) {
             dataset = dataset.filter(dataset.col("position").contains("Engineer")
                                      .or(dataset.col("position").equalTo("Technical Lead"))
                                      .or(dataset.col("position").equalTo("System Architect")));
        }

        dataset = dataset.map(
                new ToClusteringTrainingExample(),
                RowEncoder.apply(transformSchema(dataset.schema())));

        dataset.count();
        dataset.cache();

        return dataset;
    }

    @Override
    public StructType transformSchema(StructType schema) {
        return new StructType(new StructField[]{
                DataTypes.createStructField("features", new VectorUDT(), false)
        });
    }

    @Override
    public Transformer copy(ParamMap extra) {
        return super.defaultCopy(extra);
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public MLWriter write() {
        return new DefaultParamsWriter(this);
    }

    @Override
    public void save(String path) throws IOException {
        write().save(path);
    }

    public static MLReader<DouClusteringFeatureExtractor> read() {
        return new DefaultParamsReader<>();
    }
}
