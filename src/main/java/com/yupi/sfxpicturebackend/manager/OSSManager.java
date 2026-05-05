package com.yupi.sfxpicturebackend.manager;

import cn.hutool.http.HttpUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.yupi.sfxpicturebackend.config.OssClientConfig;
import org.springframework.stereotype.Component;

import java.io.File;

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
}
