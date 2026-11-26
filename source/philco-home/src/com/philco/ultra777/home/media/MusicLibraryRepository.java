package com.philco.ultra777.home.media;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.List;
public final class MusicLibraryRepository {
private MusicLibraryRepository() { }
public static List<Track> load(Context context) {
List<Track> tracks = new ArrayList<Track>();
String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE };
String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
Cursor c = null;
try {
c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
projection, selection, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
if (c != null) while (c.moveToNext()) {
String title = c.getString(1);
if (title == null) title = ""; // MediaStore pode devolver título nulo; não pode virar NPE no player
tracks.add(new Track(android.content.ContentUris.withAppendedId(
MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getLong(0)), title));
}
} finally { if (c != null) c.close(); }
return tracks;
}
}