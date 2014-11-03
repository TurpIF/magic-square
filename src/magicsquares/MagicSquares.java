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
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.VariableSelectorWithTies;
import solver.search.strategy.selectors.values.IntDomainMax;
import solver.search.strategy.selectors.values.IntDomainMiddle;
import solver.search.strategy.selectors.values.IntDomainMin;
import solver.search.strategy.selectors.values.IntDomainRandom;
import solver.search.strategy.selectors.variables.FirstFail;
import solver.search.strategy.selectors.variables.Largest;
import solver.search.strategy.selectors.variables.Random;
import solver.search.strategy.selectors.variables.Smallest;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

public class MagicSquares {
    public static abstract class StrategyFactory {
        public abstract AbstractStrategy build(IntVar[][] vars);

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class DefaultStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return null;
        }
    }

    public static class SmallestIntDomainMaxStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Smallest()), new IntDomainMax(), ArrayUtils.flatten(vars));
        }
    }

    public static class SmallestIntDomainMinStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Smallest()), new IntDomainMin(), ArrayUtils.flatten(vars));
        }
    }

    public static class SmallestIntDomainMiddleStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Smallest()), new IntDomainMiddle(), ArrayUtils.flatten(vars));
        }
    }

    public static class SmallestIntDomainRandomStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Smallest()), new IntDomainRandom(42L), ArrayUtils.flatten(vars));
        }
    }

    public static class LargestIntDomainMaxStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Largest()), new IntDomainMax(), ArrayUtils.flatten(vars));
        }
    }

    public static class LargestIntDomainMinStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Largest()), new IntDomainMin(), ArrayUtils.flatten(vars));
        }
    }

    public static class LargestIntDomainMiddleStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Largest()), new IntDomainMiddle(), ArrayUtils.flatten(vars));
        }
    }

    public static class LargestIntDomainRandomStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Largest()), new IntDomainRandom(42L), ArrayUtils.flatten(vars));
        }
    }

    public static class RandomIntDomainMaxStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Random<IntVar>(42L)), new IntDomainMax(), ArrayUtils.flatten(vars));
        }
    }

    public static class RandomIntDomainMinStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Random<IntVar>(42L)), new IntDomainMin(), ArrayUtils.flatten(vars));
        }
    }

    public static class RandomIntDomainMiddleStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Random<IntVar>(42L)), new IntDomainMiddle(), ArrayUtils.flatten(vars));
        }
    }

    public static class RandomIntDomainRandomStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            return ISF.custom(new VariableSelectorWithTies<IntVar>(new Random<IntVar>(42L)), new IntDomainRandom(42L), ArrayUtils.flatten(vars));
        }
    }

    public static IntVar[][] solveMagicSquare(int n, StrategyFactory strategyFactory) {
        Solver solver = new Solver();
        IntVar sum = VariableFactory.fixed((n * (n * n + 1)) / 2, solver);
        IntVar[][] vs = VariableFactory.enumeratedMatrix("vs", n, n, 1, n * n, solver);

        AbstractStrategy strategy = strategyFactory.build(vs);
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
    public static void test(int n, PrintStream ps, StrategyFactory... strategies) {
        ps.print("Strategy");
        for (int i = 1; i <= n; i++) {
            ps.print("\tn=" + i);
        }
        ps.println();

        for (StrategyFactory strategy : strategies) {
            ps.print(strategy.toString());
            for (int i = 1; i <= n; i++) {
                double start = System.currentTimeMillis();
                solveMagicSquare(i, strategy);
                double end = System.currentTimeMillis();
                ps.print("\t" + (end - start));
            }
            ps.println();
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
        test(n, System.out,
                new DefaultStrategyFactory(),
                new LargestIntDomainMaxStrategyFactory(),
                new LargestIntDomainMiddleStrategyFactory(),
                new LargestIntDomainMinStrategyFactory(),
                new LargestIntDomainRandomStrategyFactory(),
                new SmallestIntDomainMaxStrategyFactory(),
                new SmallestIntDomainMiddleStrategyFactory(),
                new SmallestIntDomainMinStrategyFactory(),
                new SmallestIntDomainRandomStrategyFactory(),
                new RandomIntDomainMaxStrategyFactory(),
                new RandomIntDomainMiddleStrategyFactory(),
                new RandomIntDomainMinStrategyFactory(),
                new RandomIntDomainRandomStrategyFactory());
        
    }
}
