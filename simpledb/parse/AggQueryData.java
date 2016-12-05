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
    private Collection<String> groupByFields;
    private Predicate having;

    public AggQueryData(Collection<String> aggFunctions, Collection<String> groupByFields, Predicate having, Collection<String> fields, Collection<String> tables, Predicate pred) {
        super(fields, tables, pred);
        this.aggFunctions = aggFunctions;
        this.groupByFields = groupByFields;
        this.having = having;
    }

    public Collection<String> aggFunctions() {
        return aggFunctions;
    }
    public Collection<String> groupByFields() {
        return groupByFields;
    }
    public Predicate having() {
        return having;
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
        if (!groupByFields.isEmpty()) {
            result += " group by ";
            boolean first = true;
            for(String s : groupByFields) {
                if(first) {
                    result += s;
                    first = false;
                }
                else {
                    result += ", " + s;
                }
            }
            String havingString = having().toString();
            if(!havingString.equals("")) {
                result += " having " + havingString;
            }
        }
        return result;
    }
}
