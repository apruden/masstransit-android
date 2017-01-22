package com.monolito.masstransit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.SparseArray

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

import java.io.ByteArrayOutputStream


class CustomMapTileProvider(ctx: Context) : TileProvider {

    private val mOptions: DisplayImageOptions

    init {
        if (!ImageLoader.getInstance().isInited) {
            val config = ImageLoaderConfiguration.Builder(ctx)//
                    .diskCacheFileCount(1000) //
                    .diskCacheSize(50 * 1024 * 1024) //
                    .build()
            ImageLoader.getInstance().init(config)
        }

        val builder = DisplayImageOptions.Builder()
        builder.cacheInMemory(true).cacheOnDisk(true)
        mOptions = builder.build()
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        //if (hasTile(x, y, zoom)) {
        if (true) {
            val image = getTileImage(x, y, zoom)

            return if (image == null) null else Tile(TILE_WIDTH, TILE_HEIGHT, image)
        } else {
            return TileProvider.NO_TILE
        }
    }

    private fun hasTile(x: Int, y: Int, zoom: Int): Boolean {
        val b = TILE_ZOOMS.get(zoom)
        return b != null && b.left <= x && x <= b.right && b.top <= y && y <= b.bottom
    }

    fun getTileUrl(x: Int, y: Int, zoom: Int): String {
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
                strGalileo.substring(0, (x * 3 + y) % 8)
    }

    private fun getTileImage(x: Int, y: Int, zoom: Int): ByteArray? {
        val bitmap = ImageLoader.getInstance().loadImageSync(getTileUrl(x, y, zoom), mOptions) ?: return null

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        return stream.toByteArray()
    }

    companion object {
        private val TILE_WIDTH = 256
        private val TILE_HEIGHT = 256

        private val TILE_ZOOMS = object : SparseArray<Rect>() {
            init {
                put(8, Rect(135, 180, 135, 181))
                put(9, Rect(270, 361, 271, 363))
                put(10, Rect(541, 723, 543, 726))
                put(11, Rect(1082, 1447, 1086, 1452))
                put(12, Rect(2165, 2894, 2172, 2905))
                put(13, Rect(4330, 5789, 4345, 5810))
                put(14, Rect(8661, 11578, 8691, 11621))
            }
        }

        private val strGalileo = "Galileo"
    }
}