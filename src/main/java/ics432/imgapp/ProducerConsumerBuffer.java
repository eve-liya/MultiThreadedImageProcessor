package ics432.imgapp;

import java.util.ArrayDeque;

public class ProducerConsumerBuffer<E> {

    final Object not_empty;
    final Object not_full;
    final ArrayDeque<E> buffer;
    final int bufferSize;

    ProducerConsumerBuffer(int bufferSize) {
        buffer = new ArrayDeque<>();
        not_empty = new Object();
        not_full = new Object();
        this.bufferSize = bufferSize;
    }

    void produce(E element) {
        synchronized (not_full) {
            while(buffer.size() >= bufferSize) {
                try {
                    not_full.wait();
                } catch (InterruptedException ignore) {}
            }
            synchronized (this) {
                buffer.offer(element);
            }
        }
        synchronized (not_empty) {
            not_empty.notify();
        }
    }

    E consume() {
        E element;
        synchronized (not_empty) {
            while(buffer.isEmpty()) {
                try {
                    not_empty.wait();
                } catch (InterruptedException ignore) {}
            }
            synchronized (this) {
                element = buffer.poll();
            }
        }
        synchronized (not_full) {
            not_full.notify();
        }
        return element;
    }

}
