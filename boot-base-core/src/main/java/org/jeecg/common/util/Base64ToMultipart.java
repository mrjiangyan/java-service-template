//package org.jeecg.common.util;
//
//import lombok.SneakyThrows;
//import lombok.extern.log4j.Log4j2;
//import org.bouncycastle.util.encoders.Base64;
//import org.springframework.web.multipart.MultipartFile;
//
//
///**
// * 传入图片的base64图像数据，转换成 MultipartFile
// */
//@Log4j2
//public class Base64ToMultipart {
//
//    /**
//     * 传入图片的base64数据，转换成 MultipartFile
//     *
//     * @param base64 图片的base64数据
//     * @return MultipartFile实例
//     */
//    @SneakyThrows
//    public static MultipartFile base64ToMultipart(String base64) {
//        String[] baseStrs = base64.split(",");
//
//        // 对图片的base64数据解码
//        byte[] b;
//        if (baseStrs.length > 1) {
//            b = Base64.decode(baseStrs[1]);
//        } else {
//            b = Base64.decode(baseStrs[0]);
//        }
//
//        for (int i = 0; i < b.length; ++i) {
//            if (b[i] < 0) {
//                b[i] += 256;
//            }
//        }
//        return new Base64DecodedMultipartFile(b, baseStrs[0]);
//    }
//}
