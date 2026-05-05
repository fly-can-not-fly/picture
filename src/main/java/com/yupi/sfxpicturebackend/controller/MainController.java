package com.yupi.sfxpicturebackend.controller;

import com.aliyun.core.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;
import com.yupi.sfxpicturebackend.annotation.AuthCheck;
import com.yupi.sfxpicturebackend.common.BaseResponse;
import com.yupi.sfxpicturebackend.common.ResultUtils;
import com.yupi.sfxpicturebackend.constant.UserConstant;
import com.yupi.sfxpicturebackend.exception.BusinessException;
import com.yupi.sfxpicturebackend.exception.ErrorCode;
import com.yupi.sfxpicturebackend.manager.OSSManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/")
@Slf4j
public class MainController {
    private final OSSManager ossManager;

    public MainController(OSSManager ossManager) {
        this.ossManager = ossManager;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }

    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + filename;
        File file = new File(filepath);
        try {
            // 上传文件
            multipartFile.transferTo(file);
            ossManager.upload(file.getName(), file.getAbsolutePath());
            // 返回可访问地址
            System.out.println(ossManager.pictureInfo(file.getName()));
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }
    /**
     * 测试文件下载
     *
     * @param fileName 文件名称
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String fileName, HttpServletResponse response) throws IOException {
        InputStream objectContentStream = null;
        OSSObject ossObject = null;
        try {
             ossObject = ossManager.download2(fileName);
             objectContentStream = ossObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(objectContentStream);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + fileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (objectContentStream != null) {
                objectContentStream.close();
                ossObject.close();
            }
        }
    }

}
