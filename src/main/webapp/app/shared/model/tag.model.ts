import { IAlbum } from 'app/shared/model/album.model';

export interface ITag {
  id?: number;
  name?: string;
  albums?: IAlbum[] | null;
}

export const defaultValue: Readonly<ITag> = {};
