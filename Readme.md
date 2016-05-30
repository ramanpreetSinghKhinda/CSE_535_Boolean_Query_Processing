<p align="center">Information Retrieval</br>Boolean Query Processing</br>CSE 535 - Fall 2016
==========================================================================================

<p align="center">![Img_1](https://raw.githubusercontent.com/ramanpreet1990/CSE_535_Boolean_Query_Processing/master/Resources/1.png)


Overview
------
In this programming assignment, we are given posting lists generated from the [**RCV1 news corpus**](http://about.reuters.com/researchandstandards/corpus/). We need to get familiar with the data format of the given posting lists and rebuild the index after reading in the data. [Linked List](https://en.wikipedia.org/wiki/Linked_list) should be used to store the index data in memory as the examples shown in the textbook. 

We need to construct two index with two different ordering strategies: with one strategy, the posting of each term should be ordered by increasing document IDs; with the other
strategy, the postings of each term should be ordered by decreasing term frequencies. 

After that, we are required to implement modules that return documents based on [term-at-a-time](http://web.stanford.edu/class/cs276/handouts/efficient_scoring_cs276_2013_6.pdf) (**TAAT**) with the postings list ordered by term frequencies, and [document-at-a-time](http://web.stanford.edu/class/cs276/handouts/efficient_scoring_cs276_2013_6.pdf) (**DAAT**) with the postings list ordered by doc IDs for a set of queries.


TAAT (term-at-a-time)
------
Scores for all docs computed concurrently, one query term at a time


DAAT (document-at-a-time)
------
Total score for each doc (incl all query terms) computed, before proceeding to the next


Input Data format
-----
The index is contained in file term.idx and the given postings lists can be downloaded from here: -</br>
https://github.com/ramanpreet1990/CSE_535_CSE_535_Boolean_Query_Processing/blob/master/term.idx

The data format is summarized in the following: -</br>
> • The file is a self-contained index including the dictionary. Note **there is no document dictionary**.

> • In the index file, each posting is on a separate line in the file

> • Every posting has three values: the term, the size of the posting list and the posting list itself. Each posting is of the form **X\cY\mZ** where **X** is the term, **Y** is the size of posting list and **Z** is the posting list

> • The posting list itself is expressed as **[a/b, c/d, e/f,...]**. The square brackets denote the start and end of the list. Each entry of the form **x/y** means the term occurs **'y'** times in document id **'x'**.


Detailed Requirements
-----
The following functions are implemented and the results are written into a log file. The corresponding output formats are also defined in the following: -

### • getTopK *K*
This returns the key dictionary terms that have the K largest postings lists. The result is expected to be an ordered string in the descending order of result postings, i.e., largest in the first position, and so on.

The output should be formatted as follows (K=10 for an example)
```
FUNCTION: getTopK 10

Result: term1, term2, term3…, term10 (list the terms)
```


### • getPostings *query_term*
Retrieve the postings list for the given query. Since we have N input query terms,
this function should be executed N times, and output the postings for each term from both two different ordered postings list. 

The corresponding posting list should be displayed in the following format:
```
FUNCTION: getPostings query_term

Ordered by doc IDs: 100, 200, 300… (list the document IDs ordered by increasing document IDs)

Ordered by TF: 300, 100, 200… (list the document IDs ordered by decreasing term frequencies)

NOTE: Should display “term not found” if it is not in the index.
```

### • termAtATimeQueryAnd *query_term1, …, query_termN*
This emulates an evaluation of a multi-term Boolean AND query on the index with term-at-a-time query. Note here the number of query terms could be varied. The index ordered by decreasing term frequencies should be used in this query. **Although Java has many very powerful methods that can do intersections of sets very efficiently, they are NOT allowed in this assignment**. You should process the query terms in the order in which they appear in the query. 

For example, you should process query_term1 first, then query_term2, and so on. In order to learn the essence of the term-at-a-time strategy, we have to compare every docID in one posting with every docID in the other while performing the intersection.

As a bonus (5pts) part, you can implement query optimization by re-ordering them by the increasing size of the postings. 

For example, if the sizes of postings for query_term1, query_term2 and query_term3 are 30, 20 and 50. Then in this part, you will process them in the following order: query_term2, query_term1 and query_term3.

In the output file, you should display how may documents are found, how many comparisons are made during this query and how much time it takes. The document IDs should be sorted and listed. 

For example:
```
FUNCTION: termAtATimeQueryAnd query_term1, …, query_termN

xx documents are found
yy comparisons are made
zz seconds are used
nn comparisons are made with optimization (optional bonus part)

Result: 100, 200, 300 … (list the document IDs, re-ordered by docIDs)

NOTE: Should display “terms not found” if it is not in the index.
```


### • termAtATimeQueryOr *query_term1, …, query_termN*
This emulates an evaluation of a multi-term Boolean OR query on the index with term-at-a-time query. The index ordered by decreasing term frequencies should be used in this query. All other requirements are the same with termAtATimeQueryAnd. Output
format is the same.

For example:
```
FUNCTION: termAtATimeQueryOr query_term1, …, query_termN

xx documents are found
yy comparisons are made
zz seconds are used
nn comparisons are made with optimization (optional bonus part)

Result: 300, 100, 200… (list the document IDs, re-ordered by docIDs)

NOTE: Should display “terms not found” if it is not in the index.
```


### • docAtATimeQueryAnd *query_term1, …, query_termN*
This emulates an evaluation of a multi-term Boolean AND query on the index with document-at-a-time query. The index ordered by increasing document IDs should be used in this query. Note again, Java’s build-in intersection methods are NOT allowed in this assignment. Output format is the same.

For example:
```
FUNCTION: docAtATimeQueryAnd query_term1, …, query_termN

xx documents are found
yy comparisions are made
zz seconds are used

Result: 100, 200, 300… (list the document IDs)

NOTE: Should display “terms not found” if it is not in the index.
```


### • docAtATimeQueryOr *query_term1, …, query_termN*
This emulates an evaluation of a multi-term Boolean OR query on the index with document-at-a-time query. The index ordered by increasing document IDs should be used in this query. Output format is the same.

For example:
```
FUNCTION: docAtATimeQueryOr query_term1, …, query_termN

xx documents are found
yy comparisions are made
zz seconds are used

Result: 100, 200, 300… (list the document IDs)

NOTE: Should display “terms not found” if it is not in the index.
```


References
------
I have taken reference from below sources to design the boolean query processor: -</br>
1. [Introduction to Information Retrieval](http://nlp.stanford.edu/IR-book/)</br>
2. [Course by Oresoft LWC](https://www.youtube.com/watch?v=q0srNT_XM_Y&list=PL0ZVw5-GryEkGAQT7lX7oIHqyDPeUyOMQ)</br>


Credits
-------
I acknowledge and grateful to [**Professor Rohini K. Srihari**](http://www.cedar.buffalo.edu/~rohini/) and TAs [**James Clay**](http://www.cse.buffalo.edu/people/?u=jnclay), [**Nikhil Londhe**](http://www.cse.buffalo.edu/people/?u=nikhillo), [**Chuishi Meng**](http://www.cse.buffalo.edu/people/?u=chuishim) and [**Ruhan Sa**](http://www.cse.buffalo.edu/people/?u=ruhansa) for their continuous support throughout the Course ([**CSE 535**](http://www.cse.buffalo.edu/shared/course.php?e=CSE&n=535&t=Information+Retrieval)) that helped me learn the skills of Information Retrieval and build a Boolean Query Processor.


Developer
---------
Ramanpreet Singh Khinda (rkhinda@buffalo.edu)</br>
[![website](https://raw.githubusercontent.com/ramanpreet1990/CSE_586_Simplified_Amazon_Dynamo/master/Resources/ic_website.png)](https://branded.me/ramanpreet1990)		[![googleplay](https://raw.githubusercontent.com/ramanpreet1990/CSE_586_Simplified_Amazon_Dynamo/master/Resources/ic_google_play.png)](https://play.google.com/store/apps/details?id=suny.buffalo.mis.research&hl=en)		[![twitter](https://raw.githubusercontent.com/ramanpreet1990/CSE_586_Simplified_Amazon_Dynamo/master/Resources/ic_twitter.png)](https://twitter.com/dk_sunny1)		[![linkedin](https://raw.githubusercontent.com/ramanpreet1990/CSE_586_Simplified_Amazon_Dynamo/master/Resources/ic_linkedin.png)](https://www.linkedin.com/in/ramanpreet1990)


License
----------
Copyright {2016} 
{Ramanpreet Singh Khinda rkhinda@buffalo.edu} 

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
