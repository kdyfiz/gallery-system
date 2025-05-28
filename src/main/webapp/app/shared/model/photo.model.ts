import dayjs from 'dayjs';
import { IAlbum } from 'app/shared/model/album.model';
import { ITag } from 'app/shared/model/tag.model';

export interface IPhoto {
  id?: number;
  title?: string | null;
  description?: string | null;
  imageContentType?: string;
  image?: string;
  uploadDate?: dayjs.Dayjs;
  captureDate?: dayjs.Dayjs | null;
  location?: string | null;
  keywords?: string | null;
  album?: IAlbum | null;
  tags?: ITag[] | null;
}

export const defaultValue: Readonly<IPhoto> = {};
