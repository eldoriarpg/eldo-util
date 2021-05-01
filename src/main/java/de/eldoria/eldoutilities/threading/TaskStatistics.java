package de.eldoria.eldoutilities.threading;

/**
 * Statistics used in {@link IteratingTask}.
 *
 * @since 1.0.0
 */
public class TaskStatistics {
    private int processedElements;
    private long time;

    public void processElement() {
        this.processedElements++;
    }

    public void addTime(long time) {
        this.time += time;
    }

    public int getProcessedElements() {
        return processedElements;
    }

    public long getTime() {
        return time;
    }
}