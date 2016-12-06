# Our Project

We implemented aggregate functions, Group By, and Having into the SimpleDB architecture. SimpleDB, as it is, does not give the user any overview information about their data. They can select and update individual rows, but there's no way to get summary information about a large dataset. Our project works toward solving that problem.

### How to compile and run

Execute the script compile.sh to compile the project. This will compile the SimpleDB core and also the files in the studentClient directory, which are some example queries provided by the creator of SimpleDB. 

Execute the script [start_student.sh](/start_student.sh) to start the SimpleDB server and create a small set of tables for testing. Then you can execute [run_tests.sh](/run_tests.sh) to query the database using several example queries of our own creation to demonstrate the database's new capabilities.

Alternatively, execute [start_large.sh](/start_large.sh) to start the SimpleDB server and create the same set of tables but with a relatively large amount of data (100s of rows) in the Student and Enroll tables. Then you can execute [largetest1.sh](/largetest1.sh) or [largetest2.sh](/largetest2.sh) to see the efficiency of our implementation.

### What we set out to do

Our goals were to implement several aggregation operators, the Group By clause, and the Having clause. We wanted to be able to execute a query like the following:

```sql
Select gradyear, count(sid)
From Student
Group By gradyear
Having count(sid) = 2
```
This selects all of the graduating classes with two students, and gives the year and number of students.

### What we accomplished

### How We Did It

#### Phase 1 - Aggregation
Our first goal was to be able to aggregate data without groups. A query that we could evaluate during this phase might have looked like:

```sql
Select avg(grade), min(grade), max(grade), count(eid)
From Enroll
Where studentid = 1
```
This calculates student number 1's grade average, their best and worst grade, and the number of grades they have (the number of classes they are enrolled in). Note that at this stage, every field in the Select clause needed to have an aggregation function.

Parsing a query in this phase involved, first and foremost, modifying the Parser to be able to recognize the difference between a regular query and an aggregation query. We modified the Lexer to recognize the different aggregation functions. The Parser then stores the field name and the aggregation function associated with it. We extended the QueryData class for this purpose, creating our AggQueryData class. 

For the query execution, we created two classes - AggregatePlan and AggregateScan. A phase 1 query is guaranteed to have only 1 result tuple, so we begin execution by initializing two integer variables for each field in the result - the accumulator and the count. As we read tuples from the child scan of the AggregateScan, the accumulator for a field stores either the sum of the values, or the minimum/maximum value seen so far, depending on the aggregation function associated with the field. The count variable for a field stores the number of values seen so far, used for the count and avg aggregation functions. After all tuples have been read from the child scan, the AggregateScan reports the summary data.

#### Phase 2 - Group By

Our next goal was to be able to aggregate data with groups. A query that we could evaluate during this phase might have looked like:

```sql
Select studentid, avg(grade), min(grade), max(grade), count(eid)
From Enroll
Group By studentid
```
This calculates the grade average, best and worst grade, and number of grades for __each student__

Parsing a query in this phase involved looking for an optional Group By clause after looking for the optional Where clause. Then, if the keywords "Group By" are in the query, we parse the comma-separated list of Group By fields and store the list of fields in an AggQueryData object.

The difference in query execution for this phase is that an aggregation query can now return more than one result tuple. Now we need a list that stores the accumulators and counts for each result tuple. When we read a result tuple from the child scan, we need to check the values of the Group By fields of this tuple to see if we already have accumulators and counts for the group to which this tuple belongs. If the variables exist, then we modify them according to the values of the aggregated fields in the child tuple, otherwise we initialize new accumulators and counts for this new group, creating a new result tuple. After we read the whole child scan, we can then start returning result tuples.

#### Phase 3 - Having

The final phase of our project was to be able to filter groups based on aggregated values by implementing the Having clause. A phase 3 query might look like:

```sql
Select studentid, avg(grade), min(grade), max(grade), count(eid)
From Enroll
Group By studentid
Having max(grade) = 100
```

This calculates the grade average, best and worst grade, and number of grades for __each student__ who has gotten a __perfect score (100)__ in a class.

Parsing a query in phase 3 involed looking for an optional Having clause if the Parser found a Group By clause. Then, if the "Having" keyword is found after the comma-separated list of Group By fields, the Parser parses a Predicate from the query just like it does for the Where clause. This Predicate is stored in an AggQueryData object.

Query execution for this phase involved modifying AggQueryPlanner. After some thought, we realized that Having could be implemented by a SelectPlan, to be placed directly above the AggregatePlan in the query execution tree. So the query visualization for our example phase 3 query might look like:

1. TablePlan on Enroll
2. SelectPlan on Node 1
3. AggregatePlan on Node 2
4. SelectPlan on Node 3

### Challenges we ran into

The first challenge we faced was understanding the structure of SimpleDB. In order to determined where we needed to make changes, we first had to figure out where the query processing work was done. The earliest work was tracing function calls and finding where we could access and modify query data.

The next challenge was fitting the aggregation model into the existing SimpleDB query execution process. Unlike select, project, and product operations, aggregation functions must iterate through the entire relation before they have a result to return. We tackled this by redesigning the next() method in our AggregateQueryScan class to only be called one time and to process the entire sub-relation at once.

As we moved onto implementing group by we had to start making larger and larger changes to the base code, which took trial and error. 
### Lessons Learned
1.
2.
3.

### Testing Our Project

For editing it, use this [cheat sheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet#headers)

Here's the cool algorithm we used!

```java
int coolAlgorithm(Table t, Scan s, Query q)
{
  return 4;
}
```

