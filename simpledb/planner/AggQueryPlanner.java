package simpledb.planner;

import simpledb.tx.Transaction;
import simpledb.query.*;
import simpledb.parse.*;
import simpledb.server.SimpleDB;
import java.util.*;

/**
 * The simplest, most naive aggregation query planner possible.
 * No group by. Each field must have an associated aggregation function.
 * @author Joey Bloom
 */
public class AggQueryPlanner implements QueryPlanner {
   
   /**
    * Creates a query plan as follows.  It first takes
    * the product of all tables and views; it then selects on the predicate;
    * and finally it projects on the field list. 
    */
   public Plan createPlan(QueryData data, Transaction tx) {
        AggQueryData aggdata = (AggQueryData)data;
        //Plan p = super.createPlan(data, tx);
      //Step 1: Create a plan for each mentioned table or view
      List<Plan> plans = new ArrayList<Plan>();
      for (String tblname : data.tables()) {
         String viewdef = SimpleDB.mdMgr().getViewDef(tblname, tx);
         if (viewdef != null)
            plans.add(SimpleDB.planner().createQueryPlan(viewdef, tx));
         else
            plans.add(new TablePlan(tblname, tx));
      }
      
      //Step 2: Create the product of all table plans
      Plan p = plans.remove(0);
      for (Plan nextplan : plans)
         p = new ProductPlan(p, nextplan);
      
      //Step 3: Add a selection plan for the predicate
      p = new SelectPlan(p, data.pred());
      
      //don't project! There could be fields in the group by clause
      //that aren't in the select clause. projecting would lose
      //those fields. Let the aggregateplan take care of projection.
        /*
      //Step 4: Project on the field names
      p = new ProjectPlan(p, data.fields());
      return p;
      */
        //Step 5: Perform the aggregation
        p = new AggregatePlan(p, data.fields(), aggdata.aggFunctions(), aggdata.groupByFields());

        //Step 6: Add a selection plan for the having predicate
        p = new SelectPlan(p, aggdata.having());
        return p;
   }
}
