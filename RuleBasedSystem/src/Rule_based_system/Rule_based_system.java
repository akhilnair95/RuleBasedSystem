
package Rule_based_system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


class Hypothesis{
	                              // eg ?x lives in Italy
	Bindage_class[] Bindages;		// Bindages (Nouns) to which x,y,z are Bound
	Pointer_class pointers;		// Points to what in called Hypothesis
	
	boolean found; 				// Hyp statement found in assertions or rules (used while testing)
	boolean correct = false;	// Correctness of Hypothesis set by test() method
	
	boolean simple;				// Hyp is simple - true Complex -false (Complex means it consists of sub hyps linkd with 'and' or 'or')
	String statement;			// Hypothesis statement if hyp is simple
	
	String gate;				// 'And' or 'Or' gate connecting sub hyps if hyp is complex
	Hypothesis[] sub_hypothesis; // sub hyps if complex
	
	private long sleeptime = 100; // Time gap for effect
	
	public Hypothesis(){
	  pointers = new Pointer_class(); // sets pointers to '0'
      found = false;
	}
	

	public Hypothesis(String line){
		pointers = new Pointer_class();
		this.simple = true;						// setting initial hyp to simple
		Bindages = new Bindage_class[1];	// Crating a Bindage array
		Bindages[0] = new Bindage_class();	// Initialising
		statement = Bindages[0].make_bound(line);	// Converting input line to statement with bindages 
		found = false;
	}
	
	
	
	Hypothesis test(){
		/*
		 * Tests if the Hypothesis is correct based on rules and assertions
		 * Algorithm
		 * 		Simple
		 * 			Test in assertions get Bindages
		 * 			Test in rules get Bindages
		 * 				if presrnt in rules or assertions return correct with bindages(Setting these vals in hyp 'result')
		 * 				else return false
		 * 		Complex
		 * 			If And
		 * 				Set bindages of sub hyps with that of test hyp
		 * 				Test for the sub hyp
		 * 					if(false) return false
		 * 					else the result hyp returns a subset of orig Bindages which is assignes to this.Bindages
		 * 			else(Or)
		 * 				Set bindages of sub hyps with that of test hyp
		 * 				Test for the sub hyp
		 * 					if(true) union of result.Bind and this.bind is assgnd to this.Bind
		 * 				if(Bindage length is 0) return false
		 * 				else return this.Bindages,true	
		 */
		
		Hypothesis result = new Hypothesis();
		
		if(simple){
		p.inc();	
		
		for(int i=0;i<this.Bindages.length;i++)
			p.print("Testing: "+Bindage_class.make_line(this.statement, this.Bindages[i]));
		
					Hypothesis assn_test_result = Assertion_class.test(this);        // test in assertions
					Hypothesis rule_test_result = Rule_class.test(this);			// test in rules
					
					
					try {
						Thread.sleep(sleeptime );
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
							
					if(assn_test_result.found&&rule_test_result.correct){
						result.Bindages = Bindage_class.get_union(rule_test_result.Bindages, assn_test_result.Bindages);
						result.correct = true;
					}
					else if(assn_test_result.found){
						assn_test_result.correct = true;                 // The Simple hyp h is only present in assertions
						result = assn_test_result;  
					}
					else if(rule_test_result.correct){
						result.correct = true;
						result = rule_test_result;
					}
					
				try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(result.correct == true){
						for(int i=0;i<result.Bindages.length;i++)
						p.print(Bindage_class.make_line(this.statement, result.Bindages[i])+"	- found true");
						
					}
					else
						p.print("Found false");
				p.dec();	
			}
		//                IF Hyp is not Simple ,then
		else{        
				if(this.gate.matches("And")){
					
						Bindage_class[] dummy_bind= new Bindage_class[this.Bindages.length];
						for(int i=0;i<this.Bindages.length;i++){          // verum show
							dummy_bind[i] = new Bindage_class();
							dummy_bind[i].copy(this.Bindages[i]);
							}
						
						for(int i=0;i<this.sub_hypothesis.length;i++){
							sub_hypothesis[i].Bindages = dummy_bind;
							Hypothesis res = sub_hypothesis[i].test();
							
							if(!res.correct){
								result.correct = false;
								return result;
							}
							
							dummy_bind = res.Bindages;
						}
						// return true wid bindages
						result.Bindages = dummy_bind;
						result.correct = true;
						return result;
				}
				else{			// gate is OR
						//p.print(this.Bindages[0].y_bind);	
						Bindage_class[] dummy_bind= new Bindage_class[0];
						for(int i=0;i<this.sub_hypothesis.length;i++){
							sub_hypothesis[i].Bindages = this.Bindages;
							Hypothesis res = sub_hypothesis[i].test();
							if(res.correct){
							Bindage_class[] d_bind = res.Bindages;
							dummy_bind = Bindage_class.get_union(dummy_bind, d_bind);
							}
						}
						// end of loop test for correctness
						if(dummy_bind.length == 0) // hyp false
						{
							result.correct = false;
							return result;
						}
						else{                     // hyp true
							result.Bindages = dummy_bind;
							result.correct = true;
						}
				}
		}
		
		return result;
	}


	public static Hypothesis[] concat(Hypothesis[] matches, Hypothesis h) {
		/*
		 * Merges h with matches 
		 */
		
		Hypothesis[] temp = new Hypothesis[matches.length+1];
		for(int i=0;i<matches.length;i++){
			temp[i] = matches[i];
		}
		temp[matches.length] = h;
		return temp;
	}
	
	
}




/*
 * Main class
 * Inputs a line to terminal 
 * Converts to hypothesis
 * tests the hyp
 * */


public class Rule_based_system {
	static Assertion_class Assertion = new Assertion_class();;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			new Rule_class();                          //  for initialising the rules in rule class
			System.out.println("Enter the statement to be tested");
			try {
				// Reading from terminal
				InputStream in = System.in;
				InputStreamReader charsIn = new InputStreamReader( in );
				BufferedReader bufferedCharsIn = new BufferedReader(charsIn);
				String line = bufferedCharsIn.readLine();
				
				// Converting the input line to a valid hypothesis
				Hypothesis test_hyp = new Hypothesis(line);
				
				// Testing and printing the hypothesis
				Hypothesis Result = test_hyp.test();
				p.print("The entered statement is found to be "+Result.correct);
			} catch ( IOException e ) {}
	
			
	//		Hypothesis test_hyp = new Hypothesis("Seamus loses");
			
			// Testing and printing the hypothesis
	//		Hypothesis Result = test_hyp.test();
	//		p.print("The entered statement is found to be "+Result.correct);
			
			
	}
	

}

class p{
	int i;
	String s;
	static int space = -1;
	public p(String s){this.s =s;}
	static void print(String s){
		System.out.println();
		for(int i=0;i<space;i++)
			System.out.print("	");
		System.out.print(s);
	}


static void inc(){space++;}
static void dec(){space--;}
}
