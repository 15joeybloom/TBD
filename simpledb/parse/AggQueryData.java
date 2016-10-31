package simpledb.parse;

import simpledb.query.*;
import java.util.*;

/**
 * Data for the SQL <i>select</i> statement when one or more aggregate functions are used
 * in the select clause.
 * @author Joey Bloom
 */
public class AggQueryData extends QueryData {
    private Collection<String> aggFunctions;

    public AggQueryData(Collection<String> aggFunctions, Collection<String> fields, Collection<String> tables, Predicate pred) {
        super(fields, tables, pred);
        this.aggFunctions = aggFunctions;
    }

    public Collection<String> aggFunctions() {
        return aggFunctions;
    }
    public String toString() {
        String result = "select ";
        Iterator<String> fldIt = fields().iterator(), 
            aggFunctionIt = aggFunctions.iterator();
        while(fldIt.hasNext() && aggFunctionIt.hasNext()) {
            String fldname = fldIt.next(), 
                aggFunction = aggFunctionIt.next();
            if(aggFunction == null) {
                result += fldname + ", ";
            }
            else {
                result += aggFunction + "(" + fldname + "), ";
            }
        }
        result = result.substring(0, result.length()-1); //remove final comma
        result += " from ";
        for(String tblname : tables())
            result += tblname + ", ";
        result = result.substring(0, result.length()-2); //remove final comma
        String predstring = pred().toString();
        if (!predstring.equals(""))
            result += " where " + predstring;
        return result;
    }
}
