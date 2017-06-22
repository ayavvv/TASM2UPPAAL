package test.popup.actions;
import java.util.ArrayList;
import java.util.Hashtable;
public class Machine {
	private String name;
	private ArrayList<Rule> rl;
	private ArrayList<Machine> called;
	private ArrayList<SubMachine> suM;
	Machine(String n){
		name=n;
		rl=new ArrayList();
		called=new ArrayList();
	}
	public String getName(){
		return name;
	}
	public ArrayList<Rule> getRl(){
		return rl;
	}
	public ArrayList<Machine> getCalled(){
		return called;
	}
	public void setSub(ArrayList<SubMachine> asm){
		suM=asm;
	}
	public void changeRules(){//TODO:
		ArrayList<Rule> tempRule=null;
		int mark=0;
		while(true){
			for(Rule ru:rl){
				if(ru.getEffect().indexOf("(")>0){
					tempRule=ru.returnRules(suM);
					if(tempRule!=null){
						for(Rule r:tempRule){
							rl.add(r);
						}
					}
					rl.remove(ru);
					mark=1;
					break;
					
				}
				else{
					Rule temprl=null;
					if(ru.getStack().isEmpty()==false){
						temprl=ru.getStack().get(0);
						for(Rule rl:ru.getStack()){
							if(rl.getMax()>=temprl.getMax()){
								if(rl.getMin()>=temprl.getMin()&&rl.getRt()!=4){
									temprl=rl;
								}
							}
						}
						if(temprl.getRt()!=4){
							ru.setMax(temprl.getMax());
							ru.setMin(temprl.getMin());
							ru.setRt(temprl.getRt());
						}
					}
				}
			}
			if(mark==1){
				mark=0;
			}
			else{
				break;
			}
		}
	}
	public void displayRl(){
		System.out.println("machineName: "+name+"\n");
		if(rl!=null){
			for(Rule r: rl){
				r.display();
			}
		}
	}
	public Boolean addRule(Rule r){
		if(rl==null){
			rl=new ArrayList();
		}
		if(r!=null){
			rl.add(r);
		}
		return true;
	}
	
}
