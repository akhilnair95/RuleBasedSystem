package Rule_based_system;

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
	
}class Pointer_class{

