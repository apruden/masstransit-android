package com.monolito.masstransit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.ByteArrayOutputStream;


public class CustomMapTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;

    private final DisplayImageOptions mOptions;

    public CustomMapTileProvider(Context ctx) {
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx)//
                    .diskCacheFileCount(1000) //
                    .diskCacheSize(50 * 1024 * 1024) //
                    .build();
            ImageLoader.getInstance().init(config);
        }

        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true);
        mOptions = builder.build();
    }

    private static final SparseArray<Rect> TILE_ZOOMS = new SparseArray<Rect>() {{
        put(8,  new Rect(135,  180,  135,  181 ));
        put(9,  new Rect(270,  361,  271,  363 ));
        put(10, new Rect(541,  723,  543,  726 ));
        put(11, new Rect(1082, 1447, 1086, 1452));
        put(12, new Rect(2165, 2894, 2172, 2905));
        put(13, new Rect(4330, 5789, 4345, 5810));
        put(14, new Rect(8661, 11578, 8691, 11621));
    }};

    @Override
    public Tile getTile(int x, int y, int zoom) {
        //if (hasTile(x, y, zoom)) {
        if (true) {
            byte[] image = getTileImage(x, y, zoom);

            return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
        } else {
            return NO_TILE;
        }
    }

    private boolean hasTile(int x, int y, int zoom) {
        Rect b = TILE_ZOOMS.get(zoom);
        return b != null && (b.left <= x && x <= b.right && b.top <= y && y <= b.bottom);
    }

    private static final String strGalileo = "Galileo";

    public String getTileUrl(int x, int y, int zoom) {
        return "http://mt.google.com/vt/lyrs=m@129&" +
                "hl=" +
                "en" +
                "&src=app&x=" +
                x +
                "&y=" +
                y +
                "&z=" +
                zoom +
                "&scale=" +
                15 +
                "&s=" +
                strGalileo.substring(0, (x * 3 + y) % 8);
    }

    private byte[] getTileImage(int x, int y, int zoom) {
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(getTileUrl(x, y, zoom), mOptions);

        if (bitmap == null) {
            return null;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        return stream.toByteArray();
    }
}