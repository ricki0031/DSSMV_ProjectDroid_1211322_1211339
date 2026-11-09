package com.example.spotifyclone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;

public class MediaItemTest {

    private static ImageObject image(String url) {
        ImageObject image = new ImageObject();
        image.url = url;
        return image;
    }

    private static Artist artist(String name) {
        Artist artist = new Artist();
        artist.id = "artist1";
        artist.name = name;
        return artist;
    }

    @Test
    public void isArtist_isTrueOnlyForArtists() {
        MediaItem item = new MediaItem();
        item.type = MediaItem.TYPE_ARTIST;
        assertTrue(item.isArtist());

        item.type = MediaItem.TYPE_ALBUM;
        assertFalse(item.isArtist());
    }

    @Test
    public void isLikedSongs_isTrueOnlyForLikedSongs() {
        MediaItem item = new MediaItem();
        item.type = MediaItem.TYPE_LIKED_SONGS;
        assertTrue(item.isLikedSongs());

        item.type = MediaItem.TYPE_PLAYLIST;
        assertFalse(item.isLikedSongs());
    }

    @Test
    public void likedSongs_buildsTheFavouritesItem() {
        MediaItem item = MediaItem.likedSongs();

        assertEquals(MediaItem.TYPE_LIKED_SONGS, item.type);
        assertEquals("liked_songs", item.id);
        assertEquals("Músicas favoritas", item.title);
        assertEquals("Playlist", item.subtitle);
    }

    @Test
    public void fromAlbum_copiesFieldsAndFirstArtist() {
        Album album = new Album();
        album.id = "album1";
        album.name = "LOVED";
        album.artists = Collections.singletonList(artist("Parcels"));
        album.images = Collections.singletonList(image("cover"));

        MediaItem item = MediaItem.fromAlbum(album);

        assertEquals(MediaItem.TYPE_ALBUM, item.type);
        assertEquals("album1", item.id);
        assertEquals("LOVED", item.title);
        assertEquals("Álbum • Parcels", item.subtitle);
        assertEquals("cover", item.imageUrl);
    }

    @Test
    public void fromAlbum_handlesMissingArtistsAndImages() {
        MediaItem item = MediaItem.fromAlbum(new Album());

        assertEquals("Álbum", item.subtitle);
        assertNull(item.imageUrl);
    }

    @Test
    public void fromPlaylist_copiesFieldsAndOwner() {
        Playlist playlist = new Playlist();
        playlist.id = "playlist1";
        playlist.name = "Tryouts";
        playlist.owner = new UserProfile();
        playlist.owner.displayName = "Ricki";
        playlist.images = Collections.singletonList(image("cover"));

        MediaItem item = MediaItem.fromPlaylist(playlist);

        assertEquals(MediaItem.TYPE_PLAYLIST, item.type);
        assertEquals("playlist1", item.id);
        assertEquals("Tryouts", item.title);
        assertEquals("Playlist • Ricki", item.subtitle);
        assertEquals("cover", item.imageUrl);
    }

    @Test
    public void fromPlaylist_handlesMissingOwner() {
        assertEquals("Playlist", MediaItem.fromPlaylist(new Playlist()).subtitle);
    }

    @Test
    public void fromArtist_copiesFields() {
        Artist artist = artist("The Strokes");
        artist.images = Collections.singletonList(image("photo"));

        MediaItem item = MediaItem.fromArtist(artist);

        assertEquals(MediaItem.TYPE_ARTIST, item.type);
        assertEquals("artist1", item.id);
        assertEquals("The Strokes", item.title);
        assertEquals("Artista", item.subtitle);
        assertEquals("photo", item.imageUrl);
    }

    @Test
    public void fromArtist_handlesMissingImages() {
        assertNull(MediaItem.fromArtist(artist("The Strokes")).imageUrl);
    }
}
