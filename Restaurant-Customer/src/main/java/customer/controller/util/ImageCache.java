package customer.controller.util;

import javafx.scene.image.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image getImage(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        URL url = ImageCache.class.getResource(path);
        if (url == null) {
            url = ImageCache.class.getResource("/images/default_food.png");
        }

        if (url != null) {
            // Load in background to prevent UI freeze
            Image img = new Image(url.toExternalForm(), true);
            cache.put(path, img);
            return img;
        }

        return null;
    }
}
