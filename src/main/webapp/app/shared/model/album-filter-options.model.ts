export interface IAlbumFilterOptions {
  events?: string[];
  years?: number[];
  tags?: string[];
  contributors?: string[];
}

export const defaultFilterOptions: Readonly<IAlbumFilterOptions> = {
  events: [],
  years: [],
  tags: [],
  contributors: [],
};
