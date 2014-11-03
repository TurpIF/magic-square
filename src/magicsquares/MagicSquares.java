package magicsquares;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.search.strategy.ISF;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.selectors.VariableSelectorWithTies;
import solver.search.strategy.selectors.values.IntDomainMax;
import solver.search.strategy.selectors.values.IntDomainMin;
import solver.search.strategy.selectors.variables.FirstFail;
import solver.search.strategy.selectors.variables.Random;
import solver.search.strategy.selectors.variables.Smallest;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

public class MagicSquares {
    public static interface StrategyFactory {
        public abstract AbstractStrategy build(IntVar... vars);
    }

    public static class DefaultStrategyFactory implements StrategyFactory {
        public AbstractStrategy build(IntVar... vars) {
            return null;
        }
    }

    public static class BasicStrategyFactory implements StrategyFactory {
        public AbstractStrategy build(IntVar... vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Smallest()), new IntDomainMax(), vars);
        }
    }

    public static IntVar[][] solveMagicSquare(int n, StrategyFactory strategyFactory) {
        Solver solver = new Solver();
        IntVar sum = VariableFactory.fixed((n * (n * n + 1)) / 2, solver);
        IntVar[][] vs = VariableFactory.enumeratedMatrix("vs", n, n, 1, n * n, solver);

        AbstractStrategy strategy = strategyFactory.build(ArrayUtils.flatten(vs));
        if (strategy != null) {
            solver.set(strategy);
        }

        // Différence
        solver.post(IntConstraintFactory.alldifferent(ArrayUtils.flatten(vs)));

        IntVar[] diagonal1 = new IntVar[n];
        IntVar[] diagonal2 = new IntVar[n];

        // Lignes et colonnes
        for(int i = 0; i < n; i++) {
            solver.post(IntConstraintFactory.sum(vs[i], sum));
            solver.post(IntConstraintFactory.sum(ArrayUtils.getColumn(vs, i), sum));
            diagonal1[i] = vs[i][i];
            diagonal2[i] = vs[i][n - i - 1];
        }

        // Diagonales
        solver.post(IntConstraintFactory.sum(diagonal1, sum));
        solver.post(IntConstraintFactory.sum(diagonal2, sum));

        solver.findSolution();

        return vs;
    }
    
    //
    // Affichage d'une solution trouvée
    //
    public static void displaySolution(IntVar[][] solution) {
        for(int i = 0; i < solution.length; i++) {
            for(int j = 0; j < solution[i].length; j++) {
                System.out.print(solution[i][j].getValue());
                System.out.print(' ');
            }
            System.out.println();
        }
    }
    
    
    //
    // Tests automatiques 
    //
    // Écrit un tableau avec la taille du carré et le temps de résolution associé
    public static void test(int n, PrintStream ps, StrategyFactory strategyFactory) {
        for(int i = 1; i <= n; i++) {
            double start = System.currentTimeMillis();
            solveMagicSquare(i, strategyFactory);
            double end = System.currentTimeMillis();
            ps.println(i + "\t" + (end - start));
        }
    }
    

    //
    // Main
    //
    public static void main(String[] args) {
        
        final int n = 6;
        /*String xp_name = "xp_1";
        try {
            File f = new File("C:\\Users\\Jonathan\\SkyDrive\\Université\\M2\\Résolution de problèmes combinatoires\\Projet\\" + xp_name);
            PrintStream ps = new PrintStream(f);
            test(n, ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        test(n, System.out, new DefaultStrategyFactory());
        
    }
}
