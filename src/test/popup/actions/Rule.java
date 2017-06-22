package test.popup.actions;
import java.util.ArrayList;
import java.util.Hashtable;
public class Rule {
	private String guard;
	private String effect;
	private String name="none";
	private String rlString;
	private int rttype=-1;
	private int min=-1;
	private int max=-1;
	private String relse="false";
	private ArrayList<String> ef;
	private ArrayList<Rule> rlStack;
	public static int count=0;
	
	Rule(String n,String g,String e,String rt,String min,String max,String relse){
		guard=g;
		effect=e;
		name=n;
		rttype=Integer.parseInt(rt);
		this.min=Integer.parseInt(min);
		this.max=Integer.parseInt(max);
		this.relse=relse;
		ef=new ArrayList();
		divideEffect();
		rlStack=new ArrayList();
	}
	public String getGuard(){
		return guard;
	}
	public void setGuard(String s){
		guard=s;
	}
	public String getEffect(){
		return effect;
	}
	public void setEffect(String e){
		effect=e;
	}
	public String getName(){
		return name;
	}
	public void setName(String n){
		name=n;
	}
	public ArrayList<String> getEfs(){
		return ef;
	}
	public String getElse(){
		return relse;
	}
	public static int getCount(){
		return count;
	}
	public int getMax(){
		return max;
	}
	public void setMax(int m){
		max=m;
	}
	public int getMin(){
		return min;
	}
	public void setMin(int m){
		min=m;
	}
	
	public int getRt(){
		return rttype;
	}
	public void setRt(int m){
		rttype=m;
	}
	public void setStack(Rule r){
		rlStack.add(r);
	}
	public void catStack(ArrayList<Rule> ar){
		if(ar!=null){
			for(Rule ru:ar){
				setStack(ru);
			}
		}
	}
	public ArrayList<Rule> getStack(){
		return rlStack;
	}
	public void display(){
		rlString="ruleName:"+name+"\n";
		rlString+="rttype="+rttype+" min="+min+" max="+max+"\n";
		if(guard!=null){
			rlString+="guard: ";
			rlString+=guard+"\n";
		}
		else{
			rlString+="guard:null\n";
		}
		if(effect!=null){
			rlString+="effect: ";
			rlString+=effect+"\n";
		}
		else{
			rlString+="effect:null\n";
		}
		//divideEffect();
		
		System.out.println(rlString);
		for(String s: ef){
			System.out.println(s+"\n");
		}
	}
	public void divideEffect(){
		if(effect==null)
			return;
		if(effect.indexOf(';')<0||effect.indexOf(';')==effect.length()-1)
			ef.add(effect);
		else{
			int indexOfSemi=effect.indexOf(";");
			int lastSemi=0;
			String s=effect.substring(0,indexOfSemi).trim();
			
			while(indexOfSemi>=0){
					ef.add(s);//如果此字串不是同步的，则加入到al中
				if(indexOfSemi<effect.length()-1){
					lastSemi=indexOfSemi+1;
					indexOfSemi=effect.indexOf(";",indexOfSemi+1);
					if(indexOfSemi>=0&&indexOfSemi<effect.length()-1)
						s=effect.substring(lastSemi,indexOfSemi).trim();
					else
						break;
				}
			}
			s=effect.substring(lastSemi,effect.length()).replaceAll(";", "").trim();//处理最后一个分号后的字串
			if(s!=null&&(s.indexOf("?")<0&&s.indexOf("!")<0)&&s.equals("")==false)
				ef.add(s);
		}
	}
	public ArrayList<Rule> returnRules(ArrayList<SubMachine> subm){
		String temp="";
		String temp2="";
		ArrayList<Rule> ar=null;
		if(subm==null)
			return null;
		for(String str:ef){ 
			if(str.indexOf("(")>0){
				 temp=str.substring(0,str.indexOf("("));
				 temp2=str;
				 ef.remove(str);
				break;
			}
		}
		for(SubMachine ma:subm){
			String eff="";
			if(ma.getName().equals(temp)){
				ma.setRelse();
				ma.setSkip();
				ar=new ArrayList();
				for(Rule rul:ma.getRl()){
					for(String s:ef){
						eff+=ef;
					}
					
					eff+=temp;
					Rule r=null;
					if(this.rttype!=0)
						r=new Rule(name+rul.getName(),"("+guard+")&&("+rul.getGuard()+")",effect.replace(temp2, rul.getEffect()),((Integer)(rttype)).toString(),((Integer)(min)).toString(),((Integer)(max)).toString(),relse);//String n,String g,String e,String rt,String min,String max,String relse
					else{
						r=new Rule(name+rul.getName(),"("+guard+")&&("+rul.getGuard()+")",effect.replace(temp2, rul.getEffect()),((Integer)(rttype)).toString(),((Integer)(min)).toString(),((Integer)(max)).toString(),relse);
						if(rul.getRt()!=0){
							Rule rr=null;
							if(this.getStack()!=null&&this.getStack().isEmpty()==false)
								rr=this.getStack().get(this.getStack().size()-1);
							if(rr==null){
								r.catStack(this.getStack());
								r.setStack(rul);
							}
							else{
								if(rr.getEffect().indexOf(temp)<0){
									r.catStack(this.getStack());
									r.setStack(rul);
								}
							}
						}
					}
					ar.add(r);
					this.count++;
				}
				
				return ar;
			}
		}
		return null;
	}
}
