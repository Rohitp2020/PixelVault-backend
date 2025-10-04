package org.studyeasy.util.AppUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

public class AppUtil {
    public static String PATH = "src\\main\\resources\\static\\uploads\\";

    // This function is used to get the photo uploads path
    public static String get_photo_upload_path(String fileName, String folder_name, long album_id) throws IOException{
        String path = PATH + album_id + "\\" + folder_name;
        Files.createDirectories(Paths.get(path));
        return new File(path).getAbsolutePath() + "\\" + fileName;
    }

    public static boolean delete_photo_from_path(String fileName, String folder_name, long album_id){
        try {
            File f = new File(PATH + album_id + "\\" + folder_name + "\\" + fileName);
            if(f.delete()){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

     public static boolean delete_album_from_path(long album_id){
        try {
            File f = new File(PATH + album_id);
            if(f.delete()){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // This function is used to create thumbnails or resize the images.
    public static BufferedImage getThumbnail(MultipartFile originalFile, Integer width) throws IOException{
        BufferedImage thumbImg = null;
        BufferedImage img = ImageIO.read(originalFile.getInputStream());
        thumbImg = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
        return thumbImg;
    }

    public static Resource getFileAsResource(long album_id, String folder_name, String file_name) throws IOException{
        String location = "src\\main\\resources\\static\\uploads\\"+album_id+"\\"+folder_name+"\\"+file_name;
        File file = new File(location);
        if(file.exists()){
            Path path = Paths.get(file.getAbsolutePath());
            return new UrlResource(path.toUri());
        }else{
            return null;
        }
    }
}
