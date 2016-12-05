query="select sname, count(grade), avg(grade) from student, enroll where studentid = sid group by sname\nexit"
echo -e $query | ./run_interpreter.sh
