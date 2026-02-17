package com.campus.ordering.controller.common;

import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/public/upload")
public class FileUploadController {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 单文件上传
     */
    @PostMapping("/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "images");
    }

    /**
     * 通用文件上传
     */
    @PostMapping("/file")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "files");
    }

    private Result<?> uploadFile(MultipartFile file, String subDir) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error(400, "文件大小不能超过5MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            return Result.error(400, "不支持的文件类型，仅支持 JPG、PNG、GIF、WebP 格式");
        }

        try {
            // 创建日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fullPath = uploadPath + subDir + "/" + dateDir;
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            // 保存文件
            File destFile = new File(fullPath, fileName);
            file.transferTo(destFile);

            // 返回访问URL
            String accessUrl = "/uploads/" + subDir + "/" + dateDir + "/" + fileName;
            Map<String, String> data = new HashMap<>();
            data.put("url", accessUrl);
            data.put("fileName", fileName);
            data.put("originalName", originalFilename);

            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传
     */
    @PostMapping("/images")
    public Result<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error(400, "请选择要上传的文件");
        }

        if (files.length > 9) {
            return Result.error(400, "最多同时上传9张图片");
        }

        Map<String, Object> data = new HashMap<>();
        var urls = new java.util.ArrayList<Map<String, String>>();

        for (MultipartFile file : files) {
            Result<?> result = uploadFile(file, "images");
            if (result.getCode() == 200) {
                urls.add((Map<String, String>) result.getData());
            }
        }

        data.put("urls", urls);
        data.put("total", urls.size());
        return Result.success("上传成功", data);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<?> deleteFile(@RequestBody Map<String, String> params) {
        String url = params.get("url");
        if (url == null || url.isEmpty()) {
            return Result.error(400, "文件路径不能为空");
        }

        // 防止路径遍历攻击
        if (url.contains("..") || url.contains("\\")) {
            return Result.error(400, "非法文件路径");
        }

        // 解析文件路径
        String relativePath = url.replace("/uploads/", "");
        File file = new File(uploadPath + relativePath);

        // 二次校验：确保文件在上传目录内
        try {
            String canonicalUpload = new File(uploadPath).getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalUpload)) {
                return Result.error(400, "非法文件路径");
            }
        } catch (IOException e) {
            return Result.error(400, "文件路径解析失败");
        }

        if (file.exists()) {
            if (file.delete()) {
                return Result.success("删除成功");
            } else {
                return Result.error(500, "文件删除失败");
            }
        } else {
            return Result.error(404, "文件不存在");
        }
    }

    private boolean isAllowedType(String contentType) {
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "jpg";
    }
}
