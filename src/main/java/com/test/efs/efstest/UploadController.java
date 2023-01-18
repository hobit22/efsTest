package com.test.efs.efstest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @ResponseBody
    @RequestMapping(value = "/upload_resource", method = RequestMethod.POST)
    public List<Map<String,Object>> uploadResource(@RequestPart MultipartFile[] files) throws IOException {
        return uploadService.uploadResource(files);
    }

    @PostMapping(value = "/move")
    public void moveFile(@RequestBody String url) {
        uploadService.moveFile(url);
    }
}
