package Rule_based_system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

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
