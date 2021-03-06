package org.evaluation;

import org.apache.commons.collections.BufferOverflowException;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.operators.sort.InMemorySorter;
import org.apache.flink.runtime.operators.sort.IndexedSorter;
import org.apache.flink.runtime.operators.sort.QuickSort;
import org.apache.flink.util.MutableObjectIterator;
import org.evaluation.utils.SorterFactory;
import org.evaluation.utils.generator.RandomTuple2LongInt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;


//import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.annotations.Scope;
//import org.openjdk.jmh.annotations.State;
//import org.openjdk.jmh.annotations.Warmup;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
/**
 * Created by heytitle on 11/28/16.
 */
public class Benchmarker {


	@Benchmark
	@BenchmarkMode({Mode.SingleShotTime})
	@Warmup(iterations = Configuration.BENCHMARK_WARMUP)
	@Measurement(iterations = Configuration.BENCHMARK_ITERATION)
	public MutableObjectIterator testSort(ThreadState ts ) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		long start_time = System.nanoTime();

		ts.quickSort.sort(ts.sorter);

		long end_time = System.nanoTime();
		double difference = (end_time - start_time)/1e6;
		System.out.println(" | sort: " + difference +"ms");

		return ts.sorter.getIterator();
	}

	@State(Scope.Thread)
	public static class ThreadState {
		@Param({
			"org.apache.flink.runtime.operators.sort.NormalizedKeySorter",
//			"org.evaluation.sorter.individual.optimization.CompareUnrollLoop",
//			"org.evaluation.sorter.individual.optimization.SwapViaPutGetLong",
			"org.evaluation.sorter.individual.optimization.FindSegmentIndexViaBitwiseOperators",
			"org.evaluation.sorter.individual.optimization.EmbedQuickSortInside",
//			"org.evaluation.sorter.individual.optimization.UseLittleEndian",
//			"org.evaluation.sorter.individual.optimization.RemoveUnnecessaryBranching",
//			"org.evaluation.sorter.OptimizedSorter",
			"org.evaluation.sorter.individual.optimization.DividedByConstant",
			"org.flink3722.NormalizedKeySorter"
		})
		public String sorterClass;

		@Param({"10000", "100000", "1000000"})
		public long noRecords;

		volatile InMemorySorter sorter;
		volatile IndexedSorter quickSort;

		@Setup(Level.Invocation)
		public void writeRandomData() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
			sorter = SorterFactory.getSorter(sorterClass);
			if( sorterClass.equals("org.example.sorter.individual.optimization.EmbedQuickSortInside") ) {
				quickSort = (IndexedSorter)sorter;
			} else if( sorterClass.equals("org.flink3722.NormalizedKeySorter") ) {
				System.out.println("FLINK-3722: QuickSort");
				quickSort = new org.flink3722.QuickSort();
			} else {
				quickSort = new QuickSort();
			}

			long start_time = System.nanoTime();

			RandomTuple2LongInt generator = new RandomTuple2LongInt(Configuration.SEED);

			int num = -1;
			Tuple2<Long,Integer> record = new Tuple2<Long,Integer>();

			while (num < noRecords){
				generator.next(record);
				boolean succeed = sorter.write(record);
				if( !succeed ) {
					throw new BufferOverflowException("Buffer space is not sufficient, only " + num  + "records can be added only.");
				}
				num++;
			}

			long end_time = System.nanoTime();
			double difference = (end_time - start_time)/1e6;

			System.out.print(" setup " + noRecords + " records : " + difference +"ms");
		}
	}


//	@Benchmark
//	public long testDevisionByBitWise() {
//
//		long acc = 0;
//
//		for( int i = 0; i < 1000; i+=4 ){
//			acc = i >> 2;
//		}
//
//		return acc;
//	}
//
//	@Benchmark
//	public long testDevisionNormal() {
//
//		long acc = 0;
//
//		for( int i = 0; i < 1000; i+=4 ){
//			acc = i / 4;
//		}
//
//		return acc;
//	}
//
//	@Benchmark
//	public long testModuloByBitWise() {
//
//		long acc = 0;
//
//		for( int i = 0; i < 1000; i+=4 ){
//			acc = i & (4-1);
//		}
//
//		return acc;
//	}
//
//	@Benchmark
//	public long testModuloNormal() {
//
//		long acc = 0;
//
//		for( int i = 0; i < 1000; i+=4 ){
//			acc = i % 4;
//		}
//
//		return acc;
//	}

//	@State(Scope.Thread)
//	public static class SortState {
//
//		@Param({"true"})
//		public boolean ascOrder;
//	}
//
//	@Benchmark
//	@Warmup(iterations = 5)
//	@Measurement(iterations = 10)
//	public long testShorthandIf(SortState ts) {
//
//		long acc = 0;
//
//		for( int i = 0; i < 1000; i+=4 ){
//
//			acc = ts.ascOrder ? -i : i;
//		}
//
//		return acc;
//	}
//
//	@Benchmark
//	@Warmup(iterations = 5)
//	@Measurement(iterations = 10)
//	public long testMultiplyWithPrefix(SortState ts) {
//
//		long acc = 0;
//		int prefix = ts.ascOrder ? -1 : 1;
//
//		for( int i = 0; i < 1000; i+=4 ){
//
//			acc = prefix*i;
//		}
//
//		return acc;
//	}
}
