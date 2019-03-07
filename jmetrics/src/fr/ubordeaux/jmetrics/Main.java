package fr.ubordeaux.jmetrics;

import fr.ubordeaux.jmetrics.analysis.*;
import fr.ubordeaux.jmetrics.datastructure.*;
import fr.ubordeaux.jmetrics.metrics.GranularityScale;
import fr.ubordeaux.jmetrics.metrics.ClassGranularity;
import fr.ubordeaux.jmetrics.metrics.Metrics;
import fr.ubordeaux.jmetrics.presentation.GraphDotBuilder;
import fr.ubordeaux.jmetrics.presentation.GraphPresentationBuilder;
import fr.ubordeaux.jmetrics.project.*;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2){
            System.out.println("Invalid number of arguments");
            System.out.println("Usage : <jmetrics_run_command> <path_to_analyzed_project> [--dot-only]");
            System.exit(1);
        }
        String path = args[0];
        boolean dotOnly = args.length == 2 && args[1].equals("--dot-only");

        // Project's exploration
        ProjectStructure structure = ProjectStructure.getInstance();
        FileSystemExplorer explorer = new BytecodeFileSystemExplorer();
        try {
            structure.setStructure(explorer.generateStructure(path));
        } catch(InvalidProjectPathException e){
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // Analysis
        List<ClassFile> classes = ProjectStructure.getInstance().getClasses();

        // Abstractness analysis
        Map<ClassFile, AbstractnessData> aData = new HashMap<>();
        AbstractnessParser aParser = new IntrospectionAbstractnessParser();
        for (ClassFile c : classes) {
            aData.put(c, aParser.getAbstractnessData(c));
        }

        // Coupling analysis
        List<Dependency> dependencies = new ArrayList<>();
        CouplingParser cParser = new IntrospectionCouplingParser();
        for (ClassFile c: classes) {
            dependencies.addAll(cParser.getDependencies(c));
        }

        // Graph construction
        Set<GranularityScale> nodes = new HashSet<>();
        //FIXME this is inevitable for the moment as no mapping between ClassFile and ClassGranularity is available, will be removed later when fixed
        Map<GranularityScale, ClassFile> fileToNodeMapping = new HashMap<>();
        for (ClassFile c : classes) {
            GranularityScale node = new ClassGranularity(c);
            nodes.add(node);
            fileToNodeMapping.put(node, c);
        }
        DirectedGraph<GranularityScale, DependencyEdge> graph = (new GraphConstructor()).constructGraph(nodes, new HashSet<>(dependencies));

        // Metrics computation
        if(!dotOnly) {
            System.out.println("Metrics values by class :");
        }
        for (GranularityScale c : nodes) {
            AbstractnessData abstractnessData = aData.get(fileToNodeMapping.get(c));
            Metrics metrics = new Metrics();
            metrics.setAbstractness(abstractnessData);
            metrics.setAfferentCoupling(graph, c);
            metrics.setEfferentCoupling(graph, c);
            metrics.setInstability(metrics.getAfferentCoupling(), metrics.getEfferentCoupling());
            metrics.setNormalizedDistance(metrics.getAbstractness(), metrics.getInstability());
            c.setMetrics(metrics);

            if(!dotOnly) {
                System.out.println(c.getName());
                System.out.println("\tA : " + metrics.getAbstractness());
                System.out.println("\tCa : " + metrics.getAfferentCoupling());
                System.out.println("\tCe : " + metrics.getEfferentCoupling());
                System.out.println("\tI : " + metrics.getInstability());
                System.out.println("\tDn : " + metrics.getNormalizedDistance());
            }
        }

        // Graph's DOT representation building
        GraphPresentationBuilder gBuilder = new GraphDotBuilder();
        gBuilder.createNewGraph();
        for (GranularityScale node : graph.getNodeSet()) {
            gBuilder.addNode(node);
        }
        for (GranularityScale node : graph.getNodeSet()) {
            for (DependencyEdge edge : graph.getOutcomingEdgeList(node)) {
                gBuilder.addEdge(edge);
            }
        }
        gBuilder.endGraph();
        if(!dotOnly) {
            System.out.println();
            System.out.println("DOT-formatted dependency graph :");
        }
        System.out.println(gBuilder.getGraphPresentation());
    }

}
