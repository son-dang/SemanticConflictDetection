/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.caucho.quercus.Location;
import com.caucho.quercus.QuercusContext;
import com.caucho.quercus.env.ConstStringValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.QuercusClass;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.BinaryEqExpr;
import com.caucho.quercus.expr.CallExpr;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.expr.FunIncludeOnceExpr;
import com.caucho.quercus.expr.LiteralExpr;
import com.caucho.quercus.expr.LiteralStringExpr;
import com.caucho.quercus.function.AbstractFunction;
import com.caucho.quercus.page.InterpretedPage;
import com.caucho.quercus.page.QuercusPage;
import com.caucho.quercus.program.InterpretedClassDef;
import com.caucho.quercus.statement.BlockStatement;
import com.caucho.quercus.statement.ClassDefStatement;
import com.caucho.quercus.statement.ExprStatement;
import com.caucho.quercus.statement.Statement;
import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.debug.OutputViewer;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.Functions.__ExpectOutputRegex__;
import edu.iastate.hungnv.shadow.Functions.__ExpectOutputString__;
import edu.iastate.hungnv.util.FileIO;
import edu.iastate.hungnv.util.Logging;
import edu.iastate.hungnv.value.MultiValue;

/**
 *
 * @author 10123_000
 * Semantic Conflict Detection Tool
 */
public class Vaseco {
	
	/**
	 * Run the tool
	 * @param env
	 * @param resultPath path to the alignment result
	 * @param reportPath path to semantic conflict report file
	 */
	
    public static void run(Env env, String resultPath, String reportPath) {
        Path pwd = Vfs.lookup();
        QuercusContext quercus = env.getQuercus();
        
        // List of branches specified by user
        List<String> branchesName = new ArrayList<String>();
        branchesName.addAll(Env_.BranchDirectoryMap.keySet());
        
        // Choice variables for each branch
        List<String> branchesVar = new ArrayList<String>();
        for (int i=0; i<branchesName.size(); i++)
            branchesVar.add("$B" + (i + 1));
        
        // Map from branch name to its location
        List<String> directories = new ArrayList<String>();
        for(String branchName : branchesName){
            directories.add(Env_.BranchDirectoryMap.get(branchName));
        }
        
        // A set of all php files in all branch
        // Currently the tool only align source code in files with .php extension
        Set<String> filesName = new LinkedHashSet<String>();
        
        for(String dir : directories){
            File dirFile = new File(pwd.lookup(dir).getFullPath());
            Collection<File> files = FileUtils.listFiles(dirFile, new String[]{"php"}, true);
            for(File file : files){
                filesName.add(file.getAbsolutePath().substring(dirFile.getAbsolutePath().length()));
            }
        }
        
        // Delete old alignment result
        String resultDir = pwd.lookup(resultPath).getFullPath();
        FileIO.deleteFileRecursive(resultDir);
        
        // For each file in the file set
        // create blank file in branches that do not have the file or the file was deleted
        for(String dir : directories){
            for (String fileName : filesName){
                File file = new File(pwd.lookup(dir + "//" + fileName).getFullPath());
                if (!file.exists())
                    try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(Vaseco.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
        
        // Align statements in each file and execute tool
        for(String fileName : filesName){
        	
        	// Get paths of file in each branch
            String[] filePaths = new String[directories.size()];
            for(int i=0; i<filePaths.length; i++){
                filePaths[i] = pwd.lookup(directories.get(i) + "//" + fileName).getFullPath();
            }
            
            Path[] paths = new Path[filePaths.length];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = pwd.lookup(filePaths[i]);
            }
            
            // Parse statements in each file into 2-dimensional array Seqs
            QuercusPage[] quercusPages = new QuercusPage[filePaths.length];
            ArrayList<ArrayList<Statement>> Seqs = new ArrayList<ArrayList<Statement>>();            
            try {
                for (int i = 0; i < quercusPages.length; i++) {
                	try{
                		quercusPages[i] = quercus.parse(paths[i]);
                	}
                	catch (Exception ex){
                		Logger.getLogger(Vaseco.class.getName()).log(Level.SEVERE, null, ex);
                	}
                    Seqs.add(new ArrayList<Statement>());
                    if (quercusPages[i] instanceof InterpretedPage) {
                        Statement stm = ((InterpretedPage) quercusPages[i])._program.getStatement();
                        if (stm instanceof BlockStatement){       
                            Seqs.get(i).add(stm);
                        }
                        else{
                            ArrayList<Statement> temp = new ArrayList<Statement>();
                            temp.add(stm);
                            Seqs.get(i).add(new BlockStatement(Location.UNKNOWN, temp));
                        }
                    } else {
                        return;
                    }
                }

                // Align statements
                try{
                	Seqs = StatementAlignment.alignImpl(Seqs);
                }
                catch (Exception ex){
                	Logger.getLogger(Vaseco.class.getName()).log(Level.SEVERE, null, ex);
                	continue;
                }

                // Covert alignment result to string and write to specified directory
                StringBuilder modelString = new StringBuilder();
                modelString.append("<?php\r\n");

                for(int i=0; i<Seqs.size(); i++){
                    modelString.append(branchesVar.get(i)).append(" = ");
                    if (i==0)
                        modelString.append("true;\r\n");
                    else
                        modelString.append("__CHOICE__(\"").append(branchesName.get(i)).append("\");\r\n");
                }

                modelString.append(ModelBuilder.BuildModel(Seqs, branchesVar));

                modelString.append("?>");
                
                File resultFile = new File(pwd.lookup(resultPath + "//" + fileName).getFullPath());
                if (!resultFile.exists()){
                    resultFile.getParentFile().mkdirs();
                    resultFile.createNewFile();
                }
                FileIO.writeStringToFile(modelString.toString(), resultFile.getAbsolutePath());

            } catch (FileNotFoundException ex) {
            	Logger.getLogger(Vaseco.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            	Logger.getLogger(Vaseco.class.getName()).log(Level.SEVERE, null, ex);
            }
        } // end of for each file loop      
     
        // Copy test file to result folder
        /*String testFilePathSrc = pwd.lookup(Env_.testClass).getFullPath();
        File testFile = new File(testFilePathSrc);
        String testFilePathDest = pwd.lookup(resultPath + "//" + testFile.getName()).getFullPath();
        FileIO.copyFileOrFolder(testFilePathSrc, testFilePathDest);*/
        
        // Execute test
        try {
        	ArrayList<String> allFilesName = new ArrayList<String>();
        	allFilesName.addAll(filesName);
        	executeTest(env, resultPath, allFilesName); 
        	
        	 // Log error
            String reportFilePath = pwd.lookup(reportPath).getFullPath();
            File reportFile = new File(reportFilePath);
            if (!reportFile.exists()){
                reportFile.getParentFile().mkdirs();
                reportFile.createNewFile();
            }
            FileIO.writeStringToFile(Env_.errorLog.toString(), reportFilePath);
        	
            // Reset variables
            Env_.BranchDirectoryMap = new LinkedHashMap<String, String>();
            //Env_.testClass = null;
            Env_.errorLog = new StringBuilder();
        } catch (Exception ex) {
        	Env_.errorLog = new StringBuilder("Error occured: " + ex.toString());
            Logger.getLogger(Vaseco.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
        	if (Env_.errorLog.length() == 0){        		
        		String success = "<html> </br>Test run successfully.</br>";
        		success+= "Result directory: " + resultDir + "</br>";
        		success+= "Report file: " + pwd.lookup(reportPath).getFullPath() + "</br></html>";
        		try {
					env.getOut().write(success.getBytes(), 0, success.length());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	else{
        		try {
					env.getOut().write(Env_.errorLog.toString().getBytes(), 0, Env_.errorLog.length());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    }
    
    private static void executeTest(Env env, String resultPath, ArrayList<String> filesName) throws IOException{
    	// if using php unit framework - create dummy class
    	if (Env_.testClass.equalsIgnoreCase("PHPUnit_Framework_TestCase")){
    		ClassDefStatement classDefStm = new ClassDefStatement(Location.UNKNOWN, 
    				new InterpretedClassDef("PHPUnit_Framework_TestCase",
    				null, null));
    		classDefStm.execute(env);
    	}
    	
    	// exeception interfaces that quercus still not implemented
    	ClassDefStatement classDefStm = new ClassDefStatement(Location.UNKNOWN, 
				new InterpretedClassDef("SplObserver",
				null, null));
		classDefStm.execute(env);
		classDefStm = new ClassDefStatement(Location.UNKNOWN, 
				new InterpretedClassDef("SplSubject",
				null, null));
		classDefStm.execute(env);
    	
    	// include all php file - solve dependency
    	includeAll(env, resultPath, filesName);
    	
    	// create test instances
    	QuercusClass []allClasses = env.getAllClasses();
    	for(QuercusClass qClass : allClasses){
    		if (qClass == null)
    			continue;
    		
    	//	if (!qClass.getName().endsWith("MediatorModified\\Tests\\MediatorTest"))
    	//		continue;
    		
    		Value inst = null;
    		if (qClass.getName().equalsIgnoreCase(Env_.testClass))
    			inst = qClass.createObject(env);
    		if (qClass.getParent() != null){
	    		if (qClass.getParent().getName().equalsIgnoreCase(Env_.testClass))
	    		inst = qClass.createObject(env);
    		}
    		
    		if (inst != null){
    			Iterable<AbstractFunction> funcs = qClass.getClassMethods();
    			CustomListString methodNameList = new CustomListString();
    			for (AbstractFunction func : funcs){
    				methodNameList.add(func.getName());
    			}
    			for(String methodName : methodNameList){
    				if (methodName.equalsIgnoreCase("setUp"))
    					continue;
    				if (methodName.equalsIgnoreCase("tearDown"))
    					continue;
    				
    				String comment = inst.getQuercusClass().getMethodMap().get(new ConstStringValue(methodName)).getComment();
    				String providerFuncName = null;
    				
    				// find data provider if using PHP_Unit_Framework_Testcase
    				if (Env_.testClass.equals("PHPUnit_Framework_TestCase")){
	    				if (comment != null){
	    					int start = comment.indexOf("@dataProvider ");
	    					if (start > -1){
	    						start += "@dataProvider ".length();
	    						providerFuncName = comment.substring(start);
	    						providerFuncName = providerFuncName.substring(0, providerFuncName.indexOf("\n"));
	    					}
	    				}
    				}
    				
    				// if using provider
    				if (providerFuncName != null){
    					
    					if (methodName.equals(providerFuncName))
    						continue;
    					
    					Value providedData = inst.callMethod(env, new ConstStringValue(providerFuncName));
    					if (providedData.isArray()){
    						Value[] providedArray = providedData.getValueArray(env);
    						for (int i=0; i<providedArray.length; i++){
    							// call setup
    	    					if (methodNameList.contains("setUp")){
    	        					inst.callMethod(env, new ConstStringValue(methodNameList.get(methodNameList.indexOf("setup"))));
    	        				}
    	    					// call test method
    	    					if (!providedArray[i].isArray())
    	    						inst.callMethod(env, new ConstStringValue(methodName), providedArray[i]);
    	    					else{
    	    						Value[] providedArrayItem = providedArray[i].getValueArray(env);
    	    						int itemSize = providedArrayItem.length;
    	    						if (itemSize == 1) {inst.callMethod(env, new ConstStringValue(methodName), providedArrayItem[0]);}
    	    						else if (itemSize == 2) {inst.callMethod(env, new ConstStringValue(methodName), providedArrayItem[0], providedArrayItem[1]);}
    	    						else if (itemSize == 3) {inst.callMethod(env, new ConstStringValue(methodName), providedArrayItem[0], providedArrayItem[1], providedArrayItem[2]);}
    	    						else if (itemSize == 4) {inst.callMethod(env, new ConstStringValue(methodName), providedArrayItem[0], providedArrayItem[1], providedArrayItem[2], providedArrayItem[3]);}
    	    						else if (itemSize == 5) {inst.callMethod(env, new ConstStringValue(methodName), providedArrayItem[0], providedArrayItem[1], providedArrayItem[2], providedArrayItem[3], providedArrayItem[4]);}
    	    					}
    	    					// call tear down
    	    					if (methodNameList.contains("tearDown")){
    	        					inst.callMethod(env, new ConstStringValue("tearDown"));
    	        				}
    	    					
    	    					// for phpunit expectOutput func
    	    					ExpectOutput(env);
    						}
    					}
    				}
    				// if not using provider
    				else{
    					// call setup
    					if (methodNameList.contains("setUp")){
        					inst.callMethod(env, new ConstStringValue(methodNameList.get(methodNameList.indexOf("setup"))));
        				}
    					// call test method
    					inst.callMethod(env, new ConstStringValue(methodName));
    					// call tear down
    					if (methodNameList.contains("tearDown")){
        					inst.callMethod(env, new ConstStringValue("tearDown"));
        				}
    					// for phpunit expectOutput func
    					ExpectOutput(env);
    				}
    			}
    		}
    	}
    }
    
    private static ArrayList<String> includeAll(Env env, String resultPath, ArrayList<String> filesName) throws IOException{
    	Path pwd = Vfs.lookup();
        QuercusContext quercus = env.getQuercus();
        ArrayList<String> unincludedFiles = new ArrayList<String>();
        unincludedFiles.addAll(filesName);
        HashSet<String> included = new HashSet<String>();
        while (unincludedFiles.size() > 0){
        	for(int i=0; i<unincludedFiles.size(); i++){
	    	//for(String fileName : unincludedFiles){
        		String fileName = unincludedFiles.get(i);

	    		// parse file
	    		String fileDir = resultPath + "\\" + fileName;
	    		Path filePath = pwd.lookup(fileDir);
	    		QuercusPage page = quercus.parse(filePath);
	    		BlockStatement blockStm = ((InterpretedPage)page)._program.getStatement().createBlock();
	    		
	    		// find class dependencies
	    		ArrayList<String> dependencies = findDependency(blockStm.getStatements());
				boolean unresolvedDependency = false;
				for(String dependency : dependencies){
					QuercusClass qClass = env.findClass(dependency);
					if (qClass == null){
						unresolvedDependency = true;
						break;
					}
				}
				
				if (unresolvedDependency){
					continue;
				}
				else{
					String includeName = resultPath + fileName;
					includeName = includeName.substring(includeName.lastIndexOf("\\") + 1);
					Statement includeStm = new ExprStatement(Location.UNKNOWN, 
	    					new FunIncludeOnceExpr(filePath, new LiteralStringExpr(includeName)));
					try{
						if (included.contains(fileName))
							Logging.LOGGER.fine("Included more than once file: " + includeName);
						Logging.LOGGER.fine("Including file " + fileName);
						Logging.LOGGER.fine(included.size() + "/" + filesName.size());
						includeStm.execute(env);
					}
					catch(Exception ex){
					}
	    			unincludedFiles.remove(fileName);
	    			i--;
	    			
	    			included.add(fileName);
				}		
	    	}
        }
        
    	return null;
    }
    
    private static ArrayList<String> findDependency(Statement[] stms){
    	ArrayList<String> retValue = new ArrayList<String>();
    	for(Statement stm : stms){
    		if (stm instanceof BlockStatement){
    			retValue.addAll(findDependency(((BlockStatement)stm).getStatements()));
    		} else {
    			String stringStm = stm.toString();
    			String extendsPattern = "$__Extends__=($__Extends__ . 'extends ";
    			if (stringStm.startsWith(extendsPattern)){
    				int start = extendsPattern.length();
    				int end = stringStm.lastIndexOf("'");
    				String dependency = stringStm.substring(start, end);
    				if (!dependency.equals(" "))
    					retValue.add(dependency);
    			}
    			
    			String implPattern = "$__Implements__='implements ";
    			if (stringStm.startsWith(implPattern)){
    				int start = implPattern.length();
    				int end = stringStm.lastIndexOf("'");
    				String dependency = stringStm.substring(start, end);
    				if (!dependency.equals(" "))
    					retValue.add(dependency);
    			}
    		}
    	}
    	return retValue;
    }

    private static void ExpectOutput(Env env){
    	// expect output string
		if (Env_.expectOutput != null){
			Expr eqExpr = new BinaryEqExpr(
					new LiteralStringExpr(Env_.expectOutput), 
					new LiteralExpr(OutputViewer.inst.getOutputValue()));
			Value assertedValue = eqExpr.eval(env);
			Constraint falseConstraint = MultiValue.whenFalse(assertedValue);
			if (falseConstraint.isSatisfiable()){
				
				Env_.errorLog.append(__ExpectOutputString__.class.getSimpleName()
						+ "(" + Env_.expectOutput 
						+ ") at file " + Env_.expectOutputLoc.getFileName()
						+ " line " + Env_.expectOutputLoc.getLineNumber()
						+ " is false when " + falseConstraint.toString() + "\r\n");
			}			
			Env_.expectOutput = null;
			Env_.expectOutputLoc = null;
		}
		
		// expect output regex
		if (Env_.expectOutputRegex != null){
			ArrayList<Expr> args = new ArrayList<Expr>();
			args.add(new LiteralStringExpr(Env_.expectOutputRegex));
			args.add(new LiteralExpr(OutputViewer.inst.getOutputValue()));
			Expr pregExpr = new CallExpr("preg_match", args);
			Expr eqExpr = new BinaryEqExpr(new LiteralExpr(new LongValue(1)), pregExpr);
			
			Value assertedValue = eqExpr.eval(env);
			Constraint falseConstraint = MultiValue.whenFalse(assertedValue);
			if (falseConstraint.isSatisfiable()){
				
				Env_.errorLog.append(__ExpectOutputRegex__.class.getSimpleName()
						+ "(" + Env_.expectOutputRegex 
						+ ") at file " + Env_.expectOutputRegexLoc.getFileName()
						+ " line " + Env_.expectOutputRegexLoc.getLineNumber()
						+ " is false when " + falseConstraint.toString() + "\r\n");
			}			
			Env_.expectOutputRegex = null;
			Env_.expectOutputRegexLoc = null;
		}
    }
}
