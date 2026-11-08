package com.example.spotifyclone.model;

/**
 * Modelo "visual" usado nas grelhas e linhas da Home/Library/Search.
 * Permite misturar álbuns, playlists, artistas e o botão Liked Songs na mesma lista.
 */
public class MediaItem {
    public static final String TYPE_ALBUM = "album";
    public static final String TYPE_PLAYLIST = "playlist";
    public static final String TYPE_ARTIST = "artist";
    public static final String TYPE_LIKED_SONGS = "liked_songs";

    public String type;
    public String id;
    public String title;
    public String subtitle;
    public String imageUrl;

    public boolean isArtist() { return TYPE_ARTIST.equals(type); }
    public boolean isLikedSongs() { return TYPE_LIKED_SONGS.equals(type); }

    public static MediaItem likedSongs() {
        MediaItem m = new MediaItem();
        m.type = TYPE_LIKED_SONGS;
        m.id = "liked_songs";
        m.title = "Liked Songs";
        m.subtitle = "Playlist";
        return m;
    }

    public static MediaItem fromAlbum(Album album) {
        MediaItem m = new MediaItem();
        m.type = TYPE_ALBUM;
        m.id = album.id;
        m.title = album.name;
        m.subtitle = (album.artists != null && !album.artists.isEmpty())
                ? "Album • " + album.artists.get(0).name : "Album";
        if (album.images != null && !album.images.isEmpty()) m.imageUrl = album.images.get(0).url;
        return m;
    }

    public static MediaItem fromPlaylist(Playlist playlist) {
        MediaItem m = new MediaItem();
        m.type = TYPE_PLAYLIST;
        m.id = playlist.id;
        m.title = playlist.name;
        m.subtitle = (playlist.owner != null && playlist.owner.displayName != null)
                ? "Playlist • " + playlist.owner.displayName : "Playlist";
        m.imageUrl = playlist.firstImageUrl();
        return m;
    }

    public static MediaItem fromArtist(Artist artist) {
        MediaItem m = new MediaItem();
        m.type = TYPE_ARTIST;
        m.id = artist.id;
        m.title = artist.name;
        m.subtitle = "Artist";
        if (artist.images != null && !artist.images.isEmpty()) m.imageUrl = artist.images.get(0).url;
        return m;
    }
}
