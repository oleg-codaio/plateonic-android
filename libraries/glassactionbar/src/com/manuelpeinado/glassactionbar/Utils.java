package com.manuelpeinado.glassactionbar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static Bitmap drawViewToBitmap(Bitmap dest, View view, int width, int height, int downSampling, Drawable drawable) {
        Log.d("Utils", "drawViewToBitmap() " + dest + "; " + view + ": w: " + width + ", h: " + height);
        if (view instanceof GridView) {
            return getWholeGridViewItemsToBitmap((GridView) view, downSampling);
        }

        float scale = 1f / downSampling;
        int heightCopy = view.getHeight();
        view.layout(0, 0, width, height);
        int bmpWidth = (int) (width * scale);
        int bmpHeight = (int) (height * scale);
        if (dest == null || dest.getWidth() != bmpWidth || dest.getHeight() != bmpHeight) {
            dest = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        }
        Canvas c = new Canvas(dest);
        drawable.setBounds(new Rect(0, 0, width, height));
        drawable.draw(c);
        if (downSampling > 1) {
            c.scale(scale, scale);
        }
        view.draw(c);
        view.layout(0, 0, width, heightCopy);
        // saveToSdCard(original, "original.png");
        return dest;
    }

    // adapted from http://stackoverflow.com/questions/12742343/android-get-screenshot-of-all-listview-items/12956881#12956881
    // to work for listview
    public static Bitmap getWholeGridViewItemsToBitmap(GridView gridView, int downSampling) {

        ListAdapter adapter = gridView.getAdapter();
        int itemscount = gridView.getCount();
        int allitemsheight = 0;
        List<Bitmap> bmps = new ArrayList<Bitmap>();

        for (int i = 0; i < itemscount; i++) {
            int row = i / gridView.getNumColumns();

            View childView = adapter.getView(i, null, gridView);
            childView.measure(View.MeasureSpec.makeMeasureSpec(gridView.getWidth() / gridView.getNumColumns(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
            childView.setDrawingCacheEnabled(true);
            childView.buildDrawingCache();
            bmps.add(childView.getDrawingCache());
            if (i % gridView.getNumColumns() == 0)
                allitemsheight += childView.getMeasuredHeight();
        }

        float scale = 1f / downSampling;
        Bitmap bigbitmap = Bitmap.createBitmap((int) (gridView.getMeasuredWidth() * scale),
                (int) (allitemsheight * scale), Bitmap.Config.ARGB_8888);
        Canvas bigcanvas = new Canvas(bigbitmap);

        Paint paint = new Paint();
        int iHeight = 0;

        for (int i = 0; i < bmps.size(); i++) {
            Bitmap bmp = bmps.get(i);
            Rect src = new Rect(gridView.getMeasuredWidth() * (i % gridView.getNumColumns()) / gridView.getNumColumns(), iHeight, bmp.getWidth(), bmp.getHeight());
            RectF dst = new RectF(src.left * scale, src.top * scale, src.right * scale, src.bottom * scale);
            bigcanvas.drawBitmap(bmp, src, dst, paint);
            if (i % gridView.getNumColumns() == gridView.getNumColumns() - 1)
                iHeight += bmp.getHeight();

            bmp.recycle();
            bmp = null;
        }

        return bigbitmap;
    }

    public static void saveToSdCard(Bitmap bmp, String fileName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

            //you can create a new file name "test.jpg" in sdcard folder.
            File f = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
            f.createNewFile();
            //write the bytes in file
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            // remember close de FileOutput
            fo.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
