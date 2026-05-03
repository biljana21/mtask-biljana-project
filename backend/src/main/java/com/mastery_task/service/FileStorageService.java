package com.mastery_task.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR =
            System.getProperty("user.home") + File.separator +
                    "Documents" + File.separator +
                    "document-processing" + File.separator;

    public String storeFile(MultipartFile file) throws IOException {
        File directory = new File(UPLOAD_DIR);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        String storedFilename = UUID.randomUUID() + extension;

        File targetFile = new File(UPLOAD_DIR + storedFilename);

        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(file.getInputStream(), outputStream);
        }

        return storedFilename;
    }

    public File getFile(String storedFilename) {
        return new File(UPLOAD_DIR + storedFilename);
    }

    public boolean deleteFile(String storedFilename) {
        File file = new File(UPLOAD_DIR + storedFilename);

        if (file.exists()) {
            return file.delete();
        }

        return false;
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }
}