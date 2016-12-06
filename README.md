# Our Project

We implemented aggregate functions, Group By, and Having into the SimpleDB architecture. SimpleDB, as it is, does not give the user any overview information about their data. They can select and update individual rows, but there's no way to get summary information about a large dataset. Our project works toward solving that problem.

### How to compile and run

Execute the script compile.sh to compile the project. This will compile the SimpleDB core and also the files in the studentClient directory, which are some example queries provided by the creator of SimpleDB. 

Execute the script start_student.sh to start the SimpleDB server and create a small set of tables for testing. Then you can execute run_tests.sh to query the database using several example queries of our own creation to demonstrate the database's new capabilities.

Alternatively, execute start_large.sh to start the SimpleDB server and create the same set of tables but with a relatively large amount of data (100s of rows) in the Student and Enroll tables. Then you can execute largetest1.sh or largetest2.sh to see the efficiency of our implementation.

### What we set out to do

Here's our people writing about what we wanted to do. Here's some more stuff.

### What we accomplished

### How We Did It

### Challenges we ran into

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

