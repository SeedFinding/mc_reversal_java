package mjtb49.hashreversals;

import kaptainwutax.mathutils.arithmetic.Rational;
import kaptainwutax.mathutils.component.matrix.QMatrix;
import kaptainwutax.mathutils.component.vector.QVector;
import kaptainwutax.mathutils.lattice.LagrangeGauss;
import kaptainwutax.mathutils.util.Mth;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Lattice2D extends QMatrix {

    protected final BigInteger inverseB;
    protected final BigInteger mod;

    protected final QMatrix inverse;

    public Lattice2D(long a, long b, long mod) {
        this(BigInteger.valueOf(a), BigInteger.valueOf(b), BigInteger.valueOf(mod));
    }

    public Lattice2D(BigInteger a, BigInteger b, BigInteger mod) {
        super(2, 2);

        if(!Mth.isPowerOf2(mod)) {
            System.err.println("FindSolutionsInBox does not support non power of 2 modulus!");
        }

        this.mod = mod;
        this.inverseB = b.modInverse(mod);

        this.set(0, 0, Rational.ZERO);
        this.set(0, 1, Rational.of(mod));
        this.set(1, 0, Rational.ONE);
        this.set(1, 1, Rational.of(this.inverseB.multiply(a.negate())));

        LagrangeGauss.reduceAndSet(this);
        this.inverse = this.invert();
    }

    public BigInteger getMod() {
        return this.mod;
    }

    @Override
    public Rational getDeterminant() {
        Rational a = this.get(0, 0).multiply(this.get(1, 1));
        Rational b = this.get(1, 0).multiply(this.get(0, 1));
        return a.subtract(b);
    }

    @Override
    public QMatrix invert() {
        if(this.inverse != null)return this.inverse;

        if(this.getDeterminant().compareTo(Rational.ZERO) == 0) {
            throw new IllegalStateException("Cannot invert a singular matrix");
        }

        return new QMatrix(this.getRowCount(), this.getColumnCount(), (row, column) -> {
            if(row != column)return this.get(row, column).negate();
            return this.get((row + 1) % 2, (column + 1) % 2);
        }).divideAndSet(this.getDeterminant());
    }

    public List<QVector> findSolutionsInBox(long target, long minX, long minZ, long maxX, long maxZ) {
        return this.findSolutionsInBox(BigInteger.valueOf(target), BigInteger.valueOf(minX), BigInteger.valueOf(minZ),
                BigInteger.valueOf(maxX), BigInteger.valueOf(maxZ));
    }

    public List<QVector> findSolutionsInBox(BigInteger target, BigInteger minX, BigInteger minZ, BigInteger maxX, BigInteger maxZ) {
        BigInteger newZCenter = this.inverseB.multiply(target).mod(this.mod);
        minZ = minZ.subtract(newZCenter);
        maxZ = maxZ.subtract(newZCenter);

        //Overkill - gets mins and maxes for what x and z in the space transformed by inv can be
        Rational[] transformedMins = {Rational.ZERO, Rational.ZERO};
        Rational[] transformedMaxes = {Rational.ZERO, Rational.ZERO};

        for(int row = 0; row < 2; row++) {
            BigInteger minCoord = row == 0 ? minX : minZ, maxCoord = row == 0 ? maxX : maxZ;

            for(int col = 0; col < 2; col++) {
                Rational e = this.inverse.get(row, col);
                transformedMaxes[col] = transformedMaxes[col].add(e.multiply(e.signum() >= 0 ? maxCoord : minCoord));
                transformedMins[col] = transformedMins[col].add(e.multiply(e.signum() >= 0 ? minCoord : maxCoord));
            }
        }

        List<QVector> validCoords = new ArrayList<>();

        for(long x = transformedMins[0].longValue() - 2; x < transformedMaxes[0].longValue() + 2; x++) {
            for(long z = transformedMins[1].longValue() - 2; z < transformedMaxes[1].longValue() + 2; z++) {
                QVector coords = new QVector(Rational.of(x), Rational.of(z)).multiply(this.transpose());
                if(coords.get(0).compareTo(Rational.of(minX)) < 0)continue;
                if(coords.get(0).compareTo(Rational.of(maxX)) > 0)continue;
                if(coords.get(1).compareTo(Rational.of(minZ)) < 0)continue;
                if(coords.get(1).compareTo(Rational.of(maxZ)) > 0)continue;
                validCoords.add(coords.addAndSet(new QVector(Rational.ZERO, Rational.of(newZCenter))));
            }
        }

        return validCoords;
    }

}
