package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Comment;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.repository.CommentRepository;
import com.mycompany.myapp.repository.PhotoRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.dto.CommentDTO;
import com.mycompany.myapp.service.mapper.CommentMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Comment}.
 */
@Service
@Transactional
public class CommentService {

    private static final Logger LOG = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;

    public CommentService(
        CommentRepository commentRepository,
        CommentMapper commentMapper,
        UserRepository userRepository,
        AlbumRepository albumRepository,
        PhotoRepository photoRepository
    ) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
    }

    /**
     * Save a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO save(CommentDTO commentDTO) {
        LOG.debug("Request to save Comment : {}", commentDTO);

        Comment comment = commentMapper.toEntity(commentDTO);

        // Set proper entity references
        if (commentDTO.getAuthor() != null && commentDTO.getAuthor().getId() != null) {
            userRepository.findById(commentDTO.getAuthor().getId()).ifPresent(comment::setAuthor);
        }

        if (commentDTO.getAlbum() != null && commentDTO.getAlbum().getId() != null) {
            albumRepository.findById(commentDTO.getAlbum().getId()).ifPresent(comment::setAlbum);
        }

        if (commentDTO.getPhoto() != null && commentDTO.getPhoto().getId() != null) {
            photoRepository.findById(commentDTO.getPhoto().getId()).ifPresent(comment::setPhoto);
        }

        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    /**
     * Update a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO update(CommentDTO commentDTO) {
        LOG.debug("Request to update Comment : {}", commentDTO);
        Comment comment = commentMapper.toEntity(commentDTO);
        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    /**
     * Partially update a comment.
     *
     * @param commentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CommentDTO> partialUpdate(CommentDTO commentDTO) {
        LOG.debug("Request to partially update Comment : {}", commentDTO);

        return commentRepository
            .findById(commentDTO.getId())
            .map(existingComment -> {
                commentMapper.partialUpdate(existingComment, commentDTO);

                return existingComment;
            })
            .map(commentRepository::save)
            .map(commentMapper::toDto);
    }

    /**
     * Get all the comments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comments");
        return commentRepository.findAll(pageable).map(commentMapper::toDto);
    }

    /**
     * Get all the comments with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CommentDTO> findAllWithEagerRelationships(Pageable pageable) {
        return commentRepository.findAllWithEagerRelationships(pageable).map(commentMapper::toDto);
    }

    /**
     * Get all comments for a specific album.
     *
     * @param albumId the id of the album.
     * @return the list of comments.
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> findByAlbumId(Long albumId) {
        LOG.debug("Request to get Comments for Album : {}", albumId);
        return commentRepository
            .findByAlbumIdOrderByCreatedDateDesc(albumId)
            .stream()
            .map(commentMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all comments for a specific photo.
     *
     * @param photoId the id of the photo.
     * @return the list of comments.
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> findByPhotoId(Long photoId) {
        LOG.debug("Request to get Comments for Photo : {}", photoId);
        return commentRepository
            .findByPhotoIdOrderByCreatedDateDesc(photoId)
            .stream()
            .map(commentMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get one comment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CommentDTO> findOne(Long id) {
        LOG.debug("Request to get Comment : {}", id);
        return commentRepository.findOneWithEagerRelationships(id).map(commentMapper::toDto);
    }

    /**
     * Delete the comment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Comment : {}", id);
        commentRepository.deleteById(id);
    }
}
