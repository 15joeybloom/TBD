# Our Project Report

We implemented aggregate functions, Group By, and Having into the SimpleDB architecture. SimpleDB, as it is, does not give the user any overview information about their data. They can select and update individual rows, but there's no way to get summary information about a large dataset. Our project works toward solving that problem.

### How to compile and run

During the development process we found the need to execute several bash commands to recompile and restart the server and interpreter to be very tedious. So we wrote a few handy bash scripts to do this for us!

You can execute [run_tests.sh](/run_tests.sh) to query the database using several example queries of our own creation to demonstrate the database's new capabilities.

If you'd like to get your hands a little dirtier, go ahead and execute the script [compile.sh](/compile.sh) to compile the project. This will compile the SimpleDB core and also the files in the studentClient directory, which are some example queries provided by the creator of SimpleDB. 

Execute the script [start_student.sh](/start_student.sh) to start the SimpleDB server and create a small set of tables for testing. 

Alternatively, execute [start_large.sh](/start_large.sh) to start the SimpleDB server and create the same set of tables but with a relatively large amount of data (100s of rows) in the Student and Enroll tables. Then you can execute [largetest1.sh](/largetest1.sh) or [largetest2.sh](/largetest2.sh) to see the efficiency of our implementation.

With either start_student.sh or start_large.sh, you can execute [run_interpreter.sh](/run_interpreter.sh) to be able to try your own SQL queries.

### What we set out to do

Our goals were to implement several aggregation operators, the Group By clause, and the Having clause. We wanted to be able to execute a query like the following:

```sql
Select gradyear, count(sid)
From Student
Group By gradyear
Having count(sid) = 2
```
This selects all of the graduating classes with two students, and gives the year and number of students.

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

For the query execution, we created two classes - [AggregatePlan](/simpledb/query/AggregatePlan) and [AggregateScan](/simpledb/query/AggregateScan). A phase 1 query is guaranteed to have only 1 result tuple, so we begin execution by initializing two integer variables for each field in the result - the accumulator and the count. As we read tuples from the child scan of the AggregateScan, the accumulator for a field stores either the sum of the values, or the minimum/maximum value seen so far, depending on the aggregation function associated with the field. The count variable for a field stores the number of values seen so far, used for the count and avg aggregation functions. After all tuples have been read from the child scan, the AggregateScan reports the summary data.

We also created a class [AggQueryPlanner](/simpledb/planner/AggQueryPlanner.java). This class parallels [BasicQueryPlanner](/simpledb/planner/BasicQueryPlanner.java), they both implement the Planner interface. For phase 1, we chose to make AggQueryPlanner extend BasicQueryPlanner. We could then take advantage of that inheritance relationship within AggQueryPlanner.createPlan by calling super and then adding an AggregatePlan to the top of the execution tree. Here's what a phase 1 execution tree might have looked like:

1. TablePlan on Enroll
2. SelectPlan on Node 1
3. ProjectPlan on Node 2
4. AggregatePlan on Node 3

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

Phase 2 suggested removing the inheritance relationship between AggQueryPlanner and BasicQueryPlanner; the relationship was no longer useful. Both classes now implement the Planner interface directly. The reason for this is that an aggregate query with a group by should not include a ProjectPlan. If we project to only include the fields in the select clause, we might exclude a field that is in the group by clause and is necessary for proper query execution. So therefore, AggQueryPlanner needed to build a Plan that did not include a ProjectPlan. Here's what the simplest phase 2 query execution tree might look like:

1. TablePlan on Enroll
2. SelectPlan on Node 1
3. AggregatePlan on Node 2

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

### Other features

After we implemented aggregation, we ended up with long column names in result sets, like "count(studentid)". These were causing our results to display poorly, and the column headers would be offset from the data. We found the code in SimpleDB that handles printing the result set and improved its ability to handle longer field names.

After implementing count, sum, avg, min, and max, we realized it would be pretty easy to implement other aggregation functions. We decided to experiment a bit with expanding aggregation options beyond the SQL standard. After some fiddling, we settled on a range(fieldname) function that would determine the difference between the maximum and minimum values in a column. Here's an example query:

```sql
Select sname, range(grade)
From Student, Enroll
Where sid = studentid
Group By sname
```
This gets the range of grades for each student - the difference between the student's best and worst grades.

### Challenges we ran into

The first challenge we faced was understanding the structure of SimpleDB. In order to determined where we needed to make changes, we first had to figure out where the query processing work was done. The earliest work was tracing function calls and finding where we could access and modify query data.

The next challenge was fitting the aggregation model into the existing SimpleDB query execution process. Unlike select, project, and product operations, aggregation functions must iterate through the entire relation before they have a result to return. We tackled this by redesigning the next() method in our AggregateQueryScan class to only be called one time and to process the entire sub-relation at once.

As we moved onto implementing group by we had to start making larger and larger changes to the base code, which took trial and error. Fortunately by this point we had considerable experience working with SimpleDB and trying out different solution implementations was simply a matter of time investment.

### Lessons Learned

1. Documentation in large code bases is invaluable. If it weren't for the clear documentation and structural models for the SimpleDB code and architecture, it would have taken ages to grasp how queries, planners, scans and plans all work together to get the data the user wants.
2. Don't go overboard with version control. Early on in our project development we tried to be very conservative and precise with our version control to minimize the complexity of the repo. This ended up being extremely cumbersome and caused some issues with failed commits and eventually needs for messy merges. We simplified our repo control and communicated what files we were making changes to to improve workflow.
3. Don't be afraid to make changes. Early on we tried to change as little code as possible in order to prevent significant bugs on our end. This ended up forcing us into a small solution space since we weren't effectively expanding upon the utility of the provided code. Once we became more confident and started making larger changes, we were able to more easily realize the functionality needed for comprehensive aggregation.
