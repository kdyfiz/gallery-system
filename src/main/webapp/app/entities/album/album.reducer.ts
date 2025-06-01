import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { EntityState, IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IAlbum, defaultValue } from 'app/shared/model/album.model';
import { IAlbumFilterOptions, defaultFilterOptions } from 'app/shared/model/album-filter-options.model';

interface AlbumState extends EntityState<IAlbum> {
  filterOptions: IAlbumFilterOptions;
}

const initialState: AlbumState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
  filterOptions: defaultFilterOptions,
};

const apiUrl = 'api/albums';

// Actions

export const getEntities = createAsyncThunk(
  'album/fetch_entity_list',
  async ({ page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${new Date().getTime()}`;
    return axios.get<IAlbum[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getGalleryEntities = createAsyncThunk(
  'album/fetch_gallery_entity_list',
  async ({ sortBy }: { sortBy?: string }) => {
    const requestUrl = `${apiUrl}/gallery?${sortBy ? `sortBy=${sortBy}&` : ''}cacheBuster=${new Date().getTime()}`;
    return axios.get<IAlbum[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const searchAndFilterAlbums = createAsyncThunk(
  'album/search_and_filter_albums',
  async (filters: { keyword?: string; event?: string; year?: number; tagName?: string; contributorLogin?: string; sortBy?: string }) => {
    const params = new URLSearchParams();

    if (filters.keyword) params.append('keyword', filters.keyword);
    if (filters.event) params.append('event', filters.event);
    if (filters.year) params.append('year', filters.year.toString());
    if (filters.tagName) params.append('tagName', filters.tagName);
    if (filters.contributorLogin) params.append('contributorLogin', filters.contributorLogin);
    if (filters.sortBy) params.append('sortBy', filters.sortBy);

    params.append('cacheBuster', new Date().getTime().toString());

    const requestUrl = `${apiUrl}/search?${params.toString()}`;
    return axios.get<IAlbum[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getFilterOptions = createAsyncThunk(
  'album/fetch_filter_options',
  async () => {
    const requestUrl = `${apiUrl}/filter-options?cacheBuster=${new Date().getTime()}`;
    return axios.get<IAlbumFilterOptions>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'album/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IAlbum>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
  'album/create_entity',
  async (entity: IAlbum, thunkAPI) => {
    const result = await axios.post<IAlbum>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
  'album/update_entity',
  async (entity: IAlbum, thunkAPI) => {
    const result = await axios.put<IAlbum>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
  'album/partial_update_entity',
  async (entity: IAlbum, thunkAPI) => {
    const result = await axios.patch<IAlbum>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
  'album/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IAlbum>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

// slice

export const AlbumSlice = createEntitySlice({
  name: 'album',
  initialState,
  reducers: {
    setFilterOptions(state, action) {
      (state as any).filterOptions = action.payload;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addCase(getFilterOptions.fulfilled, (state, action) => {
        state.loading = false;
        AlbumSlice.caseReducers.setFilterOptions(state as any, { payload: action.payload.data, type: 'setFilterOptions' });
      })
      .addMatcher(isFulfilled(getEntities, getGalleryEntities, searchAndFilterAlbums), (state, action) => {
        const { data, headers } = action.payload;

        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity, getGalleryEntities, searchAndFilterAlbums, getFilterOptions), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset, setFilterOptions } = AlbumSlice.actions;

// Reducer
export default AlbumSlice.reducer;
