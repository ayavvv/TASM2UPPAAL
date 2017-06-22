package test.popup.actions;
import java.util.ArrayList;
import java.util.Hashtable;
public class SubMachine extends Machine {
	SubMachine(String n){
		super(n);
	}
	public void setRelse(){
		ArrayList<Rule> ar=this.getRl();
		String s="";
		int mark=0;
		for(Rule ru:ar){
			if(ru.getElse().equals("false")){
				if(mark==0){
					s+="!("+ru.getGuard()+")";
					mark=1;
				}
				else{
					s+="&&!("+ru.getGuard()+")";
				}
			}
		}
		for(Rule ru:ar){
			if(ru.getElse().equals("true")){
				ru.setGuard(s);
				break;
			}
		}
	}
	public void setSkip(){
		ArrayList<Rule> ar=this.getRl();
		for(Rule ru:ar){
			if(ru.getEffect()!=null&&(ru.getEffect().equals("skip;")||ru.getEffect().equals("skip"))){
				ru.setEffect("");
			}
		}
	}
}
