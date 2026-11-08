package com.example.spotifyclone.model;

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

    public boolean isArtist() {
        return TYPE_ARTIST.equals(type);
    }

    public boolean isLikedSongs() {
        return TYPE_LIKED_SONGS.equals(type);
    }

    public static MediaItem likedSongs() {
        MediaItem item = new MediaItem();
        item.type = TYPE_LIKED_SONGS;
        item.id = "liked_songs";
        item.title = "Músicas favoritas";
        item.subtitle = "Playlist";
        return item;
    }

    public static MediaItem fromAlbum(Album album) {
        MediaItem item = new MediaItem();
        item.type = TYPE_ALBUM;
        item.id = album.id;
        item.title = album.name;

        if (album.artists != null && !album.artists.isEmpty()) {
            item.subtitle = "Álbum • " + album.artists.get(0).name;
        } else {
            item.subtitle = "Álbum";
        }

        if (album.images != null && !album.images.isEmpty()) {
            item.imageUrl = album.images.get(0).url;
        }

        return item;
    }

    public static MediaItem fromPlaylist(Playlist playlist) {
        MediaItem item = new MediaItem();
        item.type = TYPE_PLAYLIST;
        item.id = playlist.id;
        item.title = playlist.name;

        if (playlist.owner != null && playlist.owner.displayName != null) {
            item.subtitle = "Playlist • " + playlist.owner.displayName;
        } else {
            item.subtitle = "Playlist";
        }

        item.imageUrl = playlist.firstImageUrl();
        return item;
    }

    public static MediaItem fromArtist(Artist artist) {
        MediaItem item = new MediaItem();
        item.type = TYPE_ARTIST;
        item.id = artist.id;
        item.title = artist.name;
        item.subtitle = "Artista";

        if (artist.images != null && !artist.images.isEmpty()) {
            item.imageUrl = artist.images.get(0).url;
        }

        return item;
    }
}