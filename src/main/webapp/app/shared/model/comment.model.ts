import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { IAlbum } from 'app/shared/model/album.model';
import { IPhoto } from 'app/shared/model/photo.model';

export interface IComment {
  id?: number;
  content?: string;
  createdDate?: dayjs.Dayjs;
  author?: IUser | null;
  album?: IAlbum | null;
  photo?: IPhoto | null;
}

export const defaultValue: Readonly<IComment> = {};
