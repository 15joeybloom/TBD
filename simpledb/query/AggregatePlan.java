package simpledb.query;

import simpledb.record.Schema;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/** The Plan class corresponding to the <i>aggregate</i>
  * relational algebra operator.
  * @author Robert Kaucic
  */

public class AggregatePlan implements Plan {
   private Plan p;
   private Schema schema = new Schema();
   private Map<String,String> schemaFields;
   private Map<String,String> schemaAggs;
   
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
      this.schemaFields = new HashMap<>();
      this.schemaAggs = new HashMap<>();
      Iterator<String> fieldlistIt, aggfnsIt;
      fieldlistIt = fieldlist.iterator();
      aggfnsIt = aggfns.iterator();
      while(fieldlistIt.hasNext() && aggfnsIt.hasNext()) {
        String field = fieldlistIt.next();
        String agg = aggfnsIt.next();
        String schemaField = agg + "("+field+")";
         schema.addIntField(schemaField);
         schemaFields.put(schemaField, field);
         schemaAggs.put(schemaField, agg);
      }
   }

   /**
    * Creates an aggregate scan for this query.
    * @see simpledb.query.Plan#open()
    */
   public Scan open() {
      Scan s = p.open();
      return new AggregateScan(s, schemaFields, schemaAggs);
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
    * in the aggregation, which is always 1.
    * @see simpledb.query.Plan#distinctValues(java.lang.String)
    */
   public int distinctValues(String fldname) {
        return 1;
   }

   /**
    * Returns the schema of the aggregation.
    * @see simpledb.query.Plan#schema()
    */
   public Schema schema() {
      return schema;
   }
}
