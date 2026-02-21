import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

class Fraction {
    BigInteger num;
    BigInteger den;

    public Fraction(BigInteger n, BigInteger d) {
        if (d.signum() == 0)
            throw new ArithmeticException("Denominator cannot be zero");

        if (d.signum() < 0) {
            n = n.negate();
            d = d.negate();
        }

        BigInteger gcd = n.gcd(d);
        num = n.divide(gcd);
        den = d.divide(gcd);
    }

    public Fraction add(Fraction other) {
        BigInteger n = num.multiply(other.den)
                .add(other.num.multiply(den));
        BigInteger d = den.multiply(other.den);
        return new Fraction(n, d);
    }

    public Fraction multiply(Fraction other) {
        return new Fraction(
                num.multiply(other.num),
                den.multiply(other.den)
        );
    }

    public Fraction multiply(BigInteger val) {
        return new Fraction(num.multiply(val), den);
    }

    public String toString() {
        if (den.equals(BigInteger.ONE))
            return num.toString();
        return num + "/" + den;
    }
}

public class PolynomialSolver {

    public static void solve(String filePath) throws Exception {

        String json = new String(Files.readAllBytes(Paths.get(filePath)));

        int n = Integer.parseInt(json.split("\"n\":")[1].split(",")[0].trim());
        int k = Integer.parseInt(json.split("\"k\":")[1].split("}")[0].trim());

        List<BigInteger> xVals = new ArrayList<>();
        List<BigInteger> yVals = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            if (json.contains("\"" + i + "\"")) {

                String block = json.split("\"" + i + "\":")[1];

                String baseStr = block.split("\"base\":")[1]
                        .split(",")[0]
                        .replaceAll("[^0-9]", "");

                String valueStr = block.split("\"value\":")[1]
                        .split("}")[0]
                        .replaceAll("[^0-9a-zA-Z]", "");

                int base = Integer.parseInt(baseStr);
                BigInteger y = new BigInteger(valueStr, base);

                xVals.add(BigInteger.valueOf(i));
                yVals.add(y);
            }
        }

        int degree = k - 1;

        Fraction[] result = new Fraction[degree + 1];
        for (int i = 0; i <= degree; i++)
            result[i] = new Fraction(BigInteger.ZERO, BigInteger.ONE);

        // Lagrange interpolation
        for (int i = 0; i < k; i++) {

            Fraction[] basis = new Fraction[1];
            basis[0] = new Fraction(BigInteger.ONE, BigInteger.ONE);

            Fraction denom = new Fraction(BigInteger.ONE, BigInteger.ONE);

            for (int j = 0; j < k; j++) {
                if (i == j) continue;

                // Multiply basis by (x - xj)
                Fraction[] newBasis = new Fraction[basis.length + 1];
                Arrays.fill(newBasis, new Fraction(BigInteger.ZERO, BigInteger.ONE));

                for (int b = 0; b < basis.length; b++) {
                    // x term
                    newBasis[b + 1] = newBasis[b + 1].add(basis[b]);
                    // constant term
                    newBasis[b] = newBasis[b].add(
                            basis[b].multiply(xVals.get(j).negate())
                    );
                }

                basis = newBasis;

                denom = denom.multiply(
                        new Fraction(
                                xVals.get(i).subtract(xVals.get(j)),
                                BigInteger.ONE
                        )
                );
            }

            Fraction scale = new Fraction(yVals.get(i), BigInteger.ONE)
                    .multiply(new Fraction(denom.den, denom.num));

            for (int d = 0; d < basis.length; d++) {
                basis[d] = basis[d].multiply(scale);
            }

            for (int d = 0; d <= degree; d++) {
                if (d < basis.length)
                    result[d] = result[d].add(basis[d]);
            }
        }

        System.out.println("Polynomial Coefficients (Highest → Constant):");

        for (int i = degree; i >= 0; i--) {
            System.out.println(result[i]);
        }
    }
}