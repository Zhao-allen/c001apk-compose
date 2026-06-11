package net.mikaelzero.mojito.view.sketch;

import android.net.Uri;
import android.view.View;

import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory;
import net.mikaelzero.mojito.loader.ContentLoader;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.SketchImageView;

import org.jetbrains.annotations.NotNull;


public class SketchImageLoadFactory implements ImageViewLoadFactory {
    @Override
    public void loadSillContent(@NotNull View view, @NotNull Uri uri) {
        if (view instanceof SketchImageView) {
            SketchImageView sketchView = (SketchImageView) view;
            String path = uri.getPath();

            // Minimal display call to identify if the placeholder logic was causing rendering artifacts.
            Sketch.with(view.getContext()).display(path, sketchView).commit();

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
