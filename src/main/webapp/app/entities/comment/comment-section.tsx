import React, { useEffect, useState } from 'react';
import { Button, Form, FormGroup, Input, Label, Card, CardBody, CardHeader } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import dayjs from 'dayjs';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntitiesByAlbum, getEntitiesByPhoto, createEntity, reset } from './comment.reducer';
import { IComment } from 'app/shared/model/comment.model';

interface CommentSectionProps {
  albumId?: number;
  photoId?: number;
  albumName?: string;
  photoTitle?: string;
}

export const CommentSection: React.FC<CommentSectionProps> = ({ albumId, photoId, albumName, photoTitle }) => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const comments = useAppSelector(state => state.comment.entities);
  const loading = useAppSelector(state => state.comment.loading);
  const updating = useAppSelector(state => state.comment.updating);
  const updateSuccess = useAppSelector(state => state.comment.updateSuccess);

  const [newComment, setNewComment] = useState('');

  useEffect(() => {
    if (albumId) {
      dispatch(getEntitiesByAlbum(albumId));
    } else if (photoId) {
      dispatch(getEntitiesByPhoto(photoId));
    }
  }, [albumId, photoId]);

  useEffect(() => {
    if (updateSuccess) {
      setNewComment('');
      dispatch(reset());
      // Reload comments after successful creation
      if (albumId) {
        dispatch(getEntitiesByAlbum(albumId));
      } else if (photoId) {
        dispatch(getEntitiesByPhoto(photoId));
      }
    }
  }, [updateSuccess]);

  const handleSubmitComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim() || !account) return;

    const comment: IComment = {
      content: newComment.trim(),
      createdDate: dayjs(),
      author: { id: account.id, login: account.login },
      ...(albumId && { album: { id: albumId } }),
      ...(photoId && { photo: { id: photoId } }),
    };

    dispatch(createEntity(comment));
  };

  const sortedComments = comments
    .filter(comment => (albumId && comment.album?.id === albumId) || (photoId && comment.photo?.id === photoId))
    .sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());

  return (
    <Card className="mt-4" data-cy="commentSection">
      <CardHeader>
        <h5 className="mb-0">
          <FontAwesomeIcon icon="comments" className="me-2" />
          <Translate contentKey="gallerySystemApp.comment.section.title">Comments</Translate> ({sortedComments.length})
        </h5>
      </CardHeader>
      <CardBody>
        {/* Comment Form */}
        {account ? (
          <Form onSubmit={handleSubmitComment} className="mb-4">
            <FormGroup>
              <Label for="newComment">
                <Translate contentKey="gallerySystemApp.comment.section.addComment">Add a comment</Translate>
              </Label>
              <Input
                type="textarea"
                id="newComment"
                value={newComment}
                onChange={e => setNewComment(e.target.value)}
                placeholder="Write your comment here..."
                rows={3}
                maxLength={1000}
                data-cy="commentInput"
                disabled={updating}
              />
              <div className="text-muted small mt-1">{newComment.length}/1000 characters</div>
            </FormGroup>
            <Button type="submit" color="primary" disabled={!newComment.trim() || updating} data-cy="submitComment">
              <FontAwesomeIcon icon="plus" className="me-1" />
              <Translate contentKey="gallerySystemApp.comment.section.submit">Submit Comment</Translate>
            </Button>
          </Form>
        ) : (
          <div className="alert alert-warning mb-4" data-cy="loginPrompt">
            <Translate contentKey="gallerySystemApp.comment.section.loginRequired">Please log in to leave a comment.</Translate>
          </div>
        )}

        {/* Comments List */}
        {loading ? (
          <div className="text-center">
            <FontAwesomeIcon icon="sync" spin />{' '}
            <Translate contentKey="gallerySystemApp.comment.section.loading">Loading comments...</Translate>
          </div>
        ) : sortedComments.length > 0 ? (
          <div className="comments-list" data-cy="commentsList">
            {sortedComments.map((comment, index) => (
              <div key={comment.id} className="comment-item border-bottom pb-3 mb-3" data-cy={`comment-${comment.id}`}>
                <div className="d-flex justify-content-between align-items-start">
                  <div className="flex-grow-1">
                    <div className="comment-header mb-2">
                      <strong className="text-primary" data-cy="commentAuthor">
                        {comment.author ? comment.author.login : 'Anonymous'}
                      </strong>
                      <small className="text-muted ms-2" data-cy="commentDate">
                        <FontAwesomeIcon icon="clock" className="me-1" />
                        <TextFormat value={comment.createdDate} type="date" format={APP_DATE_FORMAT} />
                      </small>
                    </div>
                    <div className="comment-content" data-cy="commentContent">
                      {comment.content}
                    </div>
                  </div>
                  {account && account.authorities?.includes('ROLE_ADMIN') && (
                    <Button
                      color="outline-danger"
                      size="sm"
                      className="ms-2"
                      data-cy={`deleteComment-${comment.id}`}
                      title="Delete comment (Admin only)"
                    >
                      <FontAwesomeIcon icon="trash" />
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center text-muted" data-cy="noComments">
            <FontAwesomeIcon icon="comment" className="me-2" />
            <Translate contentKey="gallerySystemApp.comment.section.noComments">
              No comments yet. Be the first to leave a comment!
            </Translate>
          </div>
        )}
      </CardBody>
    </Card>
  );
};

export default CommentSection;
