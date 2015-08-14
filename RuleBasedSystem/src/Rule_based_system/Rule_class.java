package Rule_based_system;

import java.io.IOException;

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

public class Rule_class {

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
