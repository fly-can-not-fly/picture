package com.yupi.sfxpicturebackend.manager;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.*;
import com.yupi.sfxpicturebackend.config.OssClientConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author 孙飞翔
 */
@Component
public class OSSManager {
    private final OSS ossClient;
    private final OssClientConfig ossClientConfig;

    public OSSManager(OSS ossClient, OssClientConfig ossClientConfig) {
        this.ossClient = ossClient;
        this.ossClientConfig = ossClientConfig;
    }

    /**
     * 上传文件到OSS
     *
     * @param filePath
     * @return
     */
    public PutObjectResult upload(String objectName, String filePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossClientConfig.getBucketName(), objectName, new File(filePath));
        return ossClient.putObject(putObjectRequest);
    }

    /**
     * 下载文件到本地服务器
     */
    public void download1(String objectName, String localFilePath) {
        ossClient.getObject(new GetObjectRequest(ossClientConfig.getBucketName(), objectName), new File(localFilePath));
    }

    /**
     * 获取文件对象，可以通过获取输入流直接返回给用户
     *
     * @param objectName
     * @return
     */
    public OSSObject download2(String objectName) {
        return ossClient.getObject(ossClientConfig.getBucketName(), objectName);
    }

    /**
     * 获取图片信息
     * {
     * "FileSize": {"value": "330410"},
     * "Format": {"value": "png"},
     * "FrameCount": {"value": "1"},
     * "ImageHeight": {"value": "1440"},
     * "ImageWidth": {"value": "2560"}
     * }
     */
    public String pictureInfo(String objectName) {
        String url = String.format("https://sfx-picture.oss-cn-beijing.aliyuncs.com/%s?x-oss-process=image/info", objectName);
        return HttpUtil.get(url);
    }

    /**
     * 持久化处理图片,将处理后的图片放到原来的bucket
     * @param sourceImage 原图片完整路径
     * @param targetImage 转换后图片完整路径
     * @param styleType 样式转换方式 eg："image/format,webp"
     * @return 是否转化成功
     * @throws IOException
     */
    public boolean processPermanentPic(String sourceImage,
                                       String targetImage,
                                       String styleType) throws IOException {
        String process = String.format("%s|sys/saveas,o_%s,b_%s", styleType,
                BinaryUtil.toBase64String(targetImage.getBytes()),
                BinaryUtil.toBase64String(ossClientConfig.getBucketName().getBytes()));
        ProcessObjectRequest request = new ProcessObjectRequest(ossClientConfig.getBucketName(), sourceImage, process);
        GenericResult processResult = ossClient.processObject(request);
        String json = IOUtils.readStreamAsString(processResult.getResponse().getContent(), "UTF-8");
        processResult.getResponse().getContent().close();
        String status = (String) JSONUtil.parseObj(json).get("status");
        return "OK".equals(status);
    }

    /**
     * 删除单个文件
     * @param objectName 文件完整路径
     */
    public void deleteOneFile(String objectName){
        ossClient.deleteObject(ossClientConfig.getBucketName(), objectName);
    }
}
