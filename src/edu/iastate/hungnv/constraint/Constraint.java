package edu.iastate.hungnv.constraint;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExprFactory$;
import java.util.ArrayList;
import scala.collection.immutable.Set;

/**
 * 
 * @author HUNG
 *
 */
public class Constraint {
	
	// Use the JavaBDD library instead of Sat4j 
	static {
		FeatureExprFactory.setDefault(BDDFeatureExprFactory$.MODULE$);
	}
	
	public static final Constraint TRUE	 = new Constraint(FeatureExprFactory.True());
	
	public static final Constraint FALSE = new Constraint(FeatureExprFactory.False());
	
	// The FeatureExpr representing this constraint
	private FeatureExpr featureExpr;
	
	/**
	 * Private constructor
	 * @param featureExpr
	 */
	private Constraint(FeatureExpr featureExpr) {
		this.featureExpr = featureExpr;
	}
	
	/*
	 * Methods
	 */
	
	/**
	 * @return True if the constraint is satisfiable
	 */
	public boolean isSatisfiable() {
		return featureExpr.isSatisfiable();
	}
	
	/**
	 * @return True if the constraint is a tautology
	 */
	public boolean isTautology() {
		return featureExpr.isTautology();
	}
	
	/**
	 * @return True if the constraint is a contradiction
	 */
	public boolean isContradiction() {
		return featureExpr.isContradiction();
	}
	
	/**
	 * @param constraint
	 * @return True if the two constraints are equivalent
	 */
	public boolean equivalentTo(Constraint constraint) {
		return (this.featureExpr.equivalentTo(constraint.featureExpr));
	}
	
	/**
	 * @param constraint
	 * @return True if the two constraints are opposite of each other
	 */
	public boolean oppositeOf(Constraint constraint) {
		return (this.featureExpr.equivalentTo(constraint.featureExpr.not()));
	}
	
	/**
	 * Returns the FeatureExpr representing this constraint.
	 * Should be used by edu.iastate.hungnv.empiricalstudy.EmpiricalStudy only.
	 */
	public FeatureExpr getFeatureExpr() {
		return featureExpr;
	}

	/**
	 * @return A string describing the constraint
	 */
	@Override
	public String toString() {
		return featureExpr.toString();
	}
	
	/**
	 * Given the current constraint, try adding another constraint onto it.
	 * For example, suppose the current constraint is A, the added constraint is B
	 * (A & B can be dependent, e.g. A = a & c, B = b & c),
	 * then we have 3 possible outcomes:
	 * 		+ A & B stays THE SAME:  A & B = A		(equivalently, A & !B = FALSE)
	 * 		+ A & B is ALWAYS FALSE: A & B = FALSE  (equivalently, A & !B = A)
	 * 		+ A & B cannot be determined
	 * @param constraint
	 */
	public Result tryAddingConstraint(Constraint constraint) {
		if (Constraint.createAndConstraint(this, Constraint.createNotConstraint(constraint)).isContradiction())
			return Result.THE_SAME;
		
		else if (Constraint.createAndConstraint(this, constraint).isContradiction())
			return Result.ALWAYS_FALSE;
		
		else
			return Result.UNDETERMINED;
	}

	/**
	 * @see edu.iastate.hungnv.constraint.Constraint.tryAddingConstraint(Constraint) 
	 */
	public enum Result {
		THE_SAME, 
		ALWAYS_FALSE,
		UNDETERMINED
	}
	
	/*
	 * Static methods
	 */
	
	public static Constraint createConstraint(String constraint) {
		return new Constraint(FeatureExprFactory.createDefinedExternal(constraint));
	}
	
	public static Constraint createNotConstraint(Constraint constraint) {
		return new Constraint(constraint.featureExpr.not());
	}
	
	public static Constraint createAndConstraint(Constraint constraint1, Constraint constraint2) {
		return new Constraint(constraint1.featureExpr.and(constraint2.featureExpr));
	}
	
	public static Constraint createOrConstraint(Constraint constraint1, Constraint constraint2) {
		return new Constraint(constraint1.featureExpr.or(constraint2.featureExpr));
	}
        
        /*public static Constraint clearNotConstraint(Constraint constraint){
            Set<String> temp = constraint.featureExpr.collectDistinctFeatures();
            String[] featuresStr = new String[temp.size()];
            temp.copyToArray(featuresStr);
            String constraintStr = constraint.toString();
            for(String featureStr : featuresStr){
                Constraint feature = Constraint.createConstraint(featureStr);
                if (constraintStr.contains("!" + featureStr))
                    constraint = Constraint.createAndConstraint(constraint, feature);
            }
            return constraint;
        }*/
}
