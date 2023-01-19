package com.test.efs.efstest;

import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class UploadService {

    public static final String SHARED = "/mnt/efs/fs1/resource/static/shared";
    public static final String EFS = "/mnt/efs/fs1";

    public List<Map<String, Object>> uploadResource(MultipartFile[] files) throws IOException {
        String currentDateString = getCurrentDate();

        File targetDir = new File(SHARED, currentDateString);
        log.info("File name {}" , targetDir.getName());
        if (!targetDir.exists()) {
            log.info("targetDir.exists() {}", targetDir.exists());
            if (!targetDir.mkdirs()) {
                log.info("targetDir.mkdirs() fail");
                throw new RuntimeException();
            }
        }

        List<Map<String, Object>> resInfoList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            BufferedImage bufferedImage = ImageIO.read(files[i].getInputStream());
            int imgWidth = bufferedImage.getWidth();

            String originFileName = files[i].getOriginalFilename();

            String resourceId = UUID.randomUUID().toString();
            File targetFile = new File(targetDir, resourceId);

            try {
                targetFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(targetFile);
                fos.write(files[i].getBytes());
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException();
            }

            String resourceUri = SHARED + "/" + targetDir.getName() + "/" + resourceId;

            Map<String, Object> resInfo = new HashMap<>();
            resInfo.put("resourceId", resourceId);
            resInfo.put("resourceUri", resourceUri);
            resInfo.put("originFileName", originFileName);
            resInfo.put("imgWidth", imgWidth + "pt");
            resInfoList.add(resInfo);
        }

        return resInfoList;
    }

    public void moveFile(String url) {
        String newImgLink = null;
        if (url != null) {
//            _deleteFromResourcesDir(url);
            newImgLink = _moveToResourcesDir(url);
        }
        System.out.println("newImgLink = " + newImgLink);
    }

    private String getCurrentDate() {
        Date currentDate = Calendar.getInstance(Locale.KOREA).getTime();
        SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
        sdf_date.setTimeZone(TimeZone.getTimeZone("ROK"));

        String currentDateString = sdf_date.format(currentDate);
        return currentDateString;
    }

    private String _moveToResourcesDir(String uploadUri) {
        if (uploadUri == null) return null;
        String upload_relPath = uploadUri.substring(SHARED.length());
        File uploadFile = new File(SHARED, upload_relPath);

        log.info("uploadFile name {}", uploadFile.getName());
        log.info("uploadFile path {}", uploadFile.getPath());

//        String rsc_relPath = "/" + uploadFile.getName().substring(0, 2) +
//                "/" + uploadFile.getName().substring(2, 4) +
//                "/" + uploadFile.getName();

        String rsc_relPath = "/" + uploadFile.getName();

        File rscFile = new File(EFS + rsc_relPath);

        log.info("rscFile name {}", rscFile.getName());
        log.info("rscFile path {}", rscFile.getPath());

        File rscDir = rscFile.getParentFile();
        rscDir.getParentFile().setWritable(true, false);
        if (!rscDir.exists()) {
            log.info("rscDir.exists() {}", rscDir.exists());
            boolean res = rscDir.mkdirs();
            if (!res) {
                log.info("rscDir.mkdirs() fail");
                throw new RuntimeException();
            }
        }

        uploadFile.renameTo(rscFile);

        String resourceUri = EFS + rsc_relPath;
        return resourceUri;
    }
}
