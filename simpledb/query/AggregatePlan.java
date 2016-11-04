package simpledb.query;

import simpledb.record.Schema;
import java.util.Collection;

/** The Plan class corresponding to the <i>aggregate</i>
  * relational algebra operator.
  * @author Robert Kaucic
  */

public class AggregatePlan implements Plan {
   private Plan p;
   private Schema schema = new Schema();
   private Collection<String> aggfns;
   
   /**
    * Creates a new aggregate node in the query tree,
    * having the specified subquery, field list,
    * and aggregate functions list.
    * @param p the subquery
    * @param fieldlist the list of fields
    * @param aggfnlist the list of aggregate functions
    */
    public AggregatePlan(Plan p, Collection<String> fieldlist, Collection<String> aggfns) {
      this.p = p;
      for (String fldname : fieldlist)
         schema.add(fldname, p.schema());
      this.aggfns = aggfns;
   }

   /**
    * Creates an aggregate scan for this query.
    * @see simpledb.query.Plan#open()
    */
   public Scan open() {
      Scan s = p.open();
      return new AggregateScan(s, schema.fields(), aggfns);
   }

   /**
    * Estimates the number of block accesses in the aggregation,
    * which is the same as in the underlying query.
    * @see simpledb.query.Plan#blocksAccessed()
    */
   public int blocksAccessed() {
      return p.blocksAccessed();
   }

   /**
    * Estimates the number of output records in the aggregation,
    * which should be precisely 1, since we do not currently
    * implement the GROUP BY clause
    * @see simpledb.query.Plan#recordsOutput()
    */
   public int recordsOutput() {
      return 1;
   }

    ///////
    //NOTE: Should this also just return 1?
    ///////
   /**
    * Estimates the number of distinct field values
    * in the aggregation,
    * which is the same as in the underlying query.
    * @see simpledb.query.Plan#distinctValues(java.lang.String)
    */
   public int distinctValues(String fldname) {
      return p.distinctValues(fldname);
   }

   /**
    * Returns the schema of the projection,
    * which is taken from the field list.
    * @see simpledb.query.Plan#schema()
    */
   public Schema schema() {
      return schema;
   }
}
