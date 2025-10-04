package org.studyeasy.SpringRestDemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.studyeasy.SpringRestDemo.model.Album;
import org.studyeasy.SpringRestDemo.model.Photo;
import org.studyeasy.SpringRestDemo.repository.AlbumRepository;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    public Album save(Album album){
        return albumRepository.save(album);
    }

    public List<Album> findByAccountId(long id){
        return albumRepository.findByAccountId(id);
    }

    public Optional<Album> findById(long id){
        return albumRepository.findById(id);
    }
    
    public void delete(Album album){
        albumRepository.delete(album);
    }
}
