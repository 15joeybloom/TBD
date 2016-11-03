package simpledb.query;

import java.util.*;

/**
 * The scan class corresponding to the <i>aggregation</i> relational
 * algebra operator.
 * All methods except hasField delegate their work to the
 * underlying scan.
 * @author Robert Kaucic
 */
public class ProjectScan implements Scan {
   private Scan s;
   private Collection<String> fieldlist;
   private Collection<String> aggfns;

   private boolean called;

   /**
    * Creates an aggregate scan having the specified
    * underlying scan, field, and aggfns list.
    * @param s the underlying scan
    * @param fieldlist the list of field names
    * @param aggfns the aggregation functions list
    */
   public AggregateScan(Scan s, Collection<String> fieldlist, Collection<String> aggfns) {
      this.s = s;
      this.fieldlist = fieldlist;
      this.aggfns = aggfns;
      this.called = 0;
   }

    //Honestly not sure what this is doing.
   public void beforeFirst() {
      s.beforeFirst();
   }
   
   /* Only allow next() to be called once. If it is
	* called for the first time, it returns whether
	* or not its child scan has any records. Otherwise
	* it returns false, since the aggregation can
	* only occur once.
	*/
   public boolean next() {
	  if(this.called == 0) {
		this.called = 1;
      	return s.next();
	  }
	  return false;
   }
   
    //Close the child scan, propagates.
   public void close() {
      s.close();
   }

    //Functions for getting values.
   public Constant getVal(String fldname) {
      return s.getVal(fldname);
   }
   
   /* This method is distinctly different from those in
	* other scans. It should only be called once per fldname,
	* after next() is called the one time that is allowed.
	* Gets the aggfn associated with the fldname, and
	* performs it on the values that the child scan
	* returns.
	*/
   public int getInt(String fldname) {
      if(hasField(fldname)) {
		String field_name;
		String agg_fun;
		for (int i = 0; i < fieldlist.length; ++i) {
  			field_name = fieldlist.get(i);
  			agg_fun = aggfns.get(i);
  			if(field_name.equals(fldname)) {
				break;
			}
		}
		return aggregateInt(field_name, agg_fun);
	 }
	 else {
		throw new RuntimeException("field " + fldname + " not found.");
	 }
	  
   }
   
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

	//Not sure if this method should even exist in AggScan
	//It it should, it should be similar to above, just using
	//string comparaters instead of int comparaters
   public String getString(String fldname) {
      return s.getString(fldname);
   }
   
   /**
    * Returns true if the specified field
    * is in the aggregation list.
    * @see simpledb.query.Scan#hasField(java.lang.String)
    */
   public boolean hasField(String fldname) {
      return fieldlist.contains(fldname);
   }
