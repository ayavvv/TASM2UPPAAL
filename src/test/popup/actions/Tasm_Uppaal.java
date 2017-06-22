
package test.popup.actions;




import static com.atl.common.models.Models.get;
import static com.atl.common.models.Models.inject;
import static com.atl.common.models.Models.register;
import static com.atl.common.models.Models.resource;
import static com.atl.common.trans.Transformations.transform;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.internal.adaptor.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.runtime.adaptor.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.internal.*;
import org.eclipse.core.runtime.internal.stats.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.common.ATLResourceProvider;
//import org.eclipse.core.runtime.jobs;
import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.io.*;

import javax.xml.transform.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.m2m.atl.core.emf.EMFModel;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.atl.common.trans.OneInOneOutTransformation;
import com.atl.common.trans.Transformations;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.util.*;

//执行cmd
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IResource;

public class Tasm_Uppaal implements IObjectActionDelegate {
	static DocumentBuilder domBuilder;
	static Document document;
	static DocumentBuilderFactory  domFactory;
	static Document document2;
	static Hashtable ht;//(变量名，变量类型）
	static Hashtable localvar;
	static FileReader filerder;
	static BufferedReader bufferedrder;
	static String upStr=null;
	static ArrayList<MainMachine> amm=new ArrayList();
	static ArrayList<SubMachine> asm=new ArrayList();
	static ArrayList<String> tempName=new ArrayList();
	/**
	 * Constructor for Action1.
	 */
	public Tasm_Uppaal() {
		super();
	}
	org.eclipse.core.resources.IFile iFile;
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	public void refresh(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
		IProject[] projects = workspace.getRoot().getProjects(); 
		   for (IProject project : projects) { 
		     try { 
		       project.refreshLocal(IResource.DEPTH_INFINITE, null); 
		     } catch (CoreException e1) { 
		     }
		} 
	}
	private static void addText_a(Node n){
		if(n.getNodeType()==1){//若此节点的类型为element则对于其所有子节点递归调用此方法
			if(n.getNodeName().equals("type")){
				String t=n.getAttributes().getNamedItem("tname").getNodeValue();
				tempName.add(t);
			}
			for(int i=0;i<n.getChildNodes().getLength();i++){
				addText_a(n.getChildNodes().item(i));
			}
		}
		else if(n.getNodeType()==3&&(n.getNodeValue().trim().equals("")==false)){//如果此节点为text类型并且不是由空格、tab、回车构成的空字符串
			Node f=n.getParentNode();//获取其父节点
			if(f!=null){
				Node ff=f.getParentNode();
				Element e=(Element)f;
				Element ee=document.createElement(f.getNodeName());
				for(int i=0;i<e.getAttributes().getLength();i++){
					ee.setAttribute(e.getAttributes().item(i).getNodeName(), e.getAttributes().item(i).getNodeValue());
					
				}
				ee.setAttribute("text_a", n.getNodeValue());//添加text_a属性
				f.removeChild(n);
				//ff.removeChild(f);
				
				if(ff!=null)
					ff.replaceChild(ee,f);//将复制出并添加text_a属性的节点替换源节点
			}
		}
		else
			return;
	}
	private static void removeText_a(Node n,Document d){
		Node add;
		String value;
		String labelKind;
		if(n.getNodeType()==1){
			for(int i=0;i<n.getAttributes().getLength();i++){
				//首先判断是否存在属性text_a
				if(n.getAttributes().item(i).getNodeName()=="text_a"){
					
					value=n.getAttributes().item(i).getNodeValue();//获取text_a中的内容
					n.getAttributes().removeNamedItem("text_a");//移除text_a属性
					if(n.getNodeName().equals("label")){//若为label类型节点则进行处理
						add=d.createTextNode(changeFormat(getTemplateName(n),n,value));//使用changeFormat函数进行处理,通过调用getTemplateName方法可以获得此label节点所在template的名字并以此打开存有local变量信息的文件
						n.appendChild(add);
						localvar=null;
						
						break;
					}
					else{//如果不是label类型节点直接生成
						add=d.createTextNode(value);
						n.appendChild(add);
						break;
					}
				}
			}
			//递归操作
			for(int i=0;i<n.getChildNodes().getLength();i++){
				removeText_a(n.getChildNodes().item(i),d);
			}
		}
		
	}
	//需要找到label节点对应的template名以打开记载有此template局部变量对应值的文件
	private static String getTemplateName(Node n){
		Node f=n.getParentNode().getParentNode();//因为是对label寻找template所以理论上不会出现exception
		if(f!=null){
			System.out.println("true");
			System.out.println(f.getNodeName());
		}
		Node c;
		for(int i=0;i<f.getChildNodes().getLength();i++){
			if(f.getChildNodes().item(i).getNodeName().equals("name")){
				
				c=f.getChildNodes().item(i);
				System.out.println(c.getNodeName());
				System.out.println(c.getChildNodes().item(0).getNodeValue());
				for(int k=0;k<c.getChildNodes().getLength();k++){
					if(c.getChildNodes().item(k).getNodeType()==3&&c.getChildNodes().item(k).getNodeValue().isEmpty()==false)
						return c.getChildNodes().item(0).getNodeValue();
				}
				
				
			}
		}
		return null;
	}
	//TODO:
	private static String changeFormat(String filename,Node kind,String value){
		String k;
		int index;
		String tmpKey=null;
		ArrayList al,al2;
		String v="";
		int tabPos=0;
		if(kind.getNodeName().equals("label")==false)
		return value;
		else{
			try{
				//将template内变量输入到localvar中
				System.out.println(filename);
				if(filename!=null&&filename.indexOf("ELSERULE_")<0){//若以ELSERULE_开头则视为else类型template无此种文件
					filerder=new FileReader(filename+".txt");
					bufferedrder=new BufferedReader(filerder);
					String Line=bufferedrder.readLine();
					//ht=new Hashtable();
					localvar=new Hashtable();
					while(Line!=null){
						tabPos=Line.indexOf('\t');
						if(tabPos>=0){
							tmpKey=Line.substring(0, tabPos).trim();
							System.out.println("tmp:"+tmpKey);
							v=Line.substring(tabPos,Line.length()).trim();
							System.out.println("v"+v);
							localvar.put(tmpKey, v);
						}
						Line=bufferedrder.readLine();
					}
					filerder.close();
					bufferedrder.close();
					
						
				}
				for(int i=0;i<kind.getAttributes().getLength();i++){
					if(kind.getAttributes().item(i).getNodeName().equals("kind")){
						k=kind.getAttributes().item(i).getNodeValue();
						//当为invariant时，不用做任何操作直接返回
						if(k.equals("invariant")){//invariant
							return value;
						}
						else{
							//当为同步label时，将字符串value中的同步语句字串提取出来并返回
							if(k.equals("synchronisation")){//synchronisation
								if(value.indexOf(";")<=0)
									return value;
								else{
									//根据分号分隔字符串
									int indexOfSemi=value.indexOf(";");
									int lastSemi=0;
									String s=value.substring(0,indexOfSemi);
									while(indexOfSemi>=0){
										if(s.indexOf("!")>0||s.indexOf("?")>0){//如果发现同步则返回
											return s;
										}
										if(indexOfSemi<value.length()-1){
											lastSemi=indexOfSemi+1;
											indexOfSemi=value.indexOf(";",indexOfSemi+1);
											if(indexOfSemi>=0&&indexOfSemi<value.length()-1)
												s=value.substring(lastSemi,indexOfSemi);
											else
												break;
										}
									}
									s=value.substring(lastSemi,value.length());//最后一个分号后的字符串
									System.out.println(s);
									if(s!=null&&(s.indexOf("?")>0||s.indexOf("!")>0))
											return s;
								 }
								return null;
							}
							else if(k.equals("guard")){//guard
								int u=0;
								String t="";
								int uu;
								String lString="";
								String rString="";
								String returnString="";
								char [] tmc;
								int indexOfEqual=0;
								int mark=0,mark1=0,mark2=0;
								char [] c=new char[value.length()];
								char [] tmpchar;
								char [] stringchar;
								ArrayList c2=new ArrayList();
								al=new ArrayList();
								String tString="";
								c=value.toCharArray();
								for(int j=0;j<c.length;j++){
									if(c[j]=='='&&c[j-1]!='>'&&c[j-1]!='<'&&c[j-1]!='!'&&c[j-1]!='='){//当前字符为‘=’且不是‘>=''<=''!='时
										if(c[j+1]!='=')
											c2.add('=');
										
									}
									c2.add(c[j]);
								}
								
								Character [] c3=new Character[c2.size()];
								char []c4=new char[c2.size()+1];
								for(int j=0;j<c2.size();j++){
									c3[j]=(Character)c2.get(j);
									c4[j]=(char)c3[j];//c4为字符串的字符数组
								}
								c4[c2.size()]=' ';
								t=new String(c4);//t为value将=换为==的版本
								System.out.println("t:"+t);
								//TODO:将字符串替换为数值
								tmpchar=new char[t.length()];
								stringchar=new char[t.length()];
								lString=null;rString=null;
								mark2=0;
								for(int j=0,w=0,m=0;j<c4.length;j++){
									if(Character.isLetterOrDigit(c4[j])||c4[j]=='_'){//按照C语言的变量名标准确定变量名及值名
										System.out.println(j+":"+c4[j]);
										if(mark==0){//mark为0且碰到第一个字母或下划线时mark值置1
											w=0;
											tmpchar=new char[t.length()];
											tmpchar[w++]=c4[j];
											mark=1;
											if(mark1==1){//mark1为1时为碰到右值的第一个字符，需要将前面的字符拼接到返回字符串中
												//TODO:mark2 - j 字符串
												tmc=new char[j-mark2];
												for(u=0,uu=mark2;uu<j;uu++){
													tmc[u++]=c4[uu];
												}
												String str=new String(tmc);
												returnString+=str;//拼接字符串
												
											}
											
										}
										else{
											tmpchar[w++]=c4[j];
										}
										
									}
									else{
										System.out.println(j+":"+c4[j]);
										
										if(mark==1){//mark值为1说明在字符输入状态此时碰到不是字母数字或下划线的字符说明碰到一个名字（变量或值名的）的结尾
											
											mark=0;
											char [] cc=new char[w];
											for(int ff=0;ff<w;ff++){
												cc[ff]=tmpchar[ff];
											}
											tString=new String(cc);//caution:不确定是否正确
											System.out.println(j+":"+tString);
											////////////////////////////////////
											if(lString!=null){
												mark1=0;
												mark2=j;
												rString=tString;
												if(localvar!=null&&localvar.keySet().contains(lString)==true){//先在局部变量中找，如果发现变量则替换值
													rString=replaceString((String)ht.get(lString),rString);
													
								
												}
												else if(ht!=null&&ht.keySet().contains(lString)==true){//如果局部变量中没有则在全局变量中找
													rString=replaceString((String)ht.get(lString),rString);
													
												}
												returnString+=' '+rString;
												lString=null;
											}
											
											else if(lString==null&&(localvar!=null&&(localvar.keySet().contains(tString)==true)||ht!=null&&ht.keySet().contains(tString)==true)){
												lString=tString;
												mark1=1;
											}
											else
												lString=null;
												rString=null;
											//////////////////////////////////////////////////////////
												/*
											if(mark1==0 && !tString.equals("and")&&!tString.equals("or")){//如果mark1为0，说明tString为左边
												lString=tString;//赋给lString
												mark1=1;
											}
											else{
												rString=tString;
												//TODO:寻找并替换拼接到returnString上
												if(localvar!=null&&localvar.keySet().contains(lString)==true){//先在局部变量中找，如果发现变量则替换值
													rString=replaceString((String)ht.get(lString),rString);
													
								
												}
												else if(ht!=null&&ht.keySet().contains(lString)==true){//如果局部变量中没有则在全局变量中找
													rString=replaceString((String)ht.get(lString),rString);
													
												}
												returnString+=' '+rString;
												lString="";
												rString="";
												mark2=j;
												mark1=0;
											}*/
								
										}
									}
								}
								tmc=new char[c4.length-mark2];
								for(u=0,uu=mark2;uu<c4.length;uu++){
									tmc[u++]=c4[uu];
								}
								String str=new String(tmc);
								//str=str.replaceAll("||", "||\n");//2011.2.11添加换行符
								//str=str.replaceAll("&&", "&&\n");//2011.2.11添加换行符
								returnString+=str;//拼接字符串
								char[] rs=returnString.toCharArray();
								int nor=0;
							    for(u=0;u<rs.length;u++){
							    	if(rs[u]==')')
							    		nor++;
							    }
							    char[] rss=new char[rs.length+nor];
							    for(u=0,uu=0;u<rs.length;u++){
							    	rss[uu++]=rs[u];
							    	if(rs[u]==')')
							    		rss[uu++]='\n';
							    
							    }
								returnString=new String(rss);
								return returnString;
							
								
							}
							else{//assignment
								if(value.indexOf(";")<=0||value.indexOf(";")==value.length()-1){//当只有一条语句时
									System.out.println("ass");
									if(value.indexOf("?")>0||value.indexOf("!")>0)//如果为同步类型，则返回空字符串，assignment的值即为空，在UPPAAL中将无显示
										return "";
									else{//否则此字符串为赋值语句，将':'去掉
										int lastSemi=0;
										int indexOfSemi=0;
										String tmp=(String)(value);
										indexOfSemi=tmp.indexOf(":=");
										if(indexOfSemi<=0)//此赋值语句使用=而非：=说明是在atl中生成的赋值语句
											return value;
										tmp=tmp.replaceAll(";", "").trim();
										String s1=tmp.substring(0,indexOfSemi).trim();
										String s2=tmp.substring(indexOfSemi+2,tmp.length()).replaceAll(";", "").trim();
										System.out.println("s1:"+s1);
										System.out.println("s2:"+s2);
										if(localvar!=null&&localvar.keySet().contains(s1)==true){//先在局部变量中找，如果发现变量则替换值
											System.out.println("true1");
											tmp=tmp.replaceAll(s2, replaceString((String)localvar.get(s1),s2));
											//return tmp;
											return tmp.replaceAll(":","");
											
										}
										if(ht!=null&&ht.keySet().contains(s1)==true){//如果局部变量中没有则在全局变量中找
											String ss=(String)ht.get(s1);
											System.out.println("true2");
											String sss=replaceString(ss,s2);
											tmp=tmp.replaceAll(s2, replaceString((String)ht.get(s1),s2));
											System.out.println(sss);
											System.out.println(s2);
											System.out.println(tmp);
											//return tmp;		
										}
										return tmp.replaceAll(":","");
									}
								}
								else{
									al=new ArrayList();//将以分号分隔的各赋值语句字符串分开存入到al中，将其进行处理最后在拼接回去，分隔符变为‘，’
									
									int indexOfSemi=value.indexOf(";");
									int lastSemi=0;
									String s=value.substring(0,indexOfSemi).trim();
									System.out.println("s:"+s);
									
									while(indexOfSemi>=0){
										if(s.indexOf("!")<0&&s.indexOf("?")<0){
											al.add(s);//如果此字串不是同步的，则加入到al中
										}
										
										if(indexOfSemi<value.length()-1){
											lastSemi=indexOfSemi+1;
											indexOfSemi=value.indexOf(";",indexOfSemi+1);
											if(indexOfSemi>=0&&indexOfSemi<value.length()-1)
												s=value.substring(lastSemi,indexOfSemi).trim();
											else
												break;
										}
									}
									s=value.substring(lastSemi,value.length()).replaceAll(";", "").trim();//处理最后一个分号后的字串
									if(s!=null&&(s.indexOf("?")<0&&s.indexOf("!")<0)&&s.equals("")==false)
										al.add(s);
									for(int j=0;j<al.size();j++)
										System.out.println("al:"+(String)al.get(j));
									al2=new ArrayList();
									for(int j=0;j<al.size();j++){//对在al中的每一个字串做替换
										String tmp=(String)(al.get(j));
										indexOfSemi=tmp.indexOf(":=");
										if(indexOfSemi<=0){
											al2.add(tmp);//2011.3.22 修改，当赋值语句为‘=’时，直接复制到al2中
											//此赋值语句使用=而非：=说明是在atl中生成的赋值语句
											continue;
										}
											
										String s1=tmp.substring(0,indexOfSemi).trim();
										String s2=tmp.substring(indexOfSemi+2,tmp.length()).trim();
										System.out.println("s1:"+s1+":s2:"+s2);
										if(localvar!=null&&localvar.keySet().contains(s1)==true){//先在局部变量中找，如果发现变量则替换值
											tmp=tmp.replaceAll(s2, replaceString((String)localvar.get(s1),s2));
											al2.add(tmp);
											continue;
										}
										if(ht!=null&&ht.keySet().contains(s1)==true){//如果局部变量中没有则在全局变量中找
											System.out.println("s2true"+tmp);
											tmp=tmp.replaceAll(s2, replaceString((String)ht.get(s1),s2));
											al2.add(tmp);
											continue;
										}
										al2.add(tmp);
										//如果都没有说明此字符串没有对应值，为普通变量不做替换
									}
									for(int j=0;j<al2.size();j++)
										System.out.println("al2:"+(String)al2.get(j));
									String v2=(String)al2.get(0);
									for(int j=1;j<al2.size();j++){
										String tmp=(String)(al2.get(j));//拼接字符串，以逗号分隔各个表达式
										if((tmp.equals("")||tmp.equals(" ")||tmp.equals("\t")||tmp.equals("\n"))==false)
											v2=v2+",\n"+tmp;//2011.2.9添加回车符
									}
									return v2.replaceAll(":", "");//将赋值语句中的冒号删除
								}
								
								
							}
						}
					}
			}
			return "";
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return "";
		}
	}
	
	private static String replaceString(String s,String s2){
		FileReader filerder;
		BufferedReader bufferedrder;
		int tabPos=0;
		String tmpKey;
		int v;
		try{
		filerder=new FileReader(s+".txt");//此文件中含有（enum名，值）对，即根据ENUM名，返回值
		bufferedrder=new BufferedReader(filerder);
		String Line=bufferedrder.readLine();
		while(Line!=null){
			tabPos=Line.indexOf('\t');
			if(tabPos>=0){
				tmpKey=Line.substring(0, tabPos).trim();
				if(s2.equals(tmpKey)){
					return Line.substring(tabPos+1,Line.length());
				}
			}
			Line=bufferedrder.readLine();
		}
		filerder.close();
		bufferedrder.close();
		return "";
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
		
	}
	//将xmi下的nta及其子节点复制到新的document中
	private static void replaceNode(Node parent,Node copied,Document d){
		Node copy;
		Element e;
		int iter=0;
		if(copied.getNodeType()==3){
			copy=d.createTextNode(copied.getNodeValue());
			
			parent.appendChild(copy);
		}
		else if(copied.getNodeType()==1){
			e=d.createElement(copied.getNodeName());
			//e=(Element)copy;
			for(iter=0;iter<copied.getAttributes().getLength();iter++){
				e.setAttribute(copied.getAttributes().item(iter).getNodeName(), copied.getAttributes().item(iter).getNodeValue());
				
				
			}
			parent.appendChild(e);
			for(iter=0;iter<copied.getChildNodes().getLength();iter++){
				replaceNode(e,copied.getChildNodes().item(iter),d);//递归调用
			}
		}
		else
			return;
	}
	private static void globalVar(){//将全局变量存入到ht中
		String tmpKey=null;
		String v="";
		int tabPos=0;
		try{
			FileReader fr =new FileReader("global.txt");
			BufferedReader br = new BufferedReader(fr);
			String Line=br.readLine();
			ht=new Hashtable();
			while(Line!=null){
				tabPos=Line.indexOf('\t');
				if(tabPos>=0){
					tmpKey=Line.substring(0, tabPos).trim();
					System.out.println(tmpKey);
					v=Line.substring(tabPos,Line.length()).trim();
					ht.put(tmpKey, v);
				}
				Line=br.readLine();
			}
			fr.close();
			br.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Shell shell = new Shell();
		String fileName=null;
		MainMachine m;
		Node sasms;
		Node root2;
		//EMFModel out=null;
		//---------------------预处理开始----------------------------
		try{
			String path=iFile.getLocation().toFile().getAbsolutePath();
			fileName=iFile.getName();
			int io=fileName.indexOf('.');
			fileName=fileName.substring(0, io);
			domFactory=DocumentBuilderFactory.newInstance();
			domFactory.setValidating(false);
			domFactory.setNamespaceAware(true);
			domBuilder=domFactory.newDocumentBuilder();
			 document=domBuilder.parse(new File(path));
			 //获得xml的根节点
			Node root=document.getFirstChild();
			//addText_a方法作用：将有text类型节点的节点添加text_a属性，并将值赋为text类型节点值
			addText_a(root);
			//////////////////////////////处理submachine///////////////////////////////
			root=findNode(root,"tasm");
			root2=root;
			sasms=root;
			System.out.println(root.getNodeName());
			root=findNode(root,"masms");//find node"masms" and assign it to node "root"
			if(root.getNodeName().equals("masms")){
				getMachine(root);//amm are filled with masms
			}
			sasms=findNode(sasms,"sasms");//find node "sasms" 
			if(sasms.getNodeName().equals("sasms")){
				getMachine(sasms);//asm are filled with sasms
			}
			for(Machine mm:amm){
				mm.setSub(asm);
				mm.changeRules();
				mm.displayRl();
			}
			for(SubMachine subm:asm){
				subm.displayRl();
			}
			changeMachine(root);
			deleteSub(sasms);
			//////////////////////////////处理submachine///////////////////////////////
			TransformerFactory transf=TransformerFactory.newInstance();
			Transformer trans=transf.newTransformer();
			Source in=new DOMSource(document);
			Result out=new StreamResult(new FileOutputStream("result.xml"));
			trans.transform(in, out);
			NodeList Lst=root.getChildNodes();
			in=null;
			out=null;
			trans=null;
			FileReader fr =new FileReader("result.xml");
			
			BufferedReader br = new BufferedReader(fr);
			String Line=br.readLine();
			//修改xml文件头部以满足atl格式
			Line="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns=\"TASM\">\n<tasm version=\"1.0\">";
			//源文件被覆盖
			String path2=path.substring(0,path.lastIndexOf('\\')+1);
			FileWriter fw=new FileWriter(path2+"preProcessing.xml",false);
			for (int i=0;Line !=null;i++){
				   fw.write(Line+'\n');
				   Line= br.readLine();
			  }
			//添加下面Line所指字符串以满足atl格式
			Line="<texts>\n<texts>oui</texts>\n<san>ok</san>\n</texts>\n</xmi:XMI>";
			fw.write(Line+'\n');
			fr.close();
			br.close();
			fw.close();
			File f;
			
			//---------------------预处理结束----------------------------
			//---------------------转换开始------------------------------
			java.net.URL tasmurl = getClass().getResource("/res/TASM.ecore");
			java.net.URL uppaalurl=getClass().getResource("/res/UPPAAL.ecore");
			java.net.URL mtuurl=getClass().getResource("/res/myTASM2UPPAAL_.asm");
			java.net.URL fturl=org.eclipse.core.runtime.FileLocator.toFileURL(tasmurl);
			java.net.URL fuurl=org.eclipse.core.runtime.FileLocator.toFileURL(uppaalurl);
			java.net.URL fmurl=org.eclipse.core.runtime.FileLocator.toFileURL(mtuurl);
			String ts=fturl.toString();
			String us=fuurl.toString();
			String ms=fmurl.toString();
			register(resource(ts, true));
			register(resource(us, true));
			//register(resource("file:\\"+path2+"TASM.ecore", true));
			//register(resource("file:\\"+path2+"UPPAAL.ecore", true));
			
			
			Resource model = resource("file:\\"+path2+"preProcessing.xml", true);
			
				
			
			OneInOneOutTransformation EMFtrans = new Transformations.Builder()
			
			.asm(ms)//.asm("file:\\"+path2+"myTASM2UPPAAL.asm")
			.in(get("TASM"), "IN", "TASM")
			.out(get("UPPAAL"), "OUT", "UPPAAL")
			.buildOneInOneOut();
			if(EMFtrans==null){
				MessageDialog.openInformation(
						shell,
						"Plug_in_aaxl4atl Plug-in",
						"null");
			}
			
			EMFModel EMFout = transform(inject(model, get("TASM")), EMFtrans);
		
			
			EMFout.getResource().setURI(URI.createURI("out.xml"));
			
			try {
				EMFout.getResource().save(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//---------------------转换结束------------------------------
			//---------------------后处理开始----------------------------
			f=new File(path2+"preProcessing.xml");
			f.delete();
				int i=0;
				Node n;
				
				globalVar();
				domFactory=DocumentBuilderFactory.newInstance();
				domFactory.setValidating(false);
				domFactory.setNamespaceAware(true);
				domBuilder=domFactory.newDocumentBuilder();
				document=domBuilder.parse(new File("out.xml"));
				document2=domBuilder.newDocument();
				root=document.getFirstChild();
				n=root;
				//找到多余label节点的位置
				for(i=0;i<root.getChildNodes().getLength();i++){
					n=root.getChildNodes().item(i);
					//added condition on 2011.3.17
					if(n.getNodeType()==1&&(n.getNodeName()=="Label"||n.getNodeName()=="Location"||n.getNodeName()=="Transition"||n.getNodeName()=="Loca"))
						break;
				}
				//删除多余的label节点
				for(int j=i;j<root.getChildNodes().getLength();j++){
					n=root.getChildNodes().item(j);
					root.removeChild(n);
				}
				//找到nta节点并赋给document2的根节点nRoot
				for(i=0;i<root.getChildNodes().getLength();i++){
					n=root.getChildNodes().item(i);
					if(n.getNodeType()==1&&n.getNodeName()=="nta")
						break;
				}
				Node nRoot=document2.createElement("nta");
				document2.appendChild(nRoot);
				for(i=0;i<n.getChildNodes().getLength();i++){
					Node nn=n.getChildNodes().item(i);
					if(nn.getNodeType()==1){
						System.out.println(nn.getNodeName());
					}
					replaceNode(nRoot,nn,document2);//删除xmi到并到doc2中
				}
				removeText_a(nRoot,document2);//将text_a属性移到外面并对label进行修改
				
				transf=TransformerFactory.newInstance();
				trans=transf.newTransformer();
				in=new DOMSource(document2);
				//覆盖源文件
				out=new StreamResult(new FileOutputStream(path2+fileName+"_UPPAAL.xml"));
				String fileoutput=path2+fileName+"_UPPAAL.xml";
				trans.transform(in, out);
				in=null;
				out=null;
				trans=null;
				//启动uppaal
				//searchFile("uppaal.jar","c:\\uppaal");
				String uppaalStr="java -jar ";
				java.net.URL uppaal = getClass().getResource("/res/uppaal/uppaal.jar");
				java.net.URL all= getClass().getResource("/res/uppaal");
				java.net.URL allurl=org.eclipse.core.runtime.FileLocator.toFileURL(all);
				uppaalurl=org.eclipse.core.runtime.FileLocator.toFileURL(uppaal);
				uppaalStr+=uppaalurl.toString();
				uppaalStr=uppaalStr.replace("file:/", "");
				uppaalStr=uppaalStr.replace("/", "\\");
				java.net.URL engine=getClass().getResource("/res/uppaal/bin-Win32");
				java.net.URL engineurl=org.eclipse.core.runtime.FileLocator.toFileURL(engine);
				String engineString=" --enginePath "+engineurl.toString()+" ";
				engineString=engineString.replace("file:/", "");
				engineString=engineString.replace("/", "\\");
				uppaalStr+=engineString;
				uppaalStr+="\""+fileoutput+"\"";
				
				
				MessageDialog.openInformation(
						shell,
						"Plug_in_tasm2uppaal Plug-in",
						"转换完成.\n生成文件："+fileName+"_UPPAAL.xml");
				refresh();
				//uppaalStr = "java -jar C:\\zt\\uppaal-4.0.6\\uppaal-4.0.6\\uppaal.jar "+path2+"final.xml";
				Runtime rt = Runtime.getRuntime();
				
				Process process = rt.exec(uppaalStr);
				f=new File("out.xml");
				f.delete();
				//f=new File(path2+fileName+".xml");
				//f.delete();
				for(MainMachine mm:amm){
					f=new File(mm.getName()+".txt");
					f.delete();
				}
				
				f=new File("global.txt");
				f.delete();
				f=new File("internal.txt");
				f.delete();
				for(String s:tempName){
					f=new File(s+".txt");
					f.delete();
				}
				f=new File("Boolean.txt");
				f.delete();
				f=new File("result.xml");
				f.delete();
				

		}catch(Exception e){
			e.printStackTrace();
		}
		//---------------------后处理结束----------------------------
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		TreeSelection treeSelection = (TreeSelection) selection;
		iFile = (IFile) treeSelection.getFirstElement();
	}
	public String searchFile(String file,String strPath){//disc格式为“x:\\”
		File f=new File(strPath);
		
		if(f.isDirectory())
		{
			//if(f.getName().equals("zt"))
				//System.out.println(f.getName());
			File[] fList=f.listFiles();
			if(fList!=null){
				for(int j=0;j<fList.length;j++)
				{
					searchFile(file,fList[j].getPath()); 
					
				}
			}
			else{
				//System.out.println("null");
			}
		}
		else if(f.isFile()){
			if(f.getName().equals(file)){
				System.out.println(f.getName());
				upStr=f.getAbsolutePath();
				return f.getAbsolutePath();
			}
		}
		return null;
	}
//////////////////////////////////////消除submachine//////////////////////////////////////
	private static void deleteSub(Node n){
		if(n.getChildNodes()!=null){
			for(int i=0;i<n.getChildNodes().getLength();i++){
				Node temp=n.getChildNodes().item(i);
				if(temp.getNodeType()==Node.ELEMENT_NODE)
					n.removeChild(temp);
			}
		}
	}
	private static void changeMachine(Node n){
		Node temp,temp1;
		MainMachine mm;
		SubMachine subm;
		for(int i=0;i<n.getChildNodes().getLength();i++){
			temp=n.getChildNodes().item(i);
			if(temp.getNodeName().equals("masm")){
				for(MainMachine mam:amm){
					if(mam.getName().equals(temp.getAttributes().getNamedItem("name").getNodeValue())){
						temp1=findNode(temp,"rls");
						if(temp1.getNodeName().equals("rls")){
							changeRl(temp,temp1,mam);
						}
						
					}
				}
			}
			
		}
	}
	private static void changeRl(Node root,Node n,MainMachine m){
		Node temp;
		Node temp2;
		MainMachine mm;
		SubMachine subm;
		Rule rtemp;
		String tname=null;
		String tmin=null;
		String tmax=null;
		String trttype=null;
		String telse=null;
		String g=null;
		String e=null;
		Node ntn=null;
		int count=0;
		ArrayList<Node> an=new ArrayList();
		Node nt=null;
		if(n.getChildNodes()==null)
			return ;
		for(int i=0;i<root.getChildNodes().getLength();i++){
			Node tempn=root.getChildNodes().item(i);
			if(tempn.getNodeType()==Node.TEXT_NODE&&root.getChildNodes().item(i+1).getNodeName().equals("rls")){
				ntn=tempn;
				break;
			}
		}
		if(ntn!=null)
			root.removeChild(ntn);
		root.removeChild(n);
		Element ele=document.createElement("rls");
		/*System.out.println("AAAAasdfasdfsdf"+n.getChildNodes().getLength());
		for(int i=0;i<n.getChildNodes().getLength();i++){
			temp=n.getChildNodes().item(i);
			if(temp.getNodeType()==Node.TEXT_NODE)
				n.removeChild(n.getChildNodes().item(i));
		}
		for(int i=0;i<n.getChildNodes().getLength();i++){
			temp=n.getChildNodes().item(i);
			if(temp.getNodeName().equals("rl"))
				n.removeChild(n.getChildNodes().item(i));
		}
		System.out.println("asdfasdfsdf"+n.getChildNodes().getLength());
		for(int i=0;i<n.getChildNodes().getLength();i++){
			temp=n.getChildNodes().item(i);
		    if(temp.getNodeName().equals("rl")){
				n.removeChild(temp);
			}
		}*/
		an=makeRl(m);
		Text tnt=document.createTextNode("\n");
		ele.appendChild(tnt);
		for(Node nn:an){
			ele.appendChild(nn);
			Text tn=document.createTextNode("\n");
			ele.appendChild((Node)tn);
		}
		Text tn=document.createTextNode("\n");
		
		root.appendChild((Node)ele);
		root.appendChild(tn);
		/*for(int i=0;i<count;i++){
			temp=n.getChildNodes().item(i);
			if(temp.getNodeType()==Node.TEXT_NODE)
				n.removeChild(temp);
		}*/
	}
	private static ArrayList<Node> makeRl(MainMachine mm){
		Element e1=null;
		ArrayList<Node> an=new ArrayList();
		for(Rule r:mm.getRl()){
			e1=document.createElement("rl");
			e1.setAttribute("relse", r.getElse());
			e1.setAttribute("rlname", r.getName());
			Element e2=document.createElement("rtime");
			e2.setAttribute("rtmax", ((Integer)r.getMax()).toString());
			e2.setAttribute("rtmin", ((Integer)r.getMin()).toString());
			e2.setAttribute("rttype", ((Integer)r.getRt()).toString());
			Text t1=document.createTextNode("\n");
			e1.appendChild((Node)t1);
			e1.appendChild((Node)e2);
			Text t2=document.createTextNode("\n");
			Element e3=document.createElement("rrscs");
			e1.appendChild((Node)t2);
			e1.appendChild((Node)e3);
			
			Text t3=document.createTextNode("\n");
			Element e4=document.createElement("guard");
			if(r.getGuard()!=null){
				e4.setAttribute("text_a", r.getGuard());
			}
			e1.appendChild((Node)t3);
			e1.appendChild((Node)e4);
			
			Text t4=document.createTextNode("\n");
			Element e5=document.createElement("effect");
			if(r.getEffect()!=null){
				e5.setAttribute("text_a", r.getEffect());
			}
			e1.appendChild((Node)t4);
			e1.appendChild((Node)e5);
			
			Text t5=document.createTextNode("\n");
			e1.appendChild((Node)t5);
			an.add((Node)e1);
			
		}
		return an;
	}
	private static Node findNode(Node n,String s){
		if(n.getChildNodes()==null){
			return n;
		}
		for(int i=0;i<n.getChildNodes().getLength();i++){
			if(n.getChildNodes().item(i).getNodeName().equals(s)){
				n=n.getChildNodes().item(i);
				break;
			}
		}
		return n;
	}
	private static void getMachine(Node n){
		Node temp;
		MainMachine mm;
		SubMachine subm;
		for(int i=0;i<n.getChildNodes().getLength();i++){
			temp=n.getChildNodes().item(i);
			if(temp.getNodeName().equals("masm")){
				mm=new MainMachine(temp.getAttributes().getNamedItem("name").getNodeValue());
				temp=findNode(temp,"rls");
				if(temp.getNodeName().equals("rls")){
					getRule(temp,mm);
				}
				amm.add(mm);
			}
			else if(temp.getNodeName().equals("sasm")){
				subm=new SubMachine(temp.getAttributes().getNamedItem("name").getNodeValue());
				temp=findNode(temp,"rls");
				if(temp.getNodeName().equals("rls")){
					getRule(temp,subm);
				}
				asm.add(subm);
			}
		}
	}
	private static void getRule(Node n,Machine m){
		Node temp;
		Node temp2;
		MainMachine mm;
		SubMachine subm;
		Rule rtemp;
		String tname=null;
		String tmin=null;
		String tmax=null;
		String trttype=null;
		String telse=null;
		String g=null;
		String e=null;
		if(n.getChildNodes()==null)
			return ;
		for(int i=0;i<n.getChildNodes().getLength();i++){
			temp=n.getChildNodes().item(i);
			if(temp.getNodeName().equals("rl")){
				tname=temp.getAttributes().getNamedItem("rlname").getNodeValue();
				telse=temp.getAttributes().getNamedItem("relse").getNodeValue();
				if(temp.getChildNodes()!=null){
					for(int j=0;j<temp.getChildNodes().getLength();j++){
						temp2=temp.getChildNodes().item(j);
						if(temp2.getNodeName().equals("rtime")){
							tmax=temp2.getAttributes().getNamedItem("rtmax").getNodeValue();
							tmin=temp2.getAttributes().getNamedItem("rtmin").getNodeValue();
							trttype=temp2.getAttributes().getNamedItem("rttype").getNodeValue();
						}
						else if(temp2.getNodeName().equals("guard")){
							if(temp2.getAttributes()!=null&&temp2.getAttributes().getNamedItem("text_a")!=null){
								g=temp2.getAttributes().getNamedItem("text_a").getNodeValue();
							}
						}
						else if(temp2.getNodeName().equals("effect")){
							if(temp2.getAttributes()!=null&&temp2.getAttributes().getNamedItem("text_a")!=null){
								e=temp2.getAttributes().getNamedItem("text_a").getNodeValue();
							}
						}
					}
				}
				rtemp=new Rule(tname,g,e,trttype,tmin,tmax,telse);//Rule(String n,String g,String e,String rt,String min,String max)
				g=null;
				e=null;
				m.addRule(rtemp);
			}
		}
	}

	

}

