package com.yupi.sfxpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.sfxpicturebackend.config.OssClientConfig;
import com.yupi.sfxpicturebackend.exception.BusinessException;
import com.yupi.sfxpicturebackend.exception.ErrorCode;
import com.yupi.sfxpicturebackend.exception.ThrowUtils;
import com.yupi.sfxpicturebackend.manager.OSSManager;
import com.yupi.sfxpicturebackend.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected OSSManager ossManager;

    @Resource
    protected OssClientConfig ossClientConfig;

    /**
     * 模板方法，定义上传流程
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片  
        validPicture(inputSource);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
        String filepath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + uploadFilename;
        File file = new File(filepath);
        try {
            // 处理文件来源（本地或 URL）  
            processFile(inputSource, file);
            // 4. 上传图片到对象存储  
            ossManager.upload(uploadPath, file.getAbsolutePath());
            // 持久化处理图片，格式转为webp
            String uploadFileNewName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                    "webp");
            String uploadNewPath = String.format("%s/%s", uploadPathPrefix, uploadFileNewName);
            String styleType = "image/format,webp";
            boolean result = ossManager.processPermanentPic(uploadPath, uploadNewPath, styleType);
            ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR,"图片转webp失败");
            // 上传缩略图
            String uploadThumbnailFileName = String.format("%s_%s_thumbnailUrl.%s", DateUtil.formatDate(new Date()), uuid,
                    "webp");
            String uploadThumbnailPath = String.format("%s/%s", uploadPathPrefix, uploadThumbnailFileName);
            String styleTypeThumbnail = "image/resize,w_200";
            boolean result2 = ossManager.processPermanentPic(uploadNewPath, uploadThumbnailPath, styleTypeThumbnail);
            ThrowUtils.throwIf(!result2,ErrorCode.SYSTEM_ERROR,"图片缩略失败");
            // 获取处理后的图片信息
            String infoString = ossManager.pictureInfo(uploadNewPath);
            JSONObject info = JSONUtil.parseObj(infoString);
            // 5. 封装返回结果  
            return buildResult(originFilename,uploadNewPath,uploadThumbnailPath, info);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6. 清理临时文件  
            deleteTempFile(file);
            // 删除原来的图片
            ossManager.deleteOneFile(uploadPath);
        }
    }

    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 封装返回结果
     */
    private UploadPictureResult buildResult(String originFilename, String uploadPath,String uploadThumbnailPath, JSONObject info) {
        int picWidth = Integer.parseInt(info.getJSONObject("ImageWidth").getStr("value"));
        int picHeight = Integer.parseInt(info.getJSONObject("ImageHeight").getStr("value"));
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        String format = info.getJSONObject("Format").getStr("value");
        long size = Long.parseLong(info.getJSONObject("FileSize").getStr("value"));
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(format);
        uploadPictureResult.setPicSize(size);
        uploadPictureResult.setUrl(ossClientConfig.getUrlPrefix() + "/" + uploadPath);
        uploadPictureResult.setThumbnailUrl(ossClientConfig.getUrlPrefix() + "/" + uploadThumbnailPath);
        return uploadPictureResult;
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
