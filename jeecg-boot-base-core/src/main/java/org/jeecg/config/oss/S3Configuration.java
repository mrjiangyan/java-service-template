//package org.jeecg.config.oss;
//
//import org.jeecg.common.util.oss.S3Utils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//@Component
//public class S3Configuration {
//
//  @Value("${aws.s3.accessKey}")
//  private String accessKey;
//  @Value("${aws.s3.secretKey}")
//  private String secretKey;
//  @Value("${aws.s3.region}")
//  private String region;
//  @Value("${aws.s3.bucket}")
//  private String bucket;
//
//  @Value("${aws.s3.host}")
//  private String host;
//
//  @Bean
//  public void initS3Configuration() {
//    S3Utils.setAccessKey(accessKey);
//    S3Utils.setSecretKey(secretKey);
//    S3Utils.setRegion(region);
//    S3Utils.setBucket(bucket);
//    S3Utils.setHost(host);
//  }
//
//}
