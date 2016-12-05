
# Run ./compile_run_start but without output to terminal
echo "Compiling..."
{
	fuser -k 1099/tcp
	javac -Xlint:unchecked -cp . simpledb/*/*.java simpledb/*/*/*.java
	javac -cp . studentClient/simpledb/*.java
}	1>/dev/null 2>&1


# Start the server. Keep it from printing to the terminal
echo "Starting Server..."
java simpledb.server.Startup studentdb > /dev/null &

# Run Tests on each file in /tests/
for ofilename in tests/*.txt; do
    filename=$(basename "$ofilename")
    extension="${filename##*.}"
    filename="${filename%.*}"

    if [[ ${filename} != *"_expected"* ]];then
		echo ""
        echo "Running Test: "$filename   
        expected="tests/"$filename"_expected."$extension

        if [ ! -f $expected ]; then
            echo "Matching Output File '" $expected"' not found."
        else
            rm -rf "tests/temp_input.txt"
            rm -rf "tests/temp_output.txt"
            echo "_______SQL Input_______"
            cat "tests/"$filename".txt"
			# Copy the test file to the temp_input file
            cat "tests/"$filename".txt" > "tests/temp_input.txt"
			# Append an 'exit' to the temp_input file
            echo "exit" >> "tests/temp_input.txt"
			# Run the interpreter. Read from temp_input, write to temp_output
            ./run_interpreter.sh < "tests/temp_input.txt" > "tests/temp_output.txt"
			# Remove SQLInterpreter input output, blank lines, lines from input, etc, from output
			cp tests/temp_output.txt tests/temp_output_super_temp.txt
			grep -v '^SQL>' tests/temp_output_super_temp.txt > tests/temp_output.txt
			cp tests/temp_output.txt tests/temp_output_super_temp.txt
			grep -v -x -f tests/temp_input.txt tests/temp_output_super_temp.txt > tests/temp_output.txt 
			cp tests/temp_output.txt tests/temp_output_super_temp.txt			
			grep -v -e '^$' tests/temp_output_super_temp.txt > tests/temp_output.txt
			rm -rf tests/temp_output_super_temp.txt
			echo "_______SQL Output_______"
			cat "tests/temp_output.txt"
			echo ""
            if ! cmp "tests/temp_output.txt" $expected >/dev/null 2>&1
            then
                echo "Test Differed from Expected Output."
				echo "The following was expected - "
				cat $expected
            else
                echo "Test Matched Expected Output!"
            fi
            echo ""
            rm -rf "tests/temp_input.txt"
            rm -rf "tests/temp_output.txt"
        fi
    fi    

    #echo "Running Test in File:" $filename
    #./run_interpreter.sh < "$filename" > "temp_test.txt"
done

#rm -rf server_output.txt
#rm -rf compile_output.txt
