package tslib.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Saves and restores fitted models so they don't need to be re-trained on startup.
 *
 * <p>All model classes in this library implement {@link Serializable}. Use
 * {@link #save(Serializable, String)} after fitting and {@link #load(String)} when
 * reloading from disk.
 */
public final class ModelSerializer {

    private ModelSerializer() {}

    /**
     * Serializes {@code model} to the file at {@code path}.
     *
     * @param model the fitted model to persist
     * @param path  destination file path
     * @throws IOException if the file cannot be written
     */
    public static void save(Serializable model, String path) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(model);
        }
    }

    /**
     * Deserializes a model previously saved by {@link #save}.
     *
     * @param path source file path
     * @param <T>  inferred model type
     * @return the deserialized model
     * @throws IOException            if the file cannot be read
     * @throws ClassNotFoundException if the serialized class is not on the classpath
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (T) ois.readObject();
        }
    }
}
