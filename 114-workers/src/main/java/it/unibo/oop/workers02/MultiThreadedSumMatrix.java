package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix{

    private final int numThread;

    public MultiThreadedSumMatrix(final int numThread) {
        if (numThread < 1) {
            throw new IllegalArgumentException();
        }
        this.numThread = numThread;
    }

    private static final class Worker extends Thread {
        private double result;
        private final int numElem;
        private final int posToStart;
        private final double[][] matrix;

        public Worker(double[][] matrix, int numElem, int posToStart) {
            this.matrix = matrix;
            this.numElem = numElem;
            this.posToStart = posToStart;
        }

        @Override
        public void run() {
            for (int i = posToStart; i < posToStart + numElem && i < matrix.length + numElem; i++) {
                for (double j : matrix[i]) {
                    result = result + j;
                }
            }
            super.run();
        }

        public double getResult() {
            return this.result;
        }
    }

    @Override
    public double sum(double[][] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        // calcolo il numero di righe necessarie per ogni threads
        int size = (matrix.length + numThread - 1) / numThread;
        List <Worker> workers = new ArrayList<>();

        // creo i vari threads
        for (int pos = 0; pos < matrix.length; pos = pos + size) {
            workers.add(new Worker(matrix, size, pos));
        }

        // avvio dei vari thread
        for (Thread worker : workers) {
            worker.start();
        }

        double totSum = 0;

        for (Worker worker : workers) {
            try {
                worker.join(); 
                // quando chiamo worker.join() il thread che esegue questa chiamata si sospende 
                // e attende che il thread worker termini la sua esecuzione
                totSum = totSum + worker.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Thread stopped");
            }
        }
        return totSum;
    }
}
