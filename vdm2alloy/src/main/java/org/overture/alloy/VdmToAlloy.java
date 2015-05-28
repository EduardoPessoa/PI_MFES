package org.overture.alloy;

import edu.mit.csail.sdg.alloy4.Terminal;
import org.apache.commons.cli.*;
import org.overture.alloy.ast.Part;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Settings;
import org.overture.typechecker.util.TypeCheckerUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by macbookpro on 28/05/15.
 */
public class VdmToAlloy {
    public  String nameType;
    public  String type;
    public  String path;
    public String error="";

    public VdmToAlloy(String nameType,String type,String path) {
    this.nameType=nameType;
        this.type=type;
        this.path=path;
    }

    public  int execute() throws Exception
    {
        ContextSlicing c =  new ContextSlicing();
        // create the command line parser
        String s = "";


        File input = new File(path);
        File output=null;



        Settings.dialect = Dialect.VDM_SL;
        TypeCheckerUtil.TypeCheckResult<List<AModuleModules>> result = TypeCheckerUtil.typeCheckSl(input);


        if (result.errors.isEmpty()) {
            File tmpFile = null;
            if (output == null) {
                tmpFile = File.createTempFile("vdm2alloy", ".als");
            } else {
                tmpFile = output;
            }
                p(input.toString());


            /******************** Not allowed types ************************/
            // System.out.println("/***********************************\tNot allowed types\t*********************************/");
             NotAllowed notAllowed = new NotAllowed(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
            result.result.get(0).apply(notAllowed, new ContextSlicing());
            NotAllowedTypes o = new NotAllowedTypes(notAllowed.getNotAllowed());
            if(o.hasNoAllowedType()) {
                this.error+="There are some problems on the file " + input +"\n"+o.toString();
                return 1;
            }

            /***************   Slicing  ******************/

            //  System.out.println("/***********************************\tStelicing\t*******************************************/");
            NewSlicing slicing = new NewSlicing(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
            result.result.get(0).apply(slicing, new ContextSlicing(nameType,c.inverseTranslation(type)));//t = ATypeDefinition , f = AExplicitFunctionDefinition , v = AValueDefinition
            //System.out.println(slicing.getNodeList().toString());
            //System.out.println(slicing.toString());



            /***************   Proof Obligations  ******************/

            Proofs proof = new Proofs(slicing.getModuleModules());
           // System.out.println (proof.getNode().toString());

            //IProofObligationList x = new IProofObligationList(null);
            // System.out.println (ProofObligationGenerator.generateProofObligations(slicing.getModuleModules()).get(0).getValueTree().toString());
            // System.out.println(proof.getModulePO().toString());

            //System.out.println(slicing.getNodeList().toString());
            //System.out.println("\n\n\n");
            //example.......
            //AFunctionType f = (AFunctionType) slicing.getNodeList().getLast();
            //System.out.println(f.getDefinitions().toString());


            /*********************** Translation ******************/
            //System.out.println("/***********************************\tTranslation\t*****************************************/");
            Alloy2VdmAnalysis analysis = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")),false);
            //result.result.get(0).apply(analysis, new Context());
              slicing.getModuleModules().apply(analysis,new Context());
            // System.out.println(proof.getNode().getClass().getSimpleName().toString());
            Alloy2VdmAnalysis analysisProof = new Alloy2VdmAnalysis(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")),true);
            proof.getNode().apply(analysisProof,new Context());
            analysis.components.addAll(analysisProof.getComponentsPO());
            //System.out.println(analysisProof.getComponentsPO().toString());
            //Proofs proof = new Proofs(tmpFile.getName().substring(0, tmpFile.getName().indexOf(".")));
            //result.result.get(0).apply(proof,new ContextSlicing());



            /*NotAllowedTypes notA = new NotAllowedTypes(analysis.getNotAllowedTypes(),0);
            if (notA.hasnoAllowedType()) {
                System.out.println("There are some problems on the file "+input+"\n\n"+notA.toString());
            } else {*/



            //analysis.components.add(new Pred("show", "", ""));
            //analysis.components.add(new Run("show"));

            FileWriter outFile = new FileWriter(tmpFile);
            PrintWriter out = new PrintWriter(outFile);
            for (Part string : analysis.components) {
                out.println(string);
            }

            out.close();


                System.out.println("\n------------------------------------");
                System.out.println("Running Alloy...\n");
                System.out.println("Temp file: " + tmpFile.getAbsolutePath());

                System.out.println("Running Alloy on file: "
                        + tmpFile.getName());
                int exitCode = Terminal.execute(new String[]{"-alloy",
                        tmpFile.getAbsolutePath(), "-a", "-s", "SAT4J"});
                if (exitCode != 0) {
                    return exitCode;
                }

               /* if (line.hasOption(extraAlloyTest.getOpt())) {
                    String testInputPath = line.getOptionValue(extraAlloyTest.getOpt());
                    System.out.println("Running Alloy on file: "
                            + testInputPath);
                    Terminal.main(new String[]{"-alloy", testInputPath, "-a", "-s", "SAT4J"});
                }*/


        }else
        {
            this.error+="Errors in input VDM model";
            return 1;
        }
        return 0;
    }
    public static void p(String string){
        System.out.println(string);
    }
}
