package cs321.btree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A cache is a data structure that stores key-value pairs and is structured to serve up 
 * consistently-used pages quickly. It has a maximum capacity, and when the capacity is reached, the
 * least recently used key-value pair is removed from the cache. When a key-value pair is accessed
 * (either added or retrieved), it is moved to the front of the cache, so that the most recently
 * used key-value pairs are always at the front.
 * 
 * @author Matt Youngberg
 */
public class Cache<K, V extends KeyInterface<K>> {

    /**
     * The cache is implemented as a list of values that implement the `KeyInterface`. This is an
     * interesting decision, because it means we value managing the memory footprint of the cache
     * while retaining the highest hit ratio as we can under that constraint. We maintain a high
     * hit ratio under a Gaussian distribution because lists are ordered, and by dropping the least
     * recently used key-value pair, we are more likely to drop a key-value pair that is not likely
     * to be used again soon. This is because the most recently used key-value pairs are at the
     * front of the list, and the least recently used key-value pairs are at the back of the list.
     * <p>
     * It is worth noting that using a List in this scenario suffers from lookup times, but when the
     * cache is at a reasonable capacity, it is almost certainly more efficient than doing disk I/O.
     * <p>
     * Another implementation option is using a `HashMap`, which would be more efficient for 
     * lookups, but it's harder to manage the memory footprint of the cache and maintain the same
     * hit ratio, as you lose the ability to remove the least recently used key-value pair.
     */
    private final List<V> cache;

    /**
     * The maximum number of key-value pairs that can be stored in the cache.
     */
    private final int capacity;

    /**
     * The number of times the cache has been referenced.
     */
    private int references;

    /**
     * The number of times the cache has been hit.
     */
    private int hits;

    /**
     * Construct a new `Cache` instance.
     * 
     * @param capacity  the maximum number of key-value pairs that can be stored in the cache
     */
    public Cache(int capacity) {
        // I use a LinkedList implementation as required by the assignment, but ultimately adhere
        // to the List interface so different List implementations can be swapped out and tested
        // for efficiency. Since you can't really search via index for this particular problem, I
        // believe a LinkedList is the most efficient implementation since it's constant time to add
        // and remove from the front and back, respectively (Java's is doubly linked).
        this.cache = new LinkedList<V>();
        this.capacity = capacity;
        this.references = 0;
        this.hits = 0;
    }

    /**
     * Get the value associated with the key from the cache.
     * 
     * @param key   the key to look up
     * @return      the value associated with the key, or null if not found
     */
    public V get(K key) {
        ++references;
        Iterator<V> iterator = cache.iterator();
        while (iterator.hasNext()) {
            V value = iterator.next();
            if (value.getKey().equals(key)) {
                // Move the value to the front of the cache
                ++hits;
                iterator.remove();
                cache.add(0, value);    // Won't throw ConcurrentModificationException
                return value;                 // because we return on the next line.
            }
        }
        return null;        
    }

    /**
     * Add a key-value pair to the cache.
     * 
     * @param key   the key to add
     * @param value the value to add
     * @return      the value that was removed from the cache, or null if none
     */
    public V add(K key, V value) {
        V removed = null;
        if (cache.size() == capacity) {
            // Remove the least recently used value
            removed = cache.remove(cache.size() - 1);
        }
        cache.add(0, value);
        return removed;
    }

    /**
     * Remove a key-value pair from the cache.
     * 
     * @param key   the key to remove
     * @return      the value that was removed from the cache, or null if none
     */
    public V remove(K key) {
        Iterator<V> iterator = cache.iterator();
        while (iterator.hasNext()) {
            V value = iterator.next();
            if (value.getKey().equals(key)) {
                iterator.remove();
                return value;
            }
        }
        return null;
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        cache.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        sb.append("Cache with ").append(capacity).append(" entries has been created\n");
        sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        sb.append("Total number of references:        ").append(references).append("\n");
        sb.append("Total number of cache hits:        ").append(hits).append("\n");
        sb.append("Cache hit percent:                 ");
        if (references == 0) {
            sb.append("0.00%\n");
        } 
        else {
            double hitPercent = (double) hits / references * 100;
            sb.append(String.format("%.2f", hitPercent)).append("%");
        }
        return sb.toString();
    }
}