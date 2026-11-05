package com.ellora.application.common.port;

import java.io.InputStream;

/**
 * Port interface for cloud/local image storage management (e.g. Cloudinary).
 */
public interface ImageStorage {

    String uploadImage(String folder, String filename, InputStream inputStream, long contentLength, String contentType);

    void deleteImage(String publicUrlOrId);
}
