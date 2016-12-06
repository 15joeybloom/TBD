package simpledb.query;

import java.util.*;

/**
 * The scan class corresponding to the <i>aggregation</i> relational
 * algebra operator.
 * All methods except hasField delegate their work to the
 * underlying scan.
 * @author Robert Kaucic
 */
public class AggregateScan implements Scan {
    private Scan s;
    private Map<String,String> schemaFields;
    private Map<String,String> schemaAggs;
    private List<String> groupByFields;
    private List<List<Constant>> aggKeys;
    private List<Map<String,Integer>> accumulators, counts;
    private Map<String, Integer> accumulatorInitialValues, countInitialValues;

    //the current row for next()
    private int row;

    private boolean called;

    /**
     * Creates an aggregate scan having the specified
     * underlying scan, field, and aggfns list.
     * @param s the underlying scan
     * @param fieldlist the list of field names
     * @param aggfns the aggregation functions list
     */
    public AggregateScan(Scan s, Map<String,String> schemaFields, 
        Map<String,String> schemaAggs, Collection<String> groupByFields) {
        this.s = s;
        this.schemaFields = schemaFields;
        this.schemaAggs = schemaAggs;
        this.groupByFields = new ArrayList<>(groupByFields);
        this.accumulators = new ArrayList<>();
        this.counts = new ArrayList<>();
        this.called = false;

        aggKeys = new ArrayList<>();

        accumulatorInitialValues = new HashMap<>();
        countInitialValues = new HashMap<>();
        for(Map.Entry<String, String> entry : this.schemaAggs.entrySet()) {
            if(entry.getValue() == null) {
                //if it's a regular, not aggregated field
                //don't need an accumulator or count
                continue;
            }
            if(entry.getValue().equals("min")) {
                accumulatorInitialValues.put(entry.getKey(), Integer.MAX_VALUE);
            }
            else if(entry.getValue().equals("max")) {
                accumulatorInitialValues.put(entry.getKey(), Integer.MIN_VALUE);
            }
	    else if(entry.getValue().equals("range")) {
		accumulatorInitialValues.put(entry.getKey(), Integer.MAX_VALUE);
	    }
            else {
                accumulatorInitialValues.put(entry.getKey(), 0);
            }
            countInitialValues.put(entry.getKey(), 0);
	    if(entry.getValue().equals("range")) { // a little sloppy
		countInitialValues.put(entry.getKey(), Integer.MIN_VALUE);
	    }
        }

        row = -1;
        /*
        System.out.println("groupByFields = " + groupByFields);
        System.out.println("schemaFields = " + schemaFields);
        System.out.println("schemaAggs = " + schemaAggs);

        Constant c1 = new IntConstant(1);
        Constant c2 = new IntConstant(1);
        System.out.println("c1.equals(c2) == " + c1.equals(c2));
        */
    }

    //Honestly not sure what this is doing.
    public void beforeFirst() {
        s.beforeFirst();
        this.called = false;
        aggKeys.clear();
        accumulators.clear();
        counts.clear();
        row = -1;
    }

    /* Only allow next() to be called once. If it is
     * called for the first time, it returns whether
     * or not its child scan has any records. Otherwise
     * it returns false, since the aggregation can
     * only occur once.
     */
    public boolean next() {
        if(!this.called) {
            //calculate
            while(s.next()) {
                List<Constant> key = new ArrayList<>();
                for(String groupByField : groupByFields) {
                    key.add(s.getVal(groupByField));
                }

                int keyIndex = aggKeys.indexOf(key);
                if(keyIndex == -1) {
                    keyIndex = aggKeys.size();
                    aggKeys.add(key);
                    accumulators.add(new HashMap<>(accumulatorInitialValues));
                    counts.add(new HashMap<>(countInitialValues));
                }
                for(String field : accumulatorInitialValues.keySet()) {
                    String fldname = schemaFields.get(field);
                    String aggfn = schemaAggs.get(field);
                    /*
                    System.out.println("accumulators = " + accumulators);
                    System.out.println("keyIndex = " + keyIndex);
                    System.out.println("field = " + field);
                    */
                    int accumulator = accumulators.get(keyIndex).get(field);
                    int count = counts.get(keyIndex).get(field);

                    int value = s.getInt(fldname);
                    if(aggfn.equals("avg") || aggfn.equals("sum")) {
                        accumulator += value;
                        count++;
                    }
                    else if(aggfn.equals("count")) {
                        count++;
                    }
                    else if(aggfn.equals("max")) {
                        if(value > accumulator)
                            accumulator = value;
                    }
                    else if(aggfn.equals("min")) {
                        if(value < accumulator)
                            accumulator = value;
                    }
		    else if(aggfn.equals("range")) {
			if(value > count) {
			    count = value;
			}
			if(value < accumulator) {
			    accumulator = value;
			}
		    }
                    else { //unrecognized agg function
                        throw new RuntimeException("Unrecognized aggregate function " + aggfn + ". Length is " + aggfn.length());
                    }
                    accumulators.get(keyIndex).put(field, accumulator);
                    counts.get(keyIndex).put(field, count);
                }
            }
        }
        this.called = true;
        row++;
        return row < aggKeys.size();
    }

    //Close the child scan, propagates.
    public void close() {
        s.close();
    }

    //Functions for getting values.
    public Constant getVal(String fldname) {
        if(groupByFields.contains(fldname)) {
            return aggKeys.get(row).get(groupByFields.indexOf(fldname));
        }
        else
            //otherwise it's an aggregate field
            return new IntConstant(getInt(fldname));
    }

    /* This method is distinctly different from those in
     * other scans. It should only be called once per fldname,
     * after next() is called the one time that is allowed.
     * Gets the aggfn associated with the fldname, and
     * performs it on the values that the child scan
     * returns.
     */
    public int getInt(String fldname) {
        if(groupByFields.contains(fldname)) {
            return (Integer)aggKeys.get(row).get(groupByFields.indexOf(fldname)).asJavaVal();
        }

        String target = schemaFields.get(fldname);
        if(target == null) {
            throw new RuntimeException("field " + fldname + " not found.");
        }

        String aggfn = schemaAggs.get(fldname);
        int accumulator = accumulators.get(row).get(fldname);
        int count = counts.get(row).get(fldname);

        if(aggfn.equals("avg"))
            return accumulator / count;
        else if(aggfn.equals("sum") || aggfn.equals("min") || aggfn.equals("max"))
            return accumulator;
        else if(aggfn.equals("count"))
            return count;
	else if(aggfn.equals("range"))
	    return count - accumulator;
        else //should be literally impossible to get here
            throw new RuntimeException("Unrecognized aggregate function " + aggfn + ". Length is " + aggfn.length());
    }

/*
    public double aggregateInt(String fldname, String aggfn) {
        //general accumulator variables to handle each aggfn
        int accumulator = 0; //value
        int accumulator2 = 0; //count
        while(s.next()) {
            int value = s.getInt(fldname);
            if(aggfn.equals("avg") || aggfn.equals("sum")) {
                accumulator += value;
                accumulator2++;
            }
            else if(aggfn.equals("count"))
                accumulator2++;
            else if(aggfn.equals("max")) {
                if(value > accumulator)
                    accumulator = value;
            }
            else if(aggfn.equals("min")) {
                if(value < accumulator)
                    accumulator = value;
            }
            else { //unrecognized agg function
                //uh... what to do here?
                return -1;
            }
        }
        if(aggfn.equals("avg"))
            return accumulator / accumulator2;
        else if(aggfn.equals("sum") || aggfn.equals("min") || aggfn.equals("max"))
            return accumulator;
        else if(aggfn.equals("count"))
            return accumulator2;
        else //should be literally impossible to get here
            return -1; //what do we do though?
    }
    */

    //Not sure if this method should even exist in AggScan
    //It it should, it should be similar to above, just using
    //string comparaters instead of int comparaters
    public String getString(String fldname) {
        if(groupByFields.contains(fldname)) {
            return (String)aggKeys.get(row).get(groupByFields.indexOf(fldname)).asJavaVal();
        }
            throw new RuntimeException("field " + fldname + " not found.");
    }

    /**
     * Returns true if the specified field
     * is in the aggregation list.
     * @see simpledb.query.Scan#hasField(java.lang.String)
     */
    public boolean hasField(String fldname) {
        return schemaFields.containsKey(fldname);
    }
}
