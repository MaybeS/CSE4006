import collections.concurrent.BinaryTree;
import collections.concurrent.RWBinaryTree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParBSTMain {
    private static collections.interfaces.Tree<Integer> tree;
    private static collections.interfaces.Tree<Integer> rwTree;
    private static concurrent.Pool pool;

    private static final int[] threadSizes = new int[]{ 1, 2, 4, 8 };
    private static final int[] searchRatio = new int[]{ 1, 4, 9 };
    private static final int testSize = 1000000;
    private static List<Integer> numbers;

    public static void main(String[] args) {
        numbers = IntStream.range(0, testSize).boxed().collect(Collectors.toList());
        Collections.shuffle(numbers);

        for (int threadSize : threadSizes) {
            tree = new BinaryTree<>();
            rwTree = new RWBinaryTree<>();
            pool = new concurrent.Pool(threadSize);

            System.out.println(String.format("Test Start with Thread %d", threadSize));
            try {
                testA();
                testB(threadSize);
                testC(threadSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static long executeWithTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - start;
    }

    private static boolean assertRatio(int t, int f, int v) {
        return (v % (t + f)) < t;
    }

    static void testA() throws Exception {
        System.out.println(String.format("TestA: Insert %d numbers", numbers.size()));
        long time = executeWithTime(() -> {
            numbers.forEach((e) -> pool.push(() -> tree.insert(e)));
            pool.join();
        });
        System.out.println(String.format("\tInserting %d numbers takes %dms", numbers.size(), time));
    }


    static void testB(int threadSize) throws Exception {
        long insertTime = executeWithTime(() -> {
            numbers.forEach((e) -> pool.push(() -> tree.insert(e)));
            pool.join();
        });
        System.out.println(String.format("TestB: Insert and Search %d with ratio", numbers.size()));
        for (int ratio : searchRatio) {
            long time = executeWithTime(() -> {
                pool = new concurrent.Pool(threadSize);
                numbers.forEach((e) -> {
                    if (assertRatio(1, ratio, e)) pool.push(() -> tree.insert(e));
                    else pool.push(() -> tree.search(e));
                });
                pool.join();
            });
            System.out.println(String.format("\tInsert and Search ratio 1:%d, %d numbers takes %dms", ratio, numbers.size(), time));
        }
    }

    static void testC(int threadSize) throws Exception {
        long insertTime = executeWithTime(() -> {
            numbers.forEach((e) -> pool.push(() -> rwTree.insert(e)));
            pool.join();
        });
        System.out.println(String.format("TestC: Insert and Search RWLock %d with ratio", numbers.size()));
        for (int ratio : searchRatio) {
            long time = executeWithTime(() -> {
                pool = new concurrent.Pool(threadSize);
                numbers.forEach((e) -> {
                    if (assertRatio(1, ratio, e)) pool.push(() -> rwTree.insert(e));
                    else pool.push(() -> rwTree.search(e));
                });
                pool.join();
            });
            System.out.println(String.format("\tInsert and Search ratio 1:%d, %d numbers takes %dms", ratio, numbers.size(), time));
        }
    }
}
