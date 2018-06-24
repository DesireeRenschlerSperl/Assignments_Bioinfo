import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jfree.ui.RefineryUtilities;

/**
 * Wright-Fischer & Kingman coalescent-based pop gen simulator
 * Grundlagen der Bioinformatik, University of Tuebingen, 2018, Prof. Daniel Huson
 * Rabanus Derr, Marvin Döbel
 */
public class PopGenSimulator {
    public static final String authors = "Rabanus Derr und Marvin Döbel";

    public static final String NOCHILD = "---";


    //INPUT CHANGES for PLOTTING
    public static final int MAX_POPSIZE = 100;
    public static final int MAX_SAMPLESIZE = 8;
    public static final int MIN_SAMPLESIZE = 4;

    /**
     * setup the extant (present-day) generation (generation 0). Name the 2*N=n2 individuals as A01, A02, A03, A04...
     * We will assume that the first k individuals in the present-day generation are the ones to be tracked backward through time
     *
     * @param k  the number of individuals in the "sample" to be tracked.
     * @param n2 the total number of individuals in a generation, 2*N
     * @return
     */
    public String[] setupExtantGeneration(int k, int n2) {
        String[] presentGeneration = new String[n2];
        for (int i = 0; i < presentGeneration.length; i++){
            presentGeneration[i] = String.format("A%02d", i+1);
        }
        return presentGeneration;
    }

    /**
     * given an array of strings, each string representing one individual,
     * randomly generate previous generation. In previous generation, use lexicographically first strings of all
     * children as label of parent, or ---, if parent has no child
     *
     * @param currentGeneration
     * @return previous generation
     */
    public String[] simulatePreviousGeneration(String[] currentGeneration) {
        String[] previousGeneration = new String[currentGeneration.length];



        //Initialisierung der Elterngeneration
        for (int i = 0; i < previousGeneration.length; i++){
            previousGeneration[i] = NOCHILD;
        }

        for (int i = 0; i < previousGeneration.length; i++){
            //Bereits ausgestorbene Ax suchen keinen Vorfahre mehr...
            if(!currentGeneration[i].equals(NOCHILD)) {
                int ancestor = determineAncestor(currentGeneration);
                //Wenn ein ancestor noch kein Kind hat dann er jetzt sein Kind gefunden.
                if (previousGeneration[ancestor].equals(NOCHILD)) {
                    previousGeneration[ancestor] = currentGeneration[i];

                    //Wähle den lexikographisch kleinsten Ax aus.
                } else if (Integer.parseInt(previousGeneration[ancestor].substring(1)) > Integer.parseInt(currentGeneration[i].substring(1))) {
                    previousGeneration[ancestor] = currentGeneration[i];
                }
            }
        }
        return previousGeneration;
    }

    private int determineAncestor(String[] currentGeneration) {
        int ancestor = (int) (Math.random() * currentGeneration.length);
        return ancestor;
    }

    /**
     * how many different individuals are ancestors of the k the individuals that we are tracking?
     *
     * @param currentGeneration
     * @param k
     * @return number of ancestors of the k individuals
     */
    public int determineNumberOfAncestorsOfSample(String[] currentGeneration, int k) {
        int newk = 0;

        for (int i = 0; i < currentGeneration.length; i++){
            if (currentGeneration[i].equals(NOCHILD)){
                continue;
            } else {
                if (Integer.parseInt(currentGeneration[i].substring(1)) <= k) {
                    newk++;
                }
            }
        }

        return newk;
    }


    public static void main(String[] args) throws IOException {
        System.out.println("PopGenSimulator by " + authors);

        System.out.print("Enter population size (2*N): ");
        System.out.flush();
        final int n2 = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());
        System.out.print("Enter sample size (k): ");
        System.out.flush();
        final int k = Integer.parseInt((new BufferedReader(new InputStreamReader(System.in))).readLine());

        final PopGenSimulator popGenSimulator = new PopGenSimulator();

        // 1 setup the extant generation
        String[] currentGeneration = popGenSimulator.setupExtantGeneration(k,n2);

        //2 setup output begin
        System.out.println("gen" + determineBlankSpace(currentGeneration) + "population" + determineBlankSpace(currentGeneration) +  "lineages");

        //String[] currentGeneration = popGenSimulator.simulatePreviousGeneration(extrantGeneration);

        int generation = 0;
        int newk = k;

        // 3 until we have found the MRCA of the first k individuals, simulate the previous generation and produce line of output
        while (newk != 1){
            printGeneration(generation);
            printPopulation(currentGeneration);
            printLineage(newk);

            currentGeneration = popGenSimulator.simulatePreviousGeneration(currentGeneration);
            newk = popGenSimulator.determineNumberOfAncestorsOfSample(currentGeneration, k);
            generation--;
        }

        printGeneration(generation);
        printPopulation(currentGeneration);
        printLineage(newk);

//        popGenSimulator.determineNumberOfAncestorsOfSample(extrantGeneration,k);
//        String[] nextGeneration = popGenSimulator.simulatePreviousGeneration(extrantGeneration);
//
//        for (int i = 0; i < nextGeneration.length;i++){
//            System.out.println(nextGeneration[i]);
//        }


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //calculating simulations for plots
        //can take a while ;)


        //testing the mcra time for different population sizes but a given k (sample size)

        int[][][] popSizeTimeMCRA = new int[MAX_SAMPLESIZE - MIN_SAMPLESIZE+1][MAX_POPSIZE][2];

        //iterate through all possible sample sizes
        for(int j = 0; j < popSizeTimeMCRA.length; j++) {

            //starting the itaeration through all population sizes at the minimum possible sample size
            for (int i = j+MIN_SAMPLESIZE; i < popSizeTimeMCRA[j].length; i++) {
                //initialise the different start generations
                generation = 0;
                newk = j+MIN_SAMPLESIZE;

                currentGeneration = popGenSimulator.setupExtantGeneration(newk,i);

                // 3 until we have found the MRCA of the first k individuals, simulate the previous generation and produce line of output
                while (newk != 1){
                    currentGeneration = popGenSimulator.simulatePreviousGeneration(currentGeneration);
                    newk = popGenSimulator.determineNumberOfAncestorsOfSample(currentGeneration, k);
                    generation--;
                }

                popSizeTimeMCRA[j][i][0] = i;
                popSizeTimeMCRA[j][i][1] = generation;
            }

        }


        //PLotter GUI
        final TimePopSampleSizePlotter timePopSampleSizePlotter =
                new TimePopSampleSizePlotter("Time Plotter for MCRA for a specific sample sizes and populations",popSizeTimeMCRA);
        timePopSampleSizePlotter.pack();
        RefineryUtilities.centerFrameOnScreen(timePopSampleSizePlotter);
        timePopSampleSizePlotter.setVisible(true);
    }

    private static void printLineage(int k) {
        System.out.print(k + "\n");
    }

    private static void printPopulation(String[] currentGeneration) {
        for (int i = 0; i < currentGeneration.length; i++){
            System.out.print(currentGeneration[i] + " ");
        }
    }

    private static void printGeneration(int generation) {
        if (generation == 0) {
            System.out.print(" " + generation + " ");
        } else {
            System.out.print(generation + " ");
        }
    }

    private static String determineBlankSpace(String[] extrantGeneration) {
        String blankSpace = "";
        for (int i = 0; i < extrantGeneration.length/2-2; i++){
            blankSpace = blankSpace.concat("     ");
        }
        return blankSpace;
    }
}

