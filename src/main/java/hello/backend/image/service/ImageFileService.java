package hello.backend.image.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.gcs.service.GCSService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImageFileService {

    private final GCSService gcsService;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    public String saveFile(MultipartFile image, String userId, String subDir) {
        String extension = getFileExtension(image.getOriginalFilename());
        String newFileName = UUID.randomUUID() + "." + extension;
        try {
            return gcsService.uploadToGCS(image.getBytes(), userId, subDir, newFileName, image.getContentType());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
        }
    }

    public String saveLogoFile(MultipartFile image, String userId, String subDir) {
        String extension = "png";
        String contentType = "image/png";
        String newFileName = UUID.randomUUID() + "." + extension;

        try {
            BufferedImage originalImage = ImageIO.read(image.getInputStream());
            BufferedImage resizedImage = resizeWithPadding(originalImage, 200, 200);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, extension, baos);
            byte[] resizedImageBytes = baos.toByteArray();

            return gcsService.uploadToGCS(resizedImageBytes, userId, subDir, newFileName, contentType);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
        }
    }

    private BufferedImage resizeWithPadding(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        float scale = Math.min((float) targetWidth / originalWidth, (float) targetHeight / originalHeight);
        int scaledWidth = Math.round(originalWidth * scale);
        int scaledHeight = Math.round(originalHeight * scale);

        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = outputImage.createGraphics();

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, targetWidth, targetHeight);
        g.setComposite(AlphaComposite.SrcOver);

        g.drawImage(originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH), x, y, null);
        g.dispose();

        return outputImage;
    }

    public String saveBgRemoveFile(byte[] imageBytes, String userId, Long imageId, String subDir) {
        String fileName = "processed_" + imageId + ".png";
        return gcsService.uploadToGCS(imageBytes, userId, subDir, fileName, "image/png");
    }

    public String saveBase64Image(String base64Data, String userId, String subDir) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String fileName = "processed_" + UUID.randomUUID() + ".png";
            return gcsService.uploadToGCS(imageBytes, userId, subDir, fileName, "image/png");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
        }
    }

    public String chooseFinalImage(File localFile, String userId, Long imageId, String subDir) {
        try {
            byte[] imageBytes = Files.readAllBytes(localFile.toPath());
            String fileName = "final_" + imageId + ".png";
            return gcsService.uploadToGCS(imageBytes, userId, subDir, fileName, "image/png");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        return extension;
    }
}
