package org.sag.repair;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.sag.faultlocalization.FaultLocalizationExperiment;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.wso2.balana.Rule;

import com.opencsv.CSVWriter;

public class ExperimentMultiFault {

	public static void main(String[] args) throws Exception {
		String[] policy = { "conference3", "fedora-rule3", "itrust3",
				"kmarket-blue-policy", "obligation3", "pluto3" };
		String[] testsuite = { "Basic", "Exclusive", "Pair", "PDpair",
				"DecisionCoverage", "RuleLevel", "MCDCCoverage" };
		int policyNumber = 2;
		int testsuiteNumber = 6;

		String testSuiteSpreadSheetFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "test_suites"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + File.separator
				+ policy[policyNumber] + "_" + testsuite[testsuiteNumber]
				+ ".xls";
		String policyMutantSpreadsheetFil = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "mutants"
				+ File.separator + policy[policyNumber] + "_mutants.xls";
		String timingFileName = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "repair"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + "_multiFault_repair_timing.csv";
		String repairResultFileName = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "repair"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + "_multiFault_repairedFile.csv";
		String policyFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + policy[policyNumber]
				+ ".xml";
		List<String> faultLocalizeMethods = new ArrayList<String>();
		faultLocalizeMethods.add("jaccard");
		faultLocalizeMethods.add("tarantula");
		faultLocalizeMethods.add("ochiai");
		faultLocalizeMethods.add("ochiai2");
		faultLocalizeMethods.add("cbi");
		faultLocalizeMethods.add("hamann");
		faultLocalizeMethods.add("simpleMatching");
		faultLocalizeMethods.add("sokal");
		faultLocalizeMethods.add("naish2");
		faultLocalizeMethods.add("goodman");
		faultLocalizeMethods.add("sorensenDice");
		faultLocalizeMethods.add("anderberg");
		faultLocalizeMethods.add("euclid");
		faultLocalizeMethods.add("rogersTanimoto");
		
//		int numFaults = 1;
//		// multiple faults
//		List<String> createMutantMethods = new ArrayList<String>();
//		createMutantMethods.add("createPolicyTargetTrueMutants");// PTT
//		createMutantMethods.add("createPolicyTargetFalseMutants");// PTF
//		createMutantMethods.add("createCombiningAlgorithmMutants");//CRC
//		// //comment out because cannot localize
//		createMutantMethods.add("createRuleEffectFlippingMutants");// CRE
//		// createMutantMethods.add("createRemoveRuleMutants");//RER
//		// createMutantMethods.add("createAddNewRuleMutants");//ANR
////		createMutantMethods.add("createRuleTargetTrueMutants");// RTT
//		createMutantMethods.add("createRuleTargetFalseMutants");// RTF
//		createMutantMethods.add("createRuleConditionTrueMutants");// RCT
//		createMutantMethods.add("createRuleConditionFalseMutants");// RCF
//		createMutantMethods.add("createFirstPermitRuleMutants");//FPR
//		createMutantMethods.add("createFirstDenyRuleMutants");//FDR
//		// createMutantMethods.add("createRuleTypeReplacedMutants");//RTR
//		createMutantMethods.add("createAddNotFunctionMutants");// ANF
//		createMutantMethods.add("createRemoveNotFunctionMutants");// RNF
////		createMutantMethods.add("createRemoveParallelTargetElementMutants");// RPTE
//		// createMutantMethods.add("createRemoveParallelConditionElementMutants");//RPCE
//
//		List<PolicyMutant> mutantList = FaultLocalizationExperiment
//				.createMultiFaultMutants(policyFile, numFaults,
//						createMutantMethods);
//		// String MutantsCSVFileName = createMutantsCSVFile(mutantList);
		
		int numFaults = 2;
//		String number = "MUTANT CRE5_CCF3_2";
//		String filename = "Experiments//conference3//mutants//mutants//conference3_CRE5_CCF3_2.xml";
		String number = "MUTANT RTF4_RCF2";//cannot rpair, takes 40 minutes for each run of start() 
		String filename = "Experiments//conference3//mutants//mutants//conference3_RTF4_RCF2.xml";
		int[] bugPositions = new int[] {5, 3};
		PolicyMutant policyToRepair = new PolicyMutant(number, filename, bugPositions);
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		mutantList.add(policyToRepair);
		
		CSVWriter timingWriter = new CSVWriter(new FileWriter(timingFileName), ',');
		CSVWriter repairResultWriter = new CSVWriter(new FileWriter(repairResultFileName), ',');
		writeCSVTitleRow(timingWriter, faultLocalizeMethods);
		writeCSVTitleRow(repairResultWriter, faultLocalizeMethods);
		for (PolicyMutant mutant: mutantList) {
			List<String> durationList = new ArrayList<String>();
			List<String> repairedFileList = new ArrayList<String>();
			for (String faultLocalizeMethod: faultLocalizeMethods) {
				long start = System.currentTimeMillis();
				String repaired = start(mutant, testSuiteSpreadSheetFile, faultLocalizeMethod, numFaults);
				long end = System.currentTimeMillis();
				durationList.add(Long.toString(end - start));
				if (repaired != null) {
					repairedFileList.add(repaired);
				} else {
					repairedFileList.add("cannot repair");
				}
			}
			writeCSVResultRow(timingWriter, mutant.getNumber(), durationList);
			writeCSVResultRow(repairResultWriter, mutant.getNumber(), repairedFileList);
		}
		timingWriter.close();
		repairResultWriter.close();
	}

	private static void writeCSVTitleRow(CSVWriter writer,
			List<String> faultLocalizeMethods) {
		String[] titles = new String[faultLocalizeMethods.size() + 1];
		titles[0] = "mutant";
		int index = 1;
		for (String faultLocalizeMethod : faultLocalizeMethods) {
			titles[index] = faultLocalizeMethod;
			index++;
		}
		writer.writeNext(titles);
	}

	private static void writeCSVResultRow(CSVWriter writer, String mutantNumber,
			List<String> durationList) {
		
		String[] entry = new String[durationList.size() + 1];
		entry[0] = mutantNumber;
		int index = 1;
		for (String duration: durationList) {
			entry[index] = duration;
			index ++;
		}
		writer.writeNext(entry);
	}

	ExperimentMultiFault(PolicyMutant policyToRepair,
			String faultLocalizeMethod, String testSuiteFile) throws Exception {
	}
	
	static String start(PolicyMutant policyToRepair, String testSuiteFile, String faultLocalizeMethod, int maxSearchLayer) throws Exception {
		Queue<MutantNode> queue = new PriorityQueue<MutantNode>();
		queue.add(new MutantNode(null, policyToRepair, testSuiteFile, faultLocalizeMethod, 0, 0));
		MutantNode node = null;
		boolean foundRepair = false;
		while (!queue.isEmpty()) {
			node = queue.poll();
			System.out.println("queue size: " + queue.size());
			List<Boolean> testResults = node.getTestResult();
			if (PolicyRepairer.booleanListAnd(testResults)) {
				foundRepair = true;
				break;//found a repair
			}
			if (!node.isPromising()) {
				continue;
			}
			if (node.getLayer() + 1 > maxSearchLayer)
				continue;
			List<Integer> suspicionRank = node.getSuspicionRank();
			PolicyMutator mutator = new PolicyMutator(policyToRepair);
			int rank = 1;
			for (int bugPosition : suspicionRank) {
				List<PolicyMutant> mutantList = generateMutants(mutator, bugPosition);
				for (PolicyMutant mutant: mutantList) {
					queue.add(new MutantNode(node, mutant, testSuiteFile, faultLocalizeMethod, rank, node.getLayer() + 1));
				}
				rank ++;
			}
		}
		String res = new String(node.getMutant().getNumber());
		for(MutantNode mutantNode: queue)
			mutantNode.clear();
		queue.clear();
		if(foundRepair)
			return res;
		return null;
	}
	
	private static List<PolicyMutant> generateMutants(PolicyMutator mutator, int bugPosition) throws Exception {
		if (bugPosition == -1) {
			List<PolicyMutant> mutantList = mutator.createCombiningAlgorithmMutants();
			return mutantList;
		}
		if (bugPosition == 0) {
			List<PolicyMutant> mutantList = generateMutantsPolicyTarget(mutator);
			return mutantList;
		}
		List<Rule> ruleList = mutator.getRuleList();
		Rule myrule = ruleList.get(bugPosition - 1);
		int ruleIndex = bugPosition;
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		//CRE
		mutantList.addAll(mutator.createRuleEffectFlippingMutants(myrule, ruleIndex));
//		//ANR
//		mutantList = mutator.createAddNewRuleMutants(myrule, ruleIndex);
//		correctMutant = find1stCorrectMutant(mutantList);
//		if(correctMutant != null) {
//			return correctMutant;
//		}
		//RTT
		mutantList.addAll(mutator.createRuleTargetTrueMutants(myrule, ruleIndex));
		//RTF
		mutantList.addAll(mutator.createRuleTargetFalseMutants(myrule, ruleIndex));
		//RCT
		mutantList.addAll(mutator.createRuleConditionTrueMutants(myrule, ruleIndex));
		//RCF
		mutantList.addAll(mutator.createRuleConditionFalseMutants(myrule, ruleIndex));
		//ANF
		mutantList.addAll(mutator.createAddNotFunctionMutants(myrule, ruleIndex));
		//RNF
		mutantList.addAll(mutator.createRemoveNotFunctionMutants(myrule,  ruleIndex));
		//FCF
		mutantList.addAll(mutator.createFlipComparisonFunctionMutants(myrule, ruleIndex));
		//CCF
		mutantList.addAll(mutator.createChangeComparisonFunctionMutants(myrule, ruleIndex));
		//RPTE
		mutantList.addAll(mutator.createRemoveParallelTargetElementMutants(myrule, ruleIndex));
		return mutantList;
	}
	
	private static List<PolicyMutant> generateMutantsPolicyTarget(PolicyMutator mutator) {
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		//create mutant methods who's bugPosition == 0
		// PTT
		mutantList.addAll(mutator.createPolicyTargetTrueMutants());
		// PTF
		mutantList.addAll(mutator.createPolicyTargetTrueMutants());
		//FPR
		mutantList.addAll(mutator.createFirstPermitRuleMutants());
		//FDR
		mutantList.addAll(mutator.createFirstDenyRuleMutants());
		return mutantList;
	}

}