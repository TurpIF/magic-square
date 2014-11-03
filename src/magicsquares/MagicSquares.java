package magicsquares;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

public class MagicSquares {
    public static interface SolverFactory {
        public abstract Solver build();
    }

    public static class BasicSolverFactory implements SolverFactory {
        public Solver build() {
            return new Solver();
        }
    }

    public static IntVar[][] solveMagicSquare(int n, SolverFactory solverFactory) {
        Solver solver = solverFactory.build();
        IntVar sum = VariableFactory.fixed((n * (n * n + 1)) / 2, solver);
        IntVar[][] vs = VariableFactory.enumeratedMatrix("vs", n, n, 1, n * n, solver);

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

    public static void main(String[] args) {
        final int n = 6;
        IntVar[][] solution = solveMagicSquare(n, new BasicSolverFactory());
        displaySolution(solution);
    }

    // Affichage d'une solution trouvee
    public static void displaySolution(IntVar[][] solution) {
        for(int i = 0; i < solution.length; i++) {
            for(int j = 0; j < solution[i].length; j++) {
                System.out.print(solution[i][j].getValue());
                System.out.print(' ');
            }
            System.out.println();
        }
    }
}
