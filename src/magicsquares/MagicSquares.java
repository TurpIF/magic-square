package magicsquares;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.ISF;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.VariableSelectorWithTies;
import solver.search.strategy.selectors.values.IntDomainMax;
import solver.search.strategy.selectors.values.IntDomainMiddle;
import solver.search.strategy.selectors.values.IntDomainMin;
import solver.search.strategy.selectors.values.IntDomainRandom;
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

    public static class SiamoiseStrategyFactory extends StrategyFactory {
        public AbstractStrategy build(IntVar[][] vars) {
            // Strategie valable uniquement pour les carrés d'ordre impaire
            if (vars.length % 2 != 1) {
                return null;
            }
            final int n = vars.length;
            return ISF.custom(new VariableSelector<IntVar>() {
                @Override
                public IntVar getVariable(IntVar[] intVars) {
                    if (intVars.length < n) {
                        return null;
                    }

                    if (!intVars[n / 2].isInstantiated()) {
                        return intVars[n / 2];
                    }

                    if (intVars.length < n * n) {
                        return null;
                    }

                    // On cherche la variable définie ayant la plus grande valeur
                    int index = -1;
                    int maxValue = intVars[0].getLB();
                    for (int i = 0 ; i < intVars.length ; i++) {
                        if (intVars[i].isInstantiated() && intVars[i].getValue() >= maxValue) {
                            index = i;
                            maxValue = intVars[i].getValue();
                        }
                    }
                    if (index != -1) {
                        // On prend la case en diagonal (haut droite) de la variable trouvée
                        // Si la case est déjà défini, on descend d'une case
                        int x = index % n;
                        int y = (index - x) / n;
                        int indexDiag = ((x + 1) % n) + ((y - 1) % n) * n;
                        if (intVars[indexDiag].isInstantiated()) {
                            int indexBis = x + ((y + 1) % n) * n;
                            return intVars[indexBis];
                        }
                        return intVars[indexDiag];
                    }
                    return null;
                }
            }, new IntDomainMin(), ArrayUtils.flatten(vars));
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
        for (int i = 0; i < n; i++) {
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
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
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
                long start = System.nanoTime();
                solveMagicSquare(i, strategy);
                long end = System.nanoTime();
                ps.print("\t" + (end - start));
            }
            ps.println();
        }
    }

    public static void allTests(int n, PrintStream ps) {
        test(n, ps,
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
                new RandomIntDomainRandomStrategyFactory(),
                new SiamoiseStrategyFactory());
    }

    //
    // Main
    //
    public static void main(String[] args) {
        int n = 10;
        if (args.length > 1) {
            n = Integer.parseInt(args[1]);
        }

        String xp_name = "compute.csv";
        try {
            File f = new File("./" + xp_name);
            PrintStream ps = new PrintStream(f);
            allTests(n, ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // allTests(n, System.out);
    }
}
