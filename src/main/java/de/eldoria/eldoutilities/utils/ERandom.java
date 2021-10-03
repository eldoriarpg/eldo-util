package de.eldoria.eldoutilities.utils;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class ERandom {
    private ERandom() {
    }

    /**
     * Get a random vector with a gaussian random number.
     *
     * @param count count of random vectors
     * @return list with random vectors
     */
    public static Collection<Vector> getRandomVector(int count) {
        List<Vector> vecs = new ArrayList<>();
        ThreadLocalRandom current = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            vecs.add(getRandomVector(current));
        }
        return vecs;
    }


    public static Vector getRandomVector() {
        return getRandomVector(ThreadLocalRandom.current());
    }

    /**
     * Get a random vector with a gaussian random number.
     *
     * @param random random instance
     * @return new random vector
     */
    public static Vector getRandomVector(Random random) {
        while (true) {
            double x = random.nextGaussian();
            double y = random.nextGaussian();
            double z = random.nextGaussian();
            if (x + y + z != 0) {
                return new Vector(x, y, z).normalize();
            }
        }
    }
}
