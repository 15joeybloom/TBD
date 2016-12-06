query="select title, count(grade), avg(grade) from course, section, enroll where cid = courseid and sectid = sectionid group by title\nexit"
echo -e $query | ./run_interpreter.sh
