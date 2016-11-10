for ofilename in tests/*.txt; do
    filename=$(basename "$ofilename")
    extension="${filename##*.}"
    filename="${filename%.*}"

    #echo "Filename:"$filename
    #echo "Extension:"$extension

    if [[ ${filename} != *"_expected"* ]];then
        echo "Running Test: "$filename   
        expected="tests/"$filename"_expected."$extension
        #echo "Expected:"$expected
        if [ ! -f $expected ]; then
            echo "Matching Output File '" $expected"' not found."
        else
            rm -rf "tests/temp.txt"
            rm -rf "tests/temp2.txt"
            echo "- - -"
            cat "tests/"$filename".txt"
            cat "tests/"$filename".txt" > "tests/temp2.txt"
            echo "exit" >> "tests/temp2.txt"
            #cat "tests/temp2.txt"
            echo "- - -"
            ./run_interpreter.sh < "tests/temp2.txt" > "tests/temp.txt"
            if ! cmp "tests/temp.txt" $expected >/dev/null 2>&1
            then
                echo "Test Differed from Expected Output"
            else
                echo "Test Passed!"
            fi
            echo "-----------"
            #exit 1
            #cat "tests/temp.txt"
            #diff "tests/temp.txt" "tests/"$filename".txt"
            rm -rf "tests/temp.txt"
            rm -rf "tests/temp2.txt"
            #diff "tests/temp.txt" "tests/"$filename".txt"
            #echo "File Found!"
        fi
    fi    

    #echo "Running Test in File:" $filename
    #./run_interpreter.sh < "$filename" > "temp_test.txt"
done
