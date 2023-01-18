package com.test.efs.efstest;

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
public class UploadService {

    public static final String SHARED = "/resource/static/shared";
    public static final String EFS = "/efs/upload";

    public List<Map<String, Object>> uploadResource(MultipartFile[] files) throws IOException {
        String currentDateString = getCurrentDate();

        File targetDir = new File(SHARED, currentDateString);
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) throw new RuntimeException();
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

        String rsc_relPath = "/" + uploadFile.getName().substring(0, 2) +
                "/" + uploadFile.getName().substring(2, 4) +
                "/" + uploadFile.getName();
        File rscFile = new File(EFS + rsc_relPath);

        File rscDir = rscFile.getParentFile();
        rscDir.getParentFile().setWritable(true, false);
        if (!rscDir.exists()) {
            boolean res = rscDir.mkdirs();
            if (!res) throw new RuntimeException();
        }

        uploadFile.renameTo(rscFile);

        String resourceUri = EFS + rsc_relPath;
        return resourceUri;
    }
}
