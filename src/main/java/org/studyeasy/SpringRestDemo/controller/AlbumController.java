package org.studyeasy.SpringRestDemo.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
//import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.studyeasy.SpringRestDemo.model.Account;
import org.studyeasy.SpringRestDemo.model.Album;
import org.studyeasy.SpringRestDemo.model.Photo;
import org.studyeasy.SpringRestDemo.payloads.auth.album.AlbumPayloadDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.album.AlbumViewDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.album.PhotoPayloadDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.album.PhotoViewDTO;
import org.studyeasy.SpringRestDemo.repository.AccountRepository;
import org.studyeasy.SpringRestDemo.service.AccountService;
import org.studyeasy.SpringRestDemo.service.AlbumService;
import org.studyeasy.SpringRestDemo.service.PhotoService;
import org.studyeasy.util.AppUtils.AppUtil;
import org.studyeasy.util.constants.AlbumError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@CrossOrigin(origins = "http://localhost:3000/", maxAge = 3600)
@RequestMapping("/api/v1/albums")
@Tag(name = "Album Controller", description = "Controller for Album & Photo Management.")
@Slf4j // this is for logger
public class AlbumController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PhotoService photoService;

    static final String PHOTOS_FOLDER_NAME = "photos";
    static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    static final int THUMBNAIL_WIDTH = 300;
    
    @PostMapping(value="/add",  produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please enter a valid email and password length should be between 6 and 20 OR Account already exists")
    @ApiResponse(responseCode = "201", description = "User Account added successfully.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Add an album.")
    public ResponseEntity<AlbumViewDTO> add_album(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO, Authentication authentication){
        try {
            Album album = new Album();
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountRepository.findByEmail(email);
            Account account = optionalAccount.get();
            album.setAccount(account);
            album = albumService.save(album);
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(),album.getName(),album.getDescription(),null);
            return ResponseEntity.ok(albumViewDTO);
            
        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString()+" : "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping(value="/{album_id}/update",  consumes = "application/json",produces = "application/json")
    @ApiResponse(responseCode = "400", description = "Token issue")
    @ApiResponse(responseCode = "201", description = "Album added Successfully.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Update an album.")
    public ResponseEntity<AlbumViewDTO> updateAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO, 
    @PathVariable long album_id,Authentication authentication){
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;
            if(optionalAlbum.isPresent()){
                album = optionalAlbum.get();
                if(account.getId() != album.getAccount().getId()){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            album = albumService.save(album);
            List<PhotoViewDTO> photos = new ArrayList<>();
            for(Photo photo: photoService.findByAlbumId(album.getId())){
                String link = album.getId()+"/photos/"+photo.getId()+"download_photo";
                photos.add(new PhotoViewDTO(photo.getId(),photo.getName(),photo.getDescription(),photo.getFileName()));
            }
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(),album.getName(),album.getDescription(),photos);
            return ResponseEntity.ok(albumViewDTO);
            
        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString()+" : "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        
    }

    @PostMapping(value="/{album_id}",  produces = "application/json")
    @ApiResponse(responseCode = "400", description = "Please add valid album")
    @ApiResponse(responseCode = "201", description = "Album fetched.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Fetch album by album id.")
    public ResponseEntity<AlbumViewDTO> albumById(@PathVariable long album_id, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;
        if(optionalAlbum.isPresent()){
            album = optionalAlbum.get();
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if(account.getId() != album.getAccount().getId()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        List<PhotoViewDTO> photos = new ArrayList<>();
        for(Photo photo: photoService.findByAlbumId(album.getId())){
            String link = album.getId()+"/photos/"+photo.getId()+"download_photo";
            photos.add(new PhotoViewDTO(photo.getId(),photo.getName(),photo.getDescription(),photo.getFileName()));
        }
        AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(),album.getName(), album.getDescription(), photos);
        return ResponseEntity.ok(albumViewDTO);
    }

    @GetMapping(value="/fetch", produces = "application/json")
    @ApiResponse(responseCode = "400", description = "Token Missing")
    @ApiResponse(responseCode = "200", description = "List of Albums.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "List Album API")
    public List<AlbumViewDTO> albumList(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account account = optionalAccount.get();
        List<AlbumViewDTO> albums = new ArrayList<>();
        for(Album album: albumService.findByAccountId(account.getId())){
            List<PhotoViewDTO> photos = new ArrayList<>();
            for(Photo photo: photoService.findByAlbumId(album.getId())){
                String link = album.getId()+"/photos/{photo_id}/download_photo";
                photos.add(new PhotoViewDTO(photo.getId(),photo.getName(),photo.getDescription(),photo.getFileName()));
            }
            albums.add(new AlbumViewDTO(album.getId(),album.getName(),album.getDescription(),photos));
        }
        return albums;
    }

    @GetMapping(value="/{album_id}/fetchPhoto", produces = "application/json")
    @ApiResponse(responseCode = "400", description = "Token Missing")
    @ApiResponse(responseCode = "200", description = "List of Albums.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "List Photos API")
    public List<PhotoViewDTO> photoList(@PathVariable long album_id,Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        if(optionalAccount.isPresent()){
            List<PhotoViewDTO> photos = new ArrayList<>();
            for(Photo photo: photoService.findByAlbumId(album_id)){
                photos.add(new PhotoViewDTO(photo.getId(),photo.getName(),photo.getDescription(),photo.getFileName()));
            }
            return photos;
        }else{
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @PostMapping(value = "/{album_id}/photos", consumes = {"multipart/form-data"})
    @ApiResponse(responseCode = "400", description = "Token Missing")
    @ApiResponse(responseCode = "200", description = "Photo uploaded.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Uploads photo into album")
    public ResponseEntity<List<HashMap<String, List<?>>>> photos(@RequestPart(required = true) MultipartFile[] files, @PathVariable long album_id, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;
        if(optionalAlbum.isPresent()){
            album = optionalAlbum.get();
            if(account.getId() != album.getAccount().getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<String> fileNameWithSuccess = new ArrayList<>();
        @SuppressWarnings("unused")
        List<String> fileNameWithError = new ArrayList<>();

        Arrays.asList(files).stream().forEach(
            file -> {
                String contentType = file.getContentType();
                if(contentType.equals("image/png")
                    || contentType.equals("image/jpg")
                    || contentType.equals("image/jpeg")
                ){
                    fileNameWithSuccess.add(file.getOriginalFilename());

                    int length = 10; 
                    boolean useLetters = true;
                    boolean useNumbers = true;

                    try {
                        String fileName = file.getOriginalFilename();
                        String generatedString = RandomStringUtils.random(length,useLetters,useNumbers);
                        String final_photo_name = generatedString + fileName;
                        System.out.println("final_photo_name:"+final_photo_name);
                        String absolute_fileLocation = AppUtil.get_photo_upload_path(final_photo_name, PHOTOS_FOLDER_NAME,album_id);
                        System.out.println("absolute_fileLocation:"+absolute_fileLocation);
                        Path path = Paths.get(absolute_fileLocation);
                        System.out.println("Path of photo:"+path);
                        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                        Photo photo = new Photo();
                        photo.setName(fileName);
                        photo.setFileName(final_photo_name);
                        photo.setOriginalFileName(fileName);
                        photo.setAlbum(album);
                        photoService.save(photo);

                        BufferedImage thumbImg = AppUtil.getThumbnail(file, THUMBNAIL_WIDTH);
                        File thumbnail_location = new File(AppUtil.get_photo_upload_path(final_photo_name, THUMBNAIL_FOLDER_NAME, album_id));
                        ImageIO.write(thumbImg, file.getContentType().split("/")[1], thumbnail_location);
                    } catch (Exception e) {
                        log.debug(AlbumError.PHOTO_UPLOAD_ERROR.toString()+": "+e.getMessage());
                        fileNameWithError.add(file.getOriginalFilename());
                    }
                }else{
                    fileNameWithError.add(file.getOriginalFilename());

                }
            }
        );
        HashMap<String, List<?>> result = new HashMap<>();
        result.put("SUCCESS", fileNameWithSuccess);
        result.put("ERROR", fileNameWithError);

        List<HashMap<String, List<?>>> response = new ArrayList<>();
        response.add(result);

        return ResponseEntity.ok(response); 
    }

    @DeleteMapping(value = "/{album_id}/photos/{photo_id}/delete")
    @ApiResponse(responseCode = "400", description = "Token Missing")
    @ApiResponse(responseCode = "202", description = "Photo deleted.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Delete a photo.")
    public ResponseEntity<String> deletePhoto(@PathVariable long album_id, 
        @PathVariable long photo_id, Authentication authentication){
            try {
                String email = authentication.getName();
                Optional<Account> optionalAccount = accountService.findByEmail(email);
                Account account = optionalAccount.get();

                Optional<Album> optionalAlbum = albumService.findById(album_id);
                Album album;
                if(optionalAlbum.isPresent()){
                    album = optionalAlbum.get();
                    if(account.getId() != album.getAccount().getId()){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                }else{
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
                }
                Optional<Photo> optionalPhoto = photoService.findById(photo_id);
                if(optionalPhoto.isPresent()){
                    Photo photo = optionalPhoto.get();
                    if(photo.getAlbum().getId() != album_id){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                    AppUtil.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                    AppUtil.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                    photoService.delete(photo);
                }
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            } catch (Exception e) {
                log.debug(e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
    }

    @DeleteMapping(value = "/{album_id}/delete")
    @ApiResponse(responseCode = "400", description = "Token Missing")
    @ApiResponse(responseCode = "202", description = "Album deleted.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Delete Album.")
    public ResponseEntity<String> deleteAlbum(@PathVariable long album_id,
             Authentication authentication){
            try {
                String email = authentication.getName();
                Optional<Account> optionalAccount = accountService.findByEmail(email);
                Account account = optionalAccount.get();

                Optional<Album> optionalAlbum = albumService.findById(album_id);
                Album album;
                if(optionalAlbum.isPresent()){
                    album = optionalAlbum.get();
                    if(account.getId() != album.getAccount().getId()){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                }else{
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
                }
                for(Photo photo: photoService.findByAlbumId(album.getId())){
                    AppUtil.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                    AppUtil.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                    photoService.delete(photo);
                }
                albumService.delete(album);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            } catch (Exception e) {
                log.debug(e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
    }

    @PutMapping(value = "/{album_id}/photos/{photo_id}", consumes = "application/json",produces = "application/json")
    @ApiResponse(responseCode = "400", description = "Token Missing")
    @ApiResponse(responseCode = "200", description = "Photo uploaded.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Update photo & details into album")
    public ResponseEntity<PhotoViewDTO> updatePhoto(@Valid @RequestBody PhotoPayloadDTO photoPayloadDTO,@PathVariable("album_id") long album_id,
        @PathVariable("photo_id") long photo_id, Authentication authentication){
            try {
                String email = authentication.getName();
                Optional<Account> optionalAccount = accountService.findByEmail(email);
                Account account = optionalAccount.get();

                Optional<Album> optionalAlbum = albumService.findById(album_id);
                Album album;

                if(optionalAlbum.isPresent()){
                    album = optionalAlbum.get();
                    if(account.getId() != album.getAccount().getId()){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                }else{
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                Optional<Photo> optionalPhoto = photoService.findById(photo_id);
                if(optionalPhoto.isPresent()){
                    Photo photo = optionalPhoto.get();
                    if(photo.getAlbum().getId() != album_id){
                        return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                    photo.setName(photoPayloadDTO.getName());
                    photo.setDescription(photoPayloadDTO.getDescription());
                    photoService.save(photo);
                    PhotoViewDTO photoViewDTO = new PhotoViewDTO(photo.getId(),photoPayloadDTO.getName(),photo.getDescription(),photo.getFileName());
                    return ResponseEntity.ok(photoViewDTO);
                }else{
                    return  ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
                }

            } catch (Exception e) {
                log.debug(e.getMessage());
                return  ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
            }
    }

    @GetMapping("/{album_id}/photos/{photo_id}/download_photo")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Download the photos from API.")
    public ResponseEntity<?> downloadPhoto(@PathVariable("album_id") long album_id,
        @PathVariable("photo_id") long photo_id, Authentication authentication) {
            return downloadFile(album_id, photo_id, PHOTOS_FOLDER_NAME, authentication);
    }

    @GetMapping("/{album_id}/photos/{photo_id}/download_thumbnail")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Download the thumbnail from API.")
    public ResponseEntity<?> downloadThumbnail(@PathVariable("album_id") long album_id,
        @PathVariable("photo_id") long photo_id, Authentication authentication) {
            return downloadFile(album_id, photo_id, THUMBNAIL_FOLDER_NAME, authentication);
    }

    public ResponseEntity<?> downloadFile(long album_id, long photo_id, String folder_name, Authentication authentication){
        String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            
            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;
            if(optionalAlbum.isPresent()){
                album = optionalAlbum.get();
                if(account.getId() != album.getAccount().getId()){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        
        Optional<Photo> optionalPhoto = photoService.findById(photo_id);
        if(optionalPhoto.isPresent()){
            Photo photo = optionalPhoto.get();
            if(photo.getAlbum().getId() != album_id){
                return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            Resource resource = null;
            try {
                resource = AppUtil.getFileAsResource(album_id, PHOTOS_FOLDER_NAME, photo.getFileName());
            } catch (IOException e) {
                return ResponseEntity.internalServerError().build();
            }

            if(resource == null){
                return new ResponseEntity<>("File not found.", HttpStatus.NOT_FOUND);
            }

            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\""+photo.getOriginalFileName()+"\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        
    }
    
}
