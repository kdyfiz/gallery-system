import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { ITag } from 'app/shared/model/tag.model';

export interface IAlbum {
  id?: number;
  name?: string;
  event?: string | null;
  creationDate?: dayjs.Dayjs;
  overrideDate?: dayjs.Dayjs | null;
  thumbnailContentType?: string | null;
  thumbnail?: string | null;
  user?: IUser | null;
  tags?: ITag[] | null;
}

export const defaultValue: Readonly<IAlbum> = {};
