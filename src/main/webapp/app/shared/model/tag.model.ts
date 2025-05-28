import { IAlbum } from 'app/shared/model/album.model';
import { IPhoto } from 'app/shared/model/photo.model';

export interface ITag {
  id?: number;
  name?: string;
  albums?: IAlbum[] | null;
  photos?: IPhoto[] | null;
}

export const defaultValue: Readonly<ITag> = {};
