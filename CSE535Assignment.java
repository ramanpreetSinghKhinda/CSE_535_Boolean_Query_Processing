
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ramanpreet Singh Khinda
 * @category CSE 535 IR Programming Assignment
 *
 * @code Boolean Query Processing based on Postings Lists
 *
 **/

public class CSE535Assignment {
	private static String termIndexFileName;
	private static String outputLogFileName;
	private static String inputQueryTermsFileName;

	private static int topKTerms, numOfComparisonsTaatAnd, numOfComparisonsTaatOr, numOfComparisonsDaatAnd,
			numOfComparisonsDaatOr;
	private static boolean isTermsNotFound = false;

	// Using HashMap for storing Dictionary and Posting
	private static HashMap<Dictionary, LinkedList<Posting>> taatIndexMap, daatIndexMap;

	private static Map<Dictionary, LinkedList<Posting>> sortedIndexMap;
	private static BufferedWriter bufferFileWriter;

	/**
	 * @param args
	 * @see take 4 arguments
	 * 
	 * @code CSE535Assignment term.idx output.log 10 query_file.txt
	 * 
	 */
	public static void main(String[] args) {
		termIndexFileName = args[0];
		outputLogFileName = args[1];
		topKTerms = Integer.parseInt(args[2]);
		inputQueryTermsFileName = args[3];

		try {
			FileReader termIndexFile = new FileReader(termIndexFileName);
			BufferedReader bufferIndexFileReader = new BufferedReader(termIndexFile);

			FileReader inputQueryTermsFile = new FileReader(inputQueryTermsFileName);
			BufferedReader bufferQueryFileReader = new BufferedReader(inputQueryTermsFile);

			File output = new File(outputLogFileName);
			if (!output.exists()) {
				output.createNewFile();
			}

			FileWriter outputFile = new FileWriter(output);
			bufferFileWriter = new BufferedWriter(outputFile);

			// generate index files
			generateIndex(bufferIndexFileReader);

			// retrieve the top K largest postings lists
			getTopK(sortedIndexMap, topKTerms);

			// executes other required functions as part of the assignment
			executeFunctions(bufferQueryFileReader);

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			// catch for Generic Exceptions to avoid FATAL errors while running
			// code
			ex.printStackTrace();
		} finally {
			try {
				// closing the file writer
				bufferFileWriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param br
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code Generates the index files based on - decreasing termFreq (for TAAT
	 *       implementation), increasing docId (for DAAT implementation),
	 *       increasing posting size (for retrieving top K terms)
	 */
	private static void generateIndex(BufferedReader br) throws FileNotFoundException, IOException, Exception {
		String currentPosting;

		/*
		 * The HashMap will allow for O(1) retrieval. Created two separate
		 * classes Dictionary and Posting and used as Key Value pairs
		 * 
		 */
		daatIndexMap = new HashMap<Dictionary, LinkedList<Posting>>();
		taatIndexMap = new HashMap<Dictionary, LinkedList<Posting>>();

		while ((currentPosting = br.readLine()) != null) {
			Dictionary dictionaryItem = getDictionaryItem(currentPosting);

			LinkedList<Posting> taatPostingLinkedList = sortByTermFrequency(currentPosting);
			LinkedList<Posting> daatPostingLinkedList = sortByDocId(currentPosting);

			taatIndexMap.put(dictionaryItem, taatPostingLinkedList);
			daatIndexMap.put(dictionaryItem, daatPostingLinkedList);
		}

		/*
		 * Generating sorted Index by using custom comparator which sorts the
		 * terms based of increasing Posting size
		 *
		 */
		sortedIndexMap = myPostingSizeComparatorSort(daatIndexMap);

		// printIndex(taatIndexMap, "taatIndexMap");
		// printIndex(daatIndexMap, "daatIndexMap");
		// printIndex(sortedIndexMap, "sortedIndexMap");
	}

	/**
	 * @param currentPosting
	 * @return mDictionaryItem
	 *
	 * @code helper method for getting term and size of posting for the given
	 *       posting list. Returns a Dictionary object.
	 */
	private static Dictionary getDictionaryItem(String currentPosting) {
		Dictionary mDictionaryItem = new Dictionary();

		String[] posting = currentPosting.split("\\\\");
		int sizeOfPosting = Integer.parseInt(posting[1].replaceAll("[^0-9]", ""));

		mDictionaryItem.setTerm(posting[0]);
		mDictionaryItem.setSizeOfPosting(sizeOfPosting);

		return mDictionaryItem;
	}

	/**
	 * @param currentPosting
	 * @return mPostingLinkedList
	 * 
	 * @code helper method for sorting the posting list based on increasing
	 *       docId. Returns the Linked List of sorted Posting List.
	 * 
	 */
	private static LinkedList<Posting> sortByDocId(String currentPosting) {
		LinkedList<Posting> mPostingLinkedList = new LinkedList<Posting>();

		String[] posting = currentPosting.split("\\\\");
		String[] postingList = posting[2].split("[^0-9]+");
		int sizeOfPosting = Integer.parseInt(posting[1].replaceAll("[^0-9]", ""));

		HashMap<Integer, Integer> unSortedMap = new HashMap<Integer, Integer>();

		int counter = 0;
		while (counter < (2 * sizeOfPosting)) {
			unSortedMap.put(Integer.parseInt(postingList[++counter]), Integer.parseInt(postingList[++counter]));
		}

		Map<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>(unSortedMap);

		for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
			Integer documentId = entry.getKey();
			Integer termFrequency = entry.getValue();

			mPostingLinkedList.add(new Posting(documentId, termFrequency));
		}

		return mPostingLinkedList;
	}

	/**
	 * @param currentPosting
	 * @return mPostingLinkedList
	 * 
	 * @code helper method for sorting the posting list based on decreasing
	 *       termFrequency. Returns the Linked List of sorted Posting List.
	 * 
	 */
	private static LinkedList<Posting> sortByTermFrequency(String currentPosting) {
		LinkedList<Posting> mPostingLinkedList = new LinkedList<Posting>();

		String[] posting = currentPosting.split("\\\\");
		String[] postingList = posting[2].split("[^0-9]+");
		int sizeOfPosting = Integer.parseInt(posting[1].replaceAll("[^0-9]", ""));

		HashMap<Integer, Integer> unSortedMap = new HashMap<Integer, Integer>();

		int counter = 0;
		while (counter < (2 * sizeOfPosting)) {
			unSortedMap.put(Integer.parseInt(postingList[++counter]), Integer.parseInt(postingList[++counter]));
		}

		Map<Integer, Integer> sortedMap = myTermFrequencyComparatorSort(unSortedMap);

		for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
			Integer documentId = entry.getKey();
			Integer termFrequency = entry.getValue();

			mPostingLinkedList.add(new Posting(documentId, termFrequency));
		}

		return mPostingLinkedList;
	}

	/**
	 * @param indexMap
	 * @param topKTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code retrieves the key dictionary terms from the SortedIndexMap that
	 *       have the K largest postings lists
	 */
	private static void getTopK(Map<Dictionary, LinkedList<Posting>> indexMap, int topKTerms)
			throws IOException, Exception {
		bufferFileWriter.write("FUNCTION: getTopK " + topKTerms + "\n" + "Result: ");

		for (Map.Entry<Dictionary, LinkedList<Posting>> entry : indexMap.entrySet()) {
			String mTerm = entry.getKey().term;
			bufferFileWriter.write(mTerm);

			--topKTerms;
			if (topKTerms > 0) {
				bufferFileWriter.write(", ");
			}

			if (topKTerms == 0) {
				return;
			}
		}
	}

	/**
	 * @param br
	 * @param topKTerms
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code starting point for the execution of remaining functions
	 * 
	 */
	private static void executeFunctions(BufferedReader br) throws FileNotFoundException, IOException, Exception {
		String queryLine;

		while ((queryLine = br.readLine()) != null) {
			getPostings(queryLine);
			termAtATimeQueryAnd(queryLine);
			termAtATimeQueryOR(queryLine);
			docAtATimeQueryAnd(queryLine);
			docAtATimeQueryOr(queryLine);
		}
	}

	/**
	 * @param queryLine
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code Parses the queryLine to get individual query terms. Retrieve the
	 *       postings list for the each query term, ordered by both docId and
	 *       termFrequency
	 * 
	 */
	private static void getPostings(String queryLine) throws IOException, Exception {
		String splitQueryTerms[] = queryLine.trim().split(" ");

		int noOfQueryTerms = splitQueryTerms.length;
		int count = 0;

		isTermsNotFound = false;

		while (count < noOfQueryTerms) {
			bufferFileWriter.write("\nFUNCTION: getPostings " + splitQueryTerms[count]);
			Dictionary dic = new Dictionary(splitQueryTerms[count], -1);

			if (daatIndexMap.containsKey(dic) && taatIndexMap.containsKey(dic)) {
				/*
				 * Retrieving Postings Ordered by increasing document ID
				 */
				bufferFileWriter.write("\nOrdered by doc IDs: ");

				LinkedList<Posting> daatPostingList = daatIndexMap.get(dic);

				int daatPostingSize = daatPostingList.size();
				int daatCount = 0;

				while (daatCount < daatPostingSize) {
					bufferFileWriter.write(String.valueOf(daatPostingList.get(daatCount).documentId));
					++daatCount;

					if (daatCount < daatPostingSize) {
						bufferFileWriter.write(", ");
					}
				}

				/*
				 * Retrieving Postings ordered by decreasing term frequency
				 */
				bufferFileWriter.write("\nOrdered by TF: ");

				LinkedList<Posting> taatPostingList = taatIndexMap.get(dic);
				int taatPostingSize = taatPostingList.size();
				int taatCount = 0;

				while (taatCount < taatPostingSize) {
					bufferFileWriter.write(String.valueOf(taatPostingList.get(taatCount).documentId));
					++taatCount;

					if (taatCount < taatPostingSize) {
						bufferFileWriter.write(", ");
					}
				}

			} else {
				bufferFileWriter.write("\nterm not found");
			}
			++count;
		}
	}

	/**
	 * @param queryTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This method evaluates a multi-term Boolean AND query on the index
	 *       sorted by decreasing termFreq
	 * 
	 */
	private static void termAtATimeQueryAnd(String queryTerms) throws IOException, Exception {
		String splitQueryTerms[] = queryTerms.trim().split(" ");
		bufferFileWriter.write("\nFUNCTION: termAtATimeQueryAnd " + queryTerms.trim().replace(" ", ", "));
		taatQueryAnd(splitQueryTerms, false);
	}

	/**
	 * @param queryTerms
	 * @param isOptimized
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This is a helper method for performing TAAT AND on each query term.
	 *       It retrieves the posting list from taatIndexMap and calls another
	 *       function intersectTaatList() which actually performs the
	 *       intersection and sends results back which gets logged into the
	 *       output log file.
	 * 
	 *       To make use of this same function to perform TAAT AND for optimized
	 *       output. We have used a boolean isOptimized which performs specific
	 *       task when the function is called with isOptimized = true
	 * 
	 */
	private static void taatQueryAnd(String[] queryTerms, boolean isOptimized) throws IOException, Exception {
		long startTime = System.currentTimeMillis();
		numOfComparisonsTaatAnd = 0;

		Map<String, Integer> queryTermsMap = new HashMap<String, Integer>();
		LinkedList<Integer> tempResult;
		LinkedList<Posting> p1, p2;

		p1 = taatIndexMap.get(new Dictionary(queryTerms[0], -1));

		if (null != p1) {
			/*
			 * retrieving the postings from a LinkedList<Posting> into
			 * LinkedList<Integer> which contains only docId sorted by
			 * decreasing term frequency
			 */
			tempResult = initiateTempResult(p1);

			/*
			 * performing sorting of query terms on-the-fly so as to avoid
			 * further processing and sorting operation
			 * 
			 * Putting query terms in a HashMap as we are retrieving posting
			 * list so as to sort the query terms based on posting size
			 */
			if (!isOptimized) {
				queryTermsMap.put(queryTerms[0], tempResult.size());
			}

			int countOfTerms = queryTerms.length;
			int count = 1;

			while (count < countOfTerms) {
				p2 = taatIndexMap.get(new Dictionary(queryTerms[count], -1));

				if (null != p2) {
					if (!isOptimized) {
						queryTermsMap.put(queryTerms[count], p2.size());
					}

					/*
					 * calling a helper function to perform intersection of two
					 * posting lists and using the temporary result received
					 * after processing in next subsequent call until all terms
					 * are processed
					 */
					tempResult = intersectTaatList(tempResult, p2);
				} else {
					isTermsNotFound = true;
					break;
				}
				++count;
			}

			if (!isTermsNotFound) {
				if (!isOptimized) {
					bufferFileWriter.write("\n" + String.valueOf(tempResult.size()) + " documents are found");
					bufferFileWriter.write("\n" + String.valueOf(numOfComparisonsTaatAnd) + " comparisions are made");

					// sorting the query terms for optimized query
					String[] sortedQueryTerms = myPostingSizeOptimizedSort(queryTermsMap);
					taatQueryAnd(sortedQueryTerms, true);

				} else {
					long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

					bufferFileWriter.write("\n" + String.valueOf(elapsedTime) + " seconds are used");
					bufferFileWriter.write(
							"\n" + String.valueOf(numOfComparisonsTaatAnd) + " comparisons are made with optimization");
					bufferFileWriter.write("\nResult: ");

					// sorting the final result based on increasing docId which
					// will be logged into the output log file
					Collections.sort(tempResult);
					ListIterator<Integer> resultIterator = tempResult.listIterator();

					while (resultIterator.hasNext()) {
						bufferFileWriter.write(String.valueOf(resultIterator.next()));

						if (resultIterator.hasNext()) {
							bufferFileWriter.write(", ");
						}
					}
				}
			} else {
				bufferFileWriter.write("\nterms not found");
			}
		} else {
			bufferFileWriter.write("\nterms not found");
		}
	}

	/**
	 * @param p1
	 * @param p2
	 * 
	 * @throws Exception
	 * 
	 * @return result
	 * 
	 * @code This is a helper method which actually performs the intersection of
	 *       two posting lists P1 and P2. Returning the temporary result back to
	 *       calling function and the calling function then again passes the
	 *       temporary result along with next posting and this process iterates
	 *       until we reach to a solution
	 */
	public static LinkedList<Integer> intersectTaatList(LinkedList<Integer> p1, LinkedList<Posting> p2)
			throws Exception {
		LinkedList<Integer> result = new LinkedList<Integer>();
		ListIterator<Integer> P1Iterator = p1.listIterator();

		int docId;
		// System.out.print("\nIntersect Temp Result : ");

		while (P1Iterator.hasNext()) {
			docId = P1Iterator.next();

			for (int i = 0; i < p2.size(); i++) {
				++numOfComparisonsTaatAnd;

				if (docId == p2.get(i).documentId) {
					result.add(docId);
					// System.out.print(" -> " + docId);
					break;
				}
			}
		}
		// System.out.print("\nComparisons made : " + numOfComparisonsTaatAnd +
		// "\n");
		return result;
	}

	/**
	 * @param queryTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This method evaluates a multi-term Boolean OR query on the index
	 *       sorted by decreasing termFreq
	 * 
	 */
	public static void termAtATimeQueryOR(String queryTerms) throws IOException, Exception {
		String splitQueryTerms[] = queryTerms.trim().split(" ");
		bufferFileWriter.write("\nFUNCTION: termAtATimeQueryOr " + queryTerms.trim().replace(" ", ", "));
		taatQueryOr(splitQueryTerms, false);
	}

	/**
	 * @param queryTerms
	 * @param isOptimized
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This is a helper method for performing TAAT OR on each query term.
	 *       It retrieves the posting list from taatIndexMap and calls another
	 *       function unionTaatList() which actually performs the union and
	 *       sends results back which gets logged into the output log file.
	 * 
	 *       To make use of this same function to perform TAAT OR for optimized
	 *       output. We have used a boolean isOptimized which performs specific
	 *       task when the function is called with isOptimized = true
	 * 
	 */
	private static void taatQueryOr(String[] queryTerms, boolean isOptimized) throws IOException, Exception {
		long startTime = System.currentTimeMillis();
		numOfComparisonsTaatOr = 0;

		Map<String, Integer> queryTermsMap = new HashMap<String, Integer>();
		LinkedList<Integer> tempResult;
		LinkedList<Posting> p1, p2;

		int countOfTerms = queryTerms.length;
		int count = -1;

		do {
			// retrieving first non null posting which will act as a first
			// temporary
			// result
			++count;
			p1 = taatIndexMap.get(new Dictionary(queryTerms[count], -1));

		} while (p1 == null && count < (countOfTerms - 1));

		if (null != p1) {
			tempResult = initiateTempResult(p1);

			if (!isOptimized) {
				queryTermsMap.put(queryTerms[count], tempResult.size());
			}

			while (count < (countOfTerms - 1)) {
				do {
					// this is to check that the subsequent posting list for
					// the given query term are non null
					++count;
					p2 = taatIndexMap.get(new Dictionary(queryTerms[count], -1));

				} while (p2 == null && count < (countOfTerms - 1));

				if (null != p2) {
					if (!isOptimized) {
						queryTermsMap.put(queryTerms[count], p2.size());
					}
					tempResult = unionTaatList(tempResult, p2);
				} else {
					// if no posting list found for other query terms than
					// return
					// the first temporary result
					break;
				}
			}

			if (!isOptimized) {
				bufferFileWriter.write("\n" + String.valueOf(tempResult.size()) + " documents are found");
				bufferFileWriter.write("\n" + String.valueOf(numOfComparisonsTaatOr) + " comparisions are made");

				String[] sortedQueryTerms = myPostingSizeOptimizedSort(queryTermsMap);
				taatQueryOr(sortedQueryTerms, true);

			} else {
				long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
				bufferFileWriter.write("\n" + String.valueOf(elapsedTime) + " seconds are used");
				bufferFileWriter.write(
						"\n" + String.valueOf(numOfComparisonsTaatOr) + " comparisons are made with optimization");
				bufferFileWriter.write("\nResult: ");

				Collections.sort(tempResult);
				ListIterator<Integer> resultIterator = tempResult.listIterator();

				while (resultIterator.hasNext()) {
					bufferFileWriter.write(String.valueOf(resultIterator.next()));

					if (resultIterator.hasNext()) {
						bufferFileWriter.write(", ");
					}
				}
			}
		} else {
			// if none of the query terms are found in the index
			bufferFileWriter.write("\nterms not found");
		}
	}

	/**
	 * @param p1
	 * @param p2
	 * 
	 * @throws Exception
	 * 
	 * @return result
	 * 
	 * @code This is a helper method which actually performs the union of two
	 *       posting lists P1 and P2. Returning the temporary result back to
	 *       calling function and the calling function then again passes the
	 *       temporary result along with next posting and this process iterates
	 *       until we reach to a solution
	 */
	public static LinkedList<Integer> unionTaatList(LinkedList<Integer> p1, LinkedList<Posting> p2) throws Exception {
		LinkedList<Integer> result = new LinkedList<Integer>();
		ListIterator<Integer> P1Iterator = p1.listIterator();

		boolean isFound = false;
		int docId;

		// System.out.print("\nUnion Temp Result : ");
		while (P1Iterator.hasNext()) {
			docId = P1Iterator.next();
			isFound = false;

			for (int i = 0; i < p2.size(); i++) {
				++numOfComparisonsTaatOr;

				if (docId == p2.get(i).documentId) {
					// the same document found in 2nd list as well so skip this
					// doc for now because it will be added when we combine the
					// 2nd list
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				result.add(docId);
				// System.out.print(" -> " + docId);
			}
		}

		for (int i = 0; i < p2.size(); i++) {
			result.add(p2.get(i).documentId);
			// System.out.print(" -> " + p2.get(i).documentId);
		}

		// System.out.print("\nComparisons made : " + numOfComparisonsTaatOr +
		// "\n");

		return result;
	}

	/**
	 * @param queryTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This method evaluates a multi-term Boolean AND query on the index
	 *       sorted by increasing docId
	 * 
	 */
	public static void docAtATimeQueryAnd(String queryTerms) throws IOException, Exception {
		String splitQueryTerms[] = queryTerms.trim().split(" ");
		bufferFileWriter.write("\nFUNCTION: docAtATimeQueryAnd " + queryTerms.trim().replace(" ", ", "));
		daatQueryAnd(splitQueryTerms);
	}

	/**
	 * @param queryTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This is a helper method for performing DAAT AND on each query term
	 *       using . It retrieves the posting list from daatIndexMap and calls
	 *       another function intersectDaatList() which actually performs the
	 *       intersection of the posting lists of query terms.
	 * 
	 *       Here we are not passing intermediate results recursively because
	 *       DAAT functionality is different from TAAT, where the later one
	 *       performs operation on intermediate results and make use of them
	 *       during processing, the former one does parallel processing of all
	 *       docId's and sends the final result back which gets logged into the
	 *       output log file.
	 *
	 */
	private static void daatQueryAnd(String[] queryTerms) throws IOException, Exception {
		long startTime = System.currentTimeMillis();
		numOfComparisonsDaatAnd = 0;
		isTermsNotFound = false;

		LinkedList<Integer> result = null;
		LinkedList<Posting> p1 = null;

		int countOfTerms = queryTerms.length;

		if (countOfTerms == 1) {
			p1 = daatIndexMap.get(new Dictionary(queryTerms[0], -1));

			if (null != p1)
				result = initiateTempResult(p1);
			else
				isTermsNotFound = true;

		} else if (countOfTerms >= 2) {
			result = intersectDaatList(queryTerms, countOfTerms);
		} else {
			isTermsNotFound = true;
		}

		if (!(result == null || isTermsNotFound)) {
			long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

			bufferFileWriter.write("\n" + String.valueOf(result.size()) + " documents are found");
			bufferFileWriter.write("\n" + String.valueOf(numOfComparisonsDaatAnd) + " comparisions are made");
			bufferFileWriter.write("\n" + String.valueOf(elapsedTime) + " seconds are used");
			bufferFileWriter.write("\nResult: ");

			Collections.sort(result);
			ListIterator<Integer> resultIterator = result.listIterator();

			while (resultIterator.hasNext()) {
				bufferFileWriter.write(String.valueOf(resultIterator.next()));

				if (resultIterator.hasNext()) {
					bufferFileWriter.write(", ");
				}
			}

		} else {
			bufferFileWriter.write("\nterms not found");
		}
	}

	/**
	 * @param queryTerms
	 * @param countOfTerms
	 * 
	 * @throws Exception
	 * 
	 * @return result
	 * 
	 * @code This is a helper method which actually performs the intersection of
	 *       all the posting lists associated with the query terms. The posting
	 *       lists are processed parallely to perform DAAT AND operation
	 * 
	 *       Returns the final result back to calling function
	 */
	public static LinkedList<Integer> intersectDaatList(String[] queryTerms, int countOfTerms) throws Exception {
		LinkedList<Integer> result = new LinkedList<Integer>();

		/*
		 * using LinkedList of integers which will act as a stack and stores the
		 * index of the query term which have the maximum value of docId. The
		 * idea of doing this is because we are doing parallel operation on the
		 * posting lists of each query term which are sorted on the basis of
		 * increasing docId. If we get the query term which currently have the
		 * maximum docId than we can move the pointers of other postings to
		 * forward and do comparisons again to find a matching document
		 * 
		 * Reason for choosing stack because there can be multiple documents
		 * with same docId and we have to store the index of all of them, so
		 * that we can know which pointers to move and which to not move
		 */
		LinkedList<Integer> stack = new LinkedList<Integer>();

		// using ArrayList of ListIterator which acts a parallel pointers for
		// the Posting Lists
		ArrayList<ListIterator<Posting>> pointers = new ArrayList<ListIterator<Posting>>();

		LinkedList<Posting> p1 = null;

		isTermsNotFound = false;

		int docIdP1;
		int count = 0;
		while (count < countOfTerms) {
			p1 = daatIndexMap.get(new Dictionary(queryTerms[count], -1));

			if (null != p1) {
				pointers.add(p1.listIterator());
				++count;
			} else {
				isTermsNotFound = true;
				return null;
			}
		}

		int maxDocId = Integer.MIN_VALUE;
		boolean isDocFound = false;
		int docIdP2;

		while (pointers.get(0).hasNext()) {
			docIdP1 = pointers.get(0).next().documentId;

			// even if maxDocId > docIdP1 the comparison will happen so we are
			// increasing the count
			++numOfComparisonsDaatAnd;
			if (maxDocId < docIdP1) {
				maxDocId = docIdP1;
				stack.push(0);
			}

			count = 1;
			isDocFound = false;

			while (count < countOfTerms) {
				if (pointers.get(count).hasNext()) {
					docIdP2 = pointers.get(count).next().documentId;
					++numOfComparisonsDaatAnd;

					if (docIdP1 == docIdP2) {
						isDocFound = true;
						stack.push(count);
						++count;
						continue;
					} else {
						// even the above if case does not get executed but the
						// comparison (docIdP1 == docIdP2) will be performed in
						// each case hence increasing the count here also for
						// the below if condition
						++numOfComparisonsDaatAnd;
						isDocFound = false;

						if (maxDocId == docIdP2) {
							stack.push(count);
						} else {
							// even the above if case does not get executed but
							// the comparison (maxDocId == docIdP2) will be
							// performed in each case hence increasing the count
							// here also
							// for the below if condition
							++numOfComparisonsDaatAnd;

							if (maxDocId < docIdP2) {
								maxDocId = docIdP2;
								stack.clear();
								stack.push(count);
							}
						}
						++count;
					}
				} else {
					return result;
				}
			}

			if (isDocFound && stack.size() == countOfTerms) {
				result.add(docIdP1);
			} else {
				count = 0;
				isDocFound = false;

				// since we are using Java LinkedList which automatically points
				// to next element when it retrieves the current element. Hence
				// we have to move the pointers of all the postings to its
				// previous element which are in the stack of maxima
				while (count < countOfTerms) {
					if (stack.contains(count)) {
						if (pointers.get(count).hasPrevious()) {
							pointers.get(count).previous();
						}
					}
					++count;
				}
			}
			stack.clear();
			count = 0;
			maxDocId = Integer.MIN_VALUE;
		}

		return result;

	}

	/**
	 * @param queryTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This method evaluates a multi-term Boolean OR query on the index
	 *       sorted by increasing docId
	 * 
	 */
	public static void docAtATimeQueryOr(String queryTerms) throws IOException, Exception {
		String splitQueryTerms[] = queryTerms.trim().split(" ");
		bufferFileWriter.write("\nFUNCTION: docAtATimeQueryOr " + queryTerms.trim().replace(" ", ", "));
		daatQueryOr(splitQueryTerms);
	}

	/**
	 * @param queryTerms
	 * 
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @code This is a helper method for performing DAAT OR on each query term.
	 *       It retrieves the posting list from daatIndexMap and calls another
	 *       function unionDaatList() which actually performs the union of the
	 *       posting lists of query terms.
	 * 
	 *       The returned final result is logged into the output log file
	 *
	 */
	private static void daatQueryOr(String[] queryTerms) throws IOException, Exception {
		long startTime = System.currentTimeMillis();
		numOfComparisonsDaatOr = 0;
		isTermsNotFound = false;

		LinkedList<Integer> result = null;
		LinkedList<Posting> p1 = null;

		int countOfTerms = queryTerms.length;

		if (countOfTerms == 1) {
			p1 = daatIndexMap.get(new Dictionary(queryTerms[0], -1));

			if (null != p1)
				result = initiateTempResult(p1);
			else
				isTermsNotFound = true;

		} else if (countOfTerms >= 2) {
			result = unionDaatList(queryTerms, countOfTerms);
		} else {
			isTermsNotFound = true;
		}

		if (!(result == null || isTermsNotFound)) {
			long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

			bufferFileWriter.write("\n" + String.valueOf(result.size()) + " documents are found");
			bufferFileWriter.write("\n" + String.valueOf(numOfComparisonsDaatOr) + " comparisions are made");
			bufferFileWriter.write("\n" + String.valueOf(elapsedTime) + " seconds are used");
			bufferFileWriter.write("\nResult: ");

			Collections.sort(result);
			ListIterator<Integer> resultIterator = result.listIterator();

			while (resultIterator.hasNext()) {
				bufferFileWriter.write(String.valueOf(resultIterator.next()));

				if (resultIterator.hasNext()) {
					bufferFileWriter.write(", ");
				}
			}
		} else {
			bufferFileWriter.write("\nterms not found");
		}
	}

	/**
	 * @param queryTerms
	 * @param countOfTerms
	 * 
	 * @throws Exception
	 * 
	 * @return result
	 * 
	 * @code This is a helper method which actually performs the union of all
	 *       the posting lists associated with the query terms. The posting
	 *       lists are processed parallely to perform DAAT OR operation
	 * 
	 *       Returns the final result back to calling function
	 */
	public static LinkedList<Integer> unionDaatList(String[] queryTerms, int countOfTerms) throws Exception {
		ArrayList<ListIterator<Posting>> pointers = new ArrayList<ListIterator<Posting>>();
		LinkedList<Integer> result = new LinkedList<Integer>();

		/*
		 * Reason for choosing stack because there can be multiple documents
		 * with same docId and we have to store the index of all of them, so
		 * that we can know which pointers to move and which to not move
		 */
		LinkedList<Integer> stack = new LinkedList<Integer>();
		LinkedList<Posting> p1 = null;

		isTermsNotFound = false;

		int docIdP1;
		int count = 0;
		while (count < countOfTerms) {
			p1 = daatIndexMap.get(new Dictionary(queryTerms[count], -1));

			if (null != p1) {
				pointers.add(p1.listIterator());
			}
			++count;
		}

		countOfTerms = pointers.size();

		if (countOfTerms == 1) {
			result = addListToResult(pointers.get(0));
			return result;
		} else if (countOfTerms == 0) {
			isTermsNotFound = true;
			return null;
		}

		/*
		 * Here we are capturing the minimum value of docId. The idea of doing
		 * this is because we are doing parallel operation on the posting lists
		 * of each query term which are sorted on the basis of increasing docId.
		 * If we get the query term which currently have the minimum docId than
		 * we can store this minDocId to the final result ensuring that it will
		 * be added only one time
		 * 
		 */
		int minDocId = Integer.MAX_VALUE;
		int docIdP2;

		int counter = 0;

		while (counter < countOfTerms) {
			if (pointers.get(counter).hasNext()) {
				docIdP1 = pointers.get(counter).next().documentId;

				++numOfComparisonsDaatOr;
				if (minDocId > docIdP1) {
					minDocId = docIdP1;
					stack.push(counter);
				}

				count = counter + 1;

				while (count < countOfTerms) {
					if (pointers.get(count).hasNext()) {
						docIdP2 = pointers.get(count).next().documentId;
						++numOfComparisonsDaatOr;

						if (docIdP1 == docIdP2) {
							stack.push(count);
							++count;
							continue;
						} else {
							++numOfComparisonsDaatOr;
							if (minDocId == docIdP2) {
								stack.push(count);
							} else {
								++numOfComparisonsDaatOr;
								if (minDocId > docIdP2) {
									minDocId = docIdP2;
									stack.clear();
									stack.push(count);
								}
							}
							++count;
						}
					} else {
						++count;
					}
				}

				// since we are using Java LinkedList which automatically points
				// to next element when it retrieves the current element. Hence
				// we have to move the pointers of all the postings to its
				// previous element which are in the stack of manima
				if (stack.size() != (countOfTerms - counter)) {
					count = counter;
					while (count < countOfTerms) {
						if (!stack.contains(count)) {
							if (pointers.get(count).hasPrevious()) {
								if (minDocId > pointers.get(count).previous().documentId) {
									pointers.get(count).next();
								}
							}
						}
						++count;
					}
				}

				result.add(minDocId);
				count = 0;
				stack.clear();
				minDocId = Integer.MAX_VALUE;
			} else {
				++counter;
			}
		}

		return result;
	}

	/**
	 * @param p1
	 * 
	 * @throws Exception
	 * 
	 * @return result
	 * 
	 * @code This is a helper method which converts a LinkedList<Posting> to
	 *       LinkedList<Integer> containing only docId's
	 * 
	 */
	public static LinkedList<Integer> initiateTempResult(LinkedList<Posting> p1) throws Exception {
		LinkedList<Integer> result = new LinkedList<Integer>();
		ListIterator<Posting> P1Iterator = p1.listIterator();

		int docId;
		while (P1Iterator.hasNext()) {
			docId = P1Iterator.next().documentId;
			result.add(docId);
		}
		return result;
	}

	/**
	 * @param P1Iterator
	 * 
	 * @throws Exception
	 * 
	 * @return result
	 * 
	 * @code This is a helper method which converts a ListIterator<Posting> to
	 *       LinkedList<Integer> containing docId's retrieved from the Posting
	 *       iterator
	 * 
	 */
	public static LinkedList<Integer> addListToResult(ListIterator<Posting> P1Iterator) throws Exception {
		LinkedList<Integer> result = new LinkedList<Integer>();

		int docId;
		while (P1Iterator.hasNext()) {
			docId = P1Iterator.next().documentId;
			result.add(docId);
		}
		return result;
	}

	// Custom comparators for custom sort
	private static Map<Integer, Integer> myTermFrequencyComparatorSort(Map<Integer, Integer> unSortedMap) {
		List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unSortedMap.entrySet());

		// we are comparing Map Values to sort with decreasing term frequencies
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private static String[] myPostingSizeOptimizedSort(Map<String, Integer> unSortedMap) {

		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unSortedMap.entrySet());

		// we are comparing Map values (Posting Size) to sort with increasing
		// Posting size for optimized queries
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				if (o1.getValue() > o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		int count = -1;
		String sortedQueryTerms[] = new String[list.size()];
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedQueryTerms[++count] = entry.getKey();
			// System.out.println(sortedQueryTerms[count]);
		}
		return sortedQueryTerms;
	}

	private static Map<Dictionary, LinkedList<Posting>> myPostingSizeComparatorSort(
			Map<Dictionary, LinkedList<Posting>> unSortedMap) {

		List<Map.Entry<Dictionary, LinkedList<Posting>>> list = new LinkedList<Map.Entry<Dictionary, LinkedList<Posting>>>(
				unSortedMap.entrySet());

		// we are comparing Map Keys to sort with decreasing Posting size
		Collections.sort(list, new Comparator<Map.Entry<Dictionary, LinkedList<Posting>>>() {
			public int compare(Map.Entry<Dictionary, LinkedList<Posting>> o1,
					Map.Entry<Dictionary, LinkedList<Posting>> o2) {
				if (o1.getKey().getSizeOfPosting() < o2.getKey().getSizeOfPosting()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		Map<Dictionary, LinkedList<Posting>> sortedMap = new LinkedHashMap<Dictionary, LinkedList<Posting>>();
		for (Iterator<Map.Entry<Dictionary, LinkedList<Posting>>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Dictionary, LinkedList<Posting>> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	// Index map printer
	/*
	 * private static void printIndex(Map<Dictionary, LinkedList<Posting>>
	 * indexMap, String strIndexMap) { System.out.print("\n****** " +
	 * strIndexMap + " Index Start ******\n"); for (Map.Entry<Dictionary,
	 * LinkedList<Posting>> entry : indexMap.entrySet()) { String mTerm =
	 * entry.getKey().term; Integer mSizeOfPosting =
	 * entry.getKey().sizeOfPosting;
	 * 
	 * LinkedList<Posting> postingList = entry.getValue();
	 * 
	 * int size = postingList.size(); int count = 0; System.out.print("\n" +
	 * mTerm + " : " + mSizeOfPosting + " ==> [ ");
	 * 
	 * while (count < size) { System.out.print(
	 * postingList.get(count).documentId + " : " +
	 * postingList.get(count).termFrequency + " -> "); ++count; }
	 * System.out.print("\n"); } System.out.print("\n****** " + strIndexMap +
	 * " Index End ******\n"); }
	 */
}