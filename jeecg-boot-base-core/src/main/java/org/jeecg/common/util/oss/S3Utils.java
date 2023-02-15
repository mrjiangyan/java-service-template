//package org.jeecg.common.util.oss;
//
//import com.aliyun.oss.internal.Mimetypes;
//import com.amazonaws.ClientConfiguration;
//import com.amazonaws.Protocol;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.client.builder.AwsClientBuilder;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import lombok.Getter;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
//import software.amazon.awssdk.core.ResponseInputStream;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Base64;
//import java.util.List;
//
//@Slf4j
//public class S3Utils {
//
//  private static String accessKey;
//  private static String secretKey;
//  private static String region;
//
//  private static S3Client client;
//
//  //  bucket
//  @Getter
//  private static String bucket;
//
//  private static String host;
//
//  public static void setAccessKey(String accessKey) {
//    S3Utils.accessKey = accessKey;
//  }
//
//  public static void setSecretKey(String secretKey) {
//    S3Utils.secretKey = secretKey;
//  }
//
//  public static void setBucket(String bucket) {
//    S3Utils.bucket = bucket;
//  }
//
//  public static void setRegion(String region) {
//    S3Utils.region = region;
//  }
//
//  public static void setHost(String host) {
//    S3Utils.host = host;
//  }
//
//  /**
//   * 获取S3Client对象
//   *
//   * @return s3
//   */
//  public static S3Client getS3() throws URISyntaxException {
//    if(client == null){
//      if(client == null){
//        client = S3Client.builder()
//          .endpointOverride(new URI(host))
//          .credentialsProvider(getAwsCredentialsProviderChain())
//          .region(Region.of(region))
//          .build();
//      }
//    }
//    return client;
//  }
//
//  // 获取aws供应商凭据
//  private static AwsCredentialsProviderChain getAwsCredentialsProviderChain(){
//    return AwsCredentialsProviderChain
//      .builder()
//      .addCredentialsProvider(() -> AwsBasicCredentials.create(accessKey, secretKey)).build();
//  }
//
//  @SneakyThrows
//  public static void writeS3ObjectToStream(S3Client s3, String bucketName, String objectKey, OutputStream stream) {
//    ResponseInputStream<GetObjectResponse> responseInputStream = null;
//    try {
//      GetObjectRequest putOb = GetObjectRequest.builder()
//        .bucket(bucketName)
//        .key(objectKey)
//        .build();
//      responseInputStream = s3.getObject(putOb);
//      byte[] bufferByte = new byte[4096];
//      int len;
//      while ((len = responseInputStream.read(bufferByte)) != -1) {
//        stream.write(bufferByte, 0, len);
//      }
//
//    } catch (S3Exception e) {
//      log.error("error:{}", e.getMessage());
//    }
//    finally{
//      if(responseInputStream != null){
//        responseInputStream.close();
//      }
//        if(stream != null){
//            stream.flush();
//        }
//      }
//  }
//
//  @SneakyThrows
//  public static void writeS3ImageThumbnailToStream(S3Client s3, String bucketName, String objectKey, OutputStream stream, int width, int height) {
//    ResponseInputStream<GetObjectResponse> responseInputStream = null;
//    try {
//      GetObjectRequest putOb = GetObjectRequest.builder()
//        .bucket(bucketName)
//        .key(objectKey)
//        .build();
//      responseInputStream = s3.getObject(putOb);
//      BufferedImage source = ImageIO.read(responseInputStream);
//
//      Image resultingImage = source.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
//      BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//      outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
//      //输出图片
//      ImageIO.write(outputImage, "JPG", stream);
//    } catch (Exception e) {
//      log.error("error:{}", e.getMessage());
//    }
//    finally{
//      if(responseInputStream != null){
//        responseInputStream.close();
//      }
//      if(stream != null){
//        stream.flush();
//      }
//    }
//  }
//
//  public static String putS3Object(S3Client s3, String bucketName, String objectKey, byte[] data) {
//    String mimetype = Mimetypes.getInstance().getMimetype(objectKey);
//    PutObjectRequest putOb = PutObjectRequest.builder()
//      .bucket(bucketName)
//      .key(objectKey)
//      .contentType(mimetype)
//      .build();
//    s3.putObject(putOb, RequestBody.fromBytes(data));
//    return objectKey;
//  }
//
//  public static List<Bucket> buckets(S3Client s3) {
//    return s3.listBuckets().buckets();
//  }
//
//  public static String getObject(S3Client s3, String bucketName, String objectKey) throws IOException {
//    GetObjectRequest putOb = GetObjectRequest.builder()
//      .bucket(bucketName)
//      .key(objectKey)
//      .build();
//
//    byte[] bufferByte = new byte[4096];
//    int len;
//    try (ResponseInputStream<GetObjectResponse> responseInputStream = s3.getObject(putOb); ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
//      while ((len = responseInputStream.read(bufferByte)) != -1) {
//        stream.write(bufferByte, 0, len);
//      }
//      byte[] data = stream.toByteArray();
//      return Base64.getEncoder().encodeToString(data);
//    }
//
//  }
//
//  public static ResponseInputStream<GetObjectResponse> getObjectStream(S3Client s3, String bucketName, String objectKey) throws IOException {
//    GetObjectRequest putOb = GetObjectRequest.builder()
//      .bucket(bucketName)
//      .key(objectKey)
//      .build();
//
//    return s3.getObject(putOb);
//  }
//
//  public static void createBucket(String bucketName) {
//    AmazonS3 s3client;
//    //设置连接时的参数
//    ClientConfiguration config = new ClientConfiguration();
//    //设置连接方式为HTTP，可选参数为HTTP和HTTPS
//    config.setProtocol(Protocol.HTTP);
//    //设置网络访问超时时间
//    config.setConnectionTimeout(30000);
//    config.setUseExpectContinue(true);
//    //获取访问凭证
//    String access_key = accessKey;
//    String secret_key = secretKey;
//    AWSCredentials credentials = new BasicAWSCredentials(access_key, secret_key);
//    //设置Endpoint
//    AwsClientBuilder.EndpointConfiguration end_point = new AwsClientBuilder.EndpointConfiguration(host, region);
//    //创建连接
//    s3client = AmazonS3ClientBuilder.standard()
//            .withCredentials(new AWSStaticCredentialsProvider(credentials))
//            .withClientConfiguration(config)
//            .withEndpointConfiguration(end_point)
//            .withPathStyleAccessEnabled(true)
//            .build();
//    s3client.createBucket(bucketName);
//  }
//
//}
