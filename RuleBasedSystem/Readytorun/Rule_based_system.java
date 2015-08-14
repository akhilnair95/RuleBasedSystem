import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class Single_rule{
	Hypothesis Reason;
	Hypothesis[] Result;
}

class Rule_class {

	static Single_rule[] rules;
	
	public Rule_class() {
		rules = this.get_Rules();
		// TODO Auto-generated constructor stub
	}

	
	
// ----------------------------------------------------------------------------------------------------------------------------
//methods for reading rules.xml and setting the vals of Single_rule	corresp to each rule in rules.xml

	private  Single_rule[] get_Rules() {
		// TODO Auto-generated method stub
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Single_rule[] new_rules = new Single_rule[0];
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			try {
				Document doc = builder.parse("rules.xml");
				Node root = doc.getFirstChild();
				Element e = (Element) root;
				NodeList Rules = e.getElementsByTagName("Rule");
				new_rules = new Single_rule[Rules.getLength()];
				for(int i=0;i<Rules.getLength();i++){
					Node n = Rules.item(i);
					Element ele = (Element) n;
					new_rules[i] = this.create_rule(ele);
				}
			} catch (SAXException e) {e.printStackTrace();} 
			catch (IOException e) {	e.printStackTrace();}
		} catch (ParserConfigurationException e) {e.printStackTrace();}
		
		return new_rules;
	}



	private  Single_rule create_rule(Element ele) {
		// TODO Auto-generated method stub
		Single_rule Rule = new Single_rule();
		NodeList Reason_nodes = ele.getElementsByTagName("Reason");
		Element reason = (Element) Reason_nodes.item(0);
		Element reason_Hyp = (Element)(reason.getElementsByTagName("Hypothesis").item(0));
		Rule.Reason = add_reason(reason_Hyp);
		Node Result_node = ele.getElementsByTagName("Result").item(0);
		NodeList statements = ((Element)Result_node).getElementsByTagName("Statement");
		Rule.Result = new Hypothesis[statements.getLength()];
		for(int i=0;i<statements.getLength();i++){
			Rule.Result[i] = new Hypothesis();
			Rule.Result[i].simple = true;
			Rule.Result[i].statement = statements.item(i).getTextContent();
		}
		//if(!Rule.Reason.simple)
			//p.print(Rule.Reason.sub_hypothesis[1].sub_hypothesis[1].statement);
		return Rule;
	}



	private static Hypothesis add_reason(Element hyp) {
		// TODO Auto-generated method stub
		Hypothesis h = new Hypothesis();
		if(hyp.getAttribute("Type").matches("Simple")){
			h.simple = true;
			Element stat = (Element)((hyp.getElementsByTagName("Statement")).item(0));
			h.statement = stat.getTextContent();
			//p.print(h.statement);
		}
		else{
			h.simple = false;
			h.gate = hyp.getAttribute("Gate");
			//p.print(h.gate);
			NodeList a = hyp.getChildNodes();
			int num = 0;
			for(int i=0;i<a.getLength();i++){
				Node n = a.item(i);
				if(n.getNodeType() == Node.ELEMENT_NODE){
					num++;
				}
			}
			h.sub_hypothesis= new Hypothesis[num];
			int j=0;
			for(int i=0;i<a.getLength();i++){
				Node n = a.item(i);
				if(n.getNodeType() == Node.ELEMENT_NODE){
					h.sub_hypothesis[j] = add_reason((Element)n);
					j++;
				}
			}
			
			
		}
		return h;
	}

	
	
//------------------------------------------------------------------------------------------------------------------------------	
/* Methods fo hypothesis testing based on rules
	
	Method   public static Hypothesis[] find_matchings(String statement) {
	
		 * Returns all rules corresp to statement as array of reason hypothesis with corresp pointers
		 * eg: statement -  :x likes ice
		 * Rules
		 * a. If (1)
		 * 		Then  ...........
		 *            ?y likes ice
		 *            ............
		 * b. If (2) 	
		 * 		      Then  ...........
		 *            ?x likes ice
		 *            ............
		 * O/p
		 * Result[0] - (1) pointers y -> x
		 * Result[1] - (2) x -> x
		 * 
		 *            
		 * Algm
		  	traverse through all rules
		  	if consequent statmnt of rule i matches with i/p statement 
		  		append result array with reason i
		  		set pointers accordingly 
		    return result array 
		 * 
*/
	
	
	
	
	public static Hypothesis[] find_matchings(String statement) {
		// Refer above for documentation
		
		String[] stat_words = statement.split(" ");
		boolean flag;
		Hypothesis[] matches = new Hypothesis[0];
		for(int i=0;i<Rule_class.rules.length;i++){        //  For each rule
			Hypothesis[] res = Rule_class.rules[i].Result;
			for(int j=0;j<res.length;j++){                 // for each consequent of rule i
					res[j].pointers = new Pointer_class(); // setting to '0'just in case
					flag = true;	
					String rule_stat = res[j].statement;
					String[] rule_words = rule_stat.split(" ");
					if(rule_words.length!=stat_words.length){ flag = false; break;}
					
					Hypothesis h = new Hypothesis();
					for(int k= 0;k<rule_words.length;k++){       // for each word
						
							if(rule_words[k].matches(stat_words[k]) && !rule_words[k].startsWith(":"))
																	continue;//matching non variable phrase
						
							else if(rule_words[k].startsWith(":") && stat_words[k].startsWith(":")){ 
													//matching variable phrase set pointers
								if(rule_words[k].matches(":x"))
									h.pointers.x = stat_words[k].charAt(1);
								else if(rule_words[k].matches(":y"))
									h.pointers.y = stat_words[k].charAt(1);
								else if(rule_words[k].matches(":z"))
									h.pointers.z = stat_words[k].charAt(1);
							}
							else { 				// mismatch . not found in consequent j break from inner inner loop check fo nxt cnsqnt
								flag = false;
								break;
							}
					}
					if(flag){     							 // statement present in jth conseuent of result i
						Rule_class.rules[i].Reason.pointers = h.pointers;
						matches = Hypothesis.concat(matches,Rule_class.rules[i].Reason); // appends reason of rule i with matches
						break;    // consequent j satisfied so continue to next rule
					}
			}
		}
		return matches;
	}



	public static Hypothesis test_single_rule(Hypothesis rule_matches, Hypothesis hyp) {
		// TODO Auto-generated method stub
		
		Pointer_class p = rule_matches.pointers;
		Bindage_class[] temp = new Bindage_class[hyp.Bindages.length];
		
		for(int i=0;i<hyp.Bindages.length;i++){        // verum show
			temp[i] = new Bindage_class();
			temp[i].copy(hyp.Bindages[i]);
		}
		
		Bindage_class new_binds[] = Bindage_class.mix_and_match(temp,p);	// matches bindages according to pointers
		rule_matches.Bindages = new_binds;
		
		Hypothesis rule_result = rule_matches.test();
		
		if(rule_result.correct){        // reverse the mix and match effect then return
			p.Inverse();			// get original bindages back 
			rule_result.Bindages = Bindage_class.get_intersection(Bindage_class.mix_and_match(rule_result.Bindages,p), hyp.Bindages);
																									// fill out missing bindages
		}
		else
			rule_result.correct = false;
		
		return rule_result;
	}



	public static Hypothesis test(Hypothesis hyp) {
		// TODO Auto-generated method stub
		Hypothesis rule_test_result = new Hypothesis();
		Hypothesis[] rule_matches = Rule_class.find_matchings(hyp.statement);     // Test in rules
		Bindage_class[] dummy_bind= new Bindage_class[0];
		if(rule_matches.length != 0){                           // if found in any 1 of rules 
				for(int i=0;i<rule_matches.length;i++){
					Hypothesis h = Rule_class.test_single_rule(rule_matches[i],hyp);
					if(h.Bindages != null)
					dummy_bind = Bindage_class.get_union(dummy_bind, h.Bindages);//if found in more than 1 rule union
				}

				if(dummy_bind.length == 0)
						rule_test_result.correct = false;
					
					else{
						rule_test_result.correct = true;
						rule_test_result.Bindages = dummy_bind;
					}
			}
			else                                      // Hypothesis stst not in rules . so false;	
				rule_test_result.correct = false;
		return rule_test_result;
	}
}






class Pointer_class{

	char x;
	char y;
	char z;
	
	public Pointer_class(){
		x = y = z ='0';
	}

	public void Inverse() {
		// TODO Auto-generated method stub
		char t_x,t_y,t_z;
		t_x = t_y = t_z = '0';
		if(x != '0'){
			if(x == 'y')
				t_y = 'x';
			else if(x == 'z')
				t_z = 'x';
			else
				t_x = 'x';
		}
		if(y != '0'){
			if(y == 'x')
				t_x = 'y';
			else if(y == 'z')
				t_z = 'y';
			else
				t_y = 'y';
		}
		if(z != '0'){
			if(z == 'y')
				t_y = 'z';
			else if(z == 'z')
				t_z = 'z';
			else
				t_x = 'z';
		}
		
		this.x = t_x; this.y = t_y; this.z = t_z;
	}
	
}

class Bindage_class{
	String x_bind;
	String y_bind;
	String z_bind;
	
	public Bindage_class(){
	}
	
	boolean equals(Bindage_class a){
		if(this.x_bind != a.x_bind)
			return false;
		if(this.y_bind != a.y_bind)
			return false;
		if(this.z_bind != a.z_bind)
			return false;
		
		return true;
	}
	
	boolean check_no_conflict(Bindage_class bin){
		/* 
		 * i/p - bin
		 * checks if bin & contents of this class has conflict 
		 * ie diff non null values in x y or z
		 * if no conflict - true
		 */
		boolean x,y,z;
		x = y = z = false;
		
		if(this.x_bind != null && bin.x_bind!=null){
			if(this.x_bind.matches(bin.x_bind))
				x = true;
		}
		else x = true;
		
		if(this.y_bind != null && bin.y_bind!=null){
			if(this.y_bind.matches(bin.y_bind))
				y = true;
		}
		else y = true;
		
		if(this.z_bind != null && bin.z_bind!=null){
			if(this.z_bind.matches(bin.z_bind))
				z = true;
		}
		else z = true;
		
			return (x && y && z);
	}
	
	static boolean check_intersection(Bindage_class[] a, Bindage_class[] b){
		if(a == null || b == null)
			return false;
		
		for(int i=0;i<a.length;i++){
			for(int j=0;j<b.length;j++)
				if(a[i].check_no_conflict(b[j]))
					return true;
		}
		return false;
		}
	
	static Bindage_class[] get_intersection(Bindage_class[] a, Bindage_class[] b){
		Bindage_class[] intersection = new Bindage_class[0];
		Bindage_class dummy;
		for(int i=0;i<a.length;i++){
			for(int j=0;j<b.length;j++)
				if(a[i].check_no_conflict(b[j])) // Append intersection
				{
					dummy = Bindage_class.merge(a[i],b[j]);
					intersection = Bindage_class.concat(intersection,dummy);
				}
		}
		return intersection;
		}
	
	static Bindage_class[] get_union(Bindage_class[] a, Bindage_class[] b){
			/*
			 * Output is union of a anb b
			 * ie elemnts of a anb b combined after removing the duplicates
			 * 
			 * 		a[]							b[]					   output
			 *  Hari    Ron  *			   Hari  *   Giri		    Hari    Ron  *	        			 * -> Dont care (null)
			 *	Hari 	*	 *			   Hari Ron  *			    Hari  *   Giri
			 *														Hari 	*	 *
			 */
		Bindage_class[] union = new Bindage_class[0];
		boolean flag;
		for(int i=0;i<a.length;i++){
			flag = false;
			for(int j=0;j<b.length;j++)
				{
				 if(a[i].equals(b[j])){  
					 flag = true;
					 break;
					 }
				}
			if(!flag) // ie element a[i] not found in b So append
			   union = 	Bindage_class.concat(union,a[i]);
			
		}
		
		//if(b!=null)
		for(int j=0;j<b.length;j++)
			union = Bindage_class.concat(union,b[j]);
		return union;
	}
	

	
	private static Bindage_class[] concat(Bindage_class[] a,Bindage_class dummy) {
		// TODO Auto-generated method stub
		Bindage_class[] temp = new Bindage_class[a.length+1];
		for(int i=0;i<a.length;i++)
			temp[i] = a[i];
		temp[a.length] = dummy;
		return temp;
	}

	private static Bindage_class merge(Bindage_class a,Bindage_class b) {
		// TODO Auto-generated method stub
		Bindage_class fin = new Bindage_class();
	
				if(a.x_bind == null)
					fin.x_bind = b.x_bind;
				else
					fin.x_bind = a.x_bind;

				if(a.y_bind == null)
					fin.y_bind = b.y_bind;
				else
					fin.y_bind = a.y_bind;

				if(a.z_bind == null)
					fin.z_bind = b.z_bind;
				else
					fin.z_bind = a.z_bind;
			
			return fin;
				
	}

	String make_bound(String line){        // Input - line o/p-line with variables and associated bindage
	  // Algm - traverse through words in line. Replace nouns with x , y and assosiates bindage
		String[] words = line.split(" ");
		int count = 0 ;
		for(int i=0;i<words.length;i++)
		{
			if(Assertion_class.is_noun(words[i]))
			{
				if(count == 0)
				{
					x_bind = words[i];
					 line = line.replaceAll(words[i], ":x");
				}
				else if(count == 1)
				{
					y_bind = words[i];
					line = line.replaceAll(words[i], ":y");
				}
				if(count == 2)
				{
					z_bind = words[i];
					line = line.replaceAll(words[i], ":z");
				}
				count++;
			}
		}

		return line;
	}
	
	
	static String make_line(String line, Bindage_class b){        // Input - line o/p-line with variables and associated bindage
		  // Algm - traverse through words in line. Replace nouns with x , y and assosiates bindage

			if(b.x_bind != null)
				line = line.replaceAll(":x", b.x_bind);
			if(b.y_bind != null)
				line = line.replaceAll(":y", b.y_bind);
			if(b.z_bind != null)
				line = line.replaceAll(":z", b.z_bind);

			return line;
		}

	public static Bindage_class[] mix_and_match(Bindage_class[] Bindages, Pointer_class p) {
		// TODO Auto-generated method stub
		Bindage_class[] temp = new Bindage_class[Bindages.length];
		
		for(int i=0;i<Bindages.length;i++)
		{
			temp[i] = new Bindage_class();
		}
		if(p.x != '0'){         // if not no need to change contents of x
			if(p.x == 'x'){
					for(int i = 0; i<temp.length;i++)
						temp[i].x_bind = Bindages[i].x_bind;
					}
				else if(p.x == 'y')
					for(int i = 0; i<temp.length;i++)
						temp[i].x_bind = Bindages[i].y_bind;
				else
					for(int i = 0; i<temp.length;i++)
						temp[i].x_bind = Bindages[i].z_bind;
		} 
		if(p.y != '0'){         // if not no need to change contents of x
			if(p.y == 'x'){
					for(int i = 0; i<temp.length;i++)
						temp[i].y_bind = Bindages[i].x_bind;
					}
				else if(p.y == 'y')
					for(int i = 0; i<temp.length;i++)
						temp[i].y_bind = Bindages[i].y_bind;
				else
					for(int i = 0; i<temp.length;i++)
						temp[i].y_bind = Bindages[i].z_bind;
		} 
		if(p.z != '0'){         // if not no need to change contents of x
			if(p.z == 'x'){
					for(int i = 0; i<temp.length;i++)
						temp[i].z_bind = Bindages[i].x_bind;
					}
				else if(p.z == 'y')
					for(int i = 0; i<temp.length;i++)
						temp[i].z_bind = Bindages[i].y_bind;
				else
					for(int i = 0; i<temp.length;i++)
						temp[i].z_bind = Bindages[i].z_bind;
		} 
		return temp;
	}

	public void copy(Bindage_class bind) {
		// TODO Auto-generated method stub
			this.x_bind = bind.x_bind;
			this.y_bind = bind.y_bind;
			this.z_bind = bind.z_bind;
		
	}
	
}








class Assertion_class{
	static String[] nouns;
	static String[] statements;
	static int statement_count;
	
	public Assertion_class(){
				statement_count = 0;
				statements = new String[1000];
				read_file();
	}
	
	
	static Hypothesis find_matchings(Hypothesis h){  
		
							/*
						    Return all assrns matching hypothesis statement of h
						    as a hypothesis with with same statem as h (does not care about bindings of h)
						    and bindings corresp to assertion statements with same statement line.
						    
						    eg - Check for: ?x flies
						         Assns : Daisy flies , George flies
						         Returns : ?x flies  ?x : Daisy,George  
						 */
						
		Hypothesis Dummy = new Hypothesis();   
		Dummy.simple = true;
		Dummy.found = false;										// Set to false just for safety (It is already set in constrctr)
		boolean flag;
		int match_count = 0;
		
		for(int i=0;i<statement_count;i++){                            // loops to all assertion statements
	            flag = true;
	            String x = null,y = null,z = null;
	            
			    String assn = statements[i];
	            String stat_words[] = h.statement.split(" ");
	            String assn_words[] = assn.split(" ");
	           
	            if(stat_words.length!=assn_words.length)
	             continue;
	            
	            for(int j=0;j<stat_words.length;j++){                // iterates through all words in the i'th assern statement
		            	if(stat_words[j].matches(assn_words[j])){
		            		continue;
		            	}
		            	else{
			             		    if(stat_words[j].matches(":x"))      x = assn_words[j];
				            		else if(stat_words[j].matches(":y")) y = assn_words[j];
				            		else if(stat_words[j].matches(":z")) z = assn_words[j];
				            		else{   				// mismatch 
				            				flag = false;
				            				break;
				            		}
		            	}
	            }
	            
	            if(flag){         //If statem matches assn i then associate bindages to dummy Hypothesis
			            	match_count++;
		            		Bindage_class[] bin = new Bindage_class[match_count];
			            	for(int g=0;g<bin.length-1;g++){
			            		bin[g] = Dummy.Bindages[g];
			            	}
			            	Bindage_class new_bin = new Bindage_class();
			            	new_bin.x_bind = x;
			            	new_bin.y_bind = y;
			            	new_bin.z_bind = z;
			            	bin[match_count-1] = new_bin;
			                Dummy.statement = h.statement;
			                Dummy.found = true;								// sets found (initially false to true)
			                Dummy.Bindages = bin;
			                
	            }
	            
	            		}
		
		
		return Dummy;
	}
	
	

	
	
	static Hypothesis test(Hypothesis h){
		/*
		 i/p hyp with bindings o/p- Assertion matching hyp
		 Algm -
			get matching hyps(from find_matchings() method) 
			Test for intersection in bindings.
				if (intersection)
					set found = true
					set bindage = intersection
				else
					do nothing (found set to false by default)
			return found, Bindages 		
		*/
		Hypothesis matchings = Assertion_class.find_matchings(h);		// returns all bindages corresp to h.statement
		
		//for(int i=0;i<matchings.Bindages.length;i++)
		//	p.print(Bindage_class.make_line(h.statement, matchings.Bindages[i]));
		
		 Hypothesis result = new Hypothesis();                                                                
		 result.statement = h.statement;
		 if(matchings.found){									// if hyp stat matches any 1 assn
				 boolean intrscn_chk = (Bindage_class.check_intersection(h.Bindages, matchings.Bindages));//check for intrscn in bindages
				 if(intrscn_chk){										// If Valid intrscn found
					 	Bindage_class[] intersection = (Bindage_class.get_intersection(h.Bindages, matchings.Bindages));
					 	result.found = true;
					 	result.Bindages = intersection;				// sets all necessafry thngs
					 }
				 }
		 else
			 ;
		 return result;
	}
	
	
	
	
	
	@SuppressWarnings("static-access")
	private void read_file(){   
				// Reads the Assertions.txt file and saves the assertion statements to the array statements
		File file = new File("Assertions.txt");
		BufferedReader in;
		int i=0;
		if ( !file.exists() || !file.canRead() ) {
			System.out.println( "Can't read " + file );
			return;
		}
		else{
				try {
					Reader ir = new InputStreamReader(new FileInputStream( file ) );
					in = new BufferedReader( ir );
					String line;
						try {
							line = in.readLine(); // read the first line containing nouns
							set_nouns(line);
							while ((line = in.readLine()) != null)
								{statements[i++] = line; this.statement_count++;}
						} catch (IOException e) { e.printStackTrace(); }
				}
						catch ( FileNotFoundException e ) {
						System.out.println( "File Disappeared" );
					}
				}
	}
	
	
	
	@SuppressWarnings("static-access")
	private void set_nouns(String line){
			/* Input - a string whose contents are in the format
			 * Nouns are <Noun1> <Noun2> ..... <Noun n>
			 * 
			 *  This function stores each of nouns in ip string to nouns array
			 */
		String[] words = line.split(" ");
		this.nouns = new String[words.length - 2];
		for(int i=2;i<words.length;i++)
			{
			nouns[i-2] = words[i];
			}
	}
	
	static boolean is_noun(String s){
		for(int i=0;i<nouns.length;i++){
			if(s.matches(nouns[i]))
				return true;
		}
		return false;
	}
}

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
