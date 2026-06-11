package net.mikaelzero.mojito.view.sketch;

import android.net.Uri;
import android.view.View;

import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory;
import net.mikaelzero.mojito.loader.ContentLoader;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.SketchImageView;
import net.mikaelzero.mojito.view.sketch.core.display.TransitionImageDisplayer;

import org.jetbrains.annotations.NotNull;

import java.io.File;


import android.graphics.drawable.Drawable;
import pl.droidsonroids.gif.GifDrawable;

public class SketchImageLoadFactory implements ImageViewLoadFactory {
    @Override
    public void loadSillContent(@NotNull View view, @NotNull Uri uri) {
        if (view instanceof SketchImageView) {
            SketchImageView sketchView = (SketchImageView) view;
            String path = uri.getPath();
            Drawable current = sketchView.getDrawable();

            net.mikaelzero.mojito.view.sketch.core.request.DisplayHelper helper = 
                    Sketch.with(view.getContext()).display(path, sketchView);

            // To use TransitionImageDisplayer with loadingImage on MATCH_PARENT views, 
            // we MUST provide a ShapeSize.
            // We use the original image's bounds to prevent cropping.
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(path, options);
            
            if (options.outWidth > 0 && options.outHeight > 0) {
                helper.shapeSize(options.outWidth, options.outHeight);
            }

            if (current != null && !(current instanceof pl.droidsonroids.gif.GifDrawable)) {
                helper.displayer(new TransitionImageDisplayer());
                // Use current drawable as placeholder to eliminate black flash
                helper.loadingImage((context, imageView, displayOptions) -> current);
            }

            helper.commit();

            sketchView.post(() -> {
                if (sketchView.getZoomer() != null) {
                    sketchView.getZoomer().reset("postLoad");
                }
            });
        }
    }

    @Override
    public void loadContentFail(@NotNull View view, int drawableResId) {
        if (view instanceof SketchImageView) {
            ((SketchImageView) view).displayResourceImage(drawableResId);
        }
    }

    @NotNull
    @Override
    public ContentLoader newContentLoader() {
        return new SketchContentLoaderImpl();
    }
}
