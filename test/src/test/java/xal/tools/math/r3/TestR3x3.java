/**
 * TestR3x3.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 27, 2013
 */
package xal.tools.math.r3;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * JUnit test cases for class <code>xal.math.r3.R3x3</code>.
 *
 *
 * @author Christopher K. Allen
 * @since Sep 27, 2013
 */
@RunWith(JUnit4.class)
public class TestR3x3 {

    /*
     * Global Attributes
     */
    /**
     * Static identity matrix
     */
    private static R3x3 MAT_I;

    /**
     * Static testing matrix
     */
    private static R3x3 MAT_2;

    /**
     * static rotation matrix about x axis
     */
    private static R3x3 MAT_X;

    /**
     * static testing matrix - a rotation about z axis
     */
    private static R3x3 MAT_Z;

    /**
     * static symmetric test matrix
     */
    private static R3x3 MAT_S;

    @BeforeClass
    public static void buildTestingResources() {

        MAT_I = R3x3.newIdentity();
        MAT_2 = MAT_I.plus(MAT_I);
        MAT_X = R3x3.newRotationX(Math.PI / 4.0);
        MAT_Z = R3x3.newRotationZ(Math.PI / 4.0);

        MAT_S = new R3x3(new double[][]{{1.0, 0.0, 0.0}, {0.0, 2.0, 0.0}, {0.0, 0.0, 3.0}});
        MAT_S = MAT_S.conjugateTrans(MAT_Z);
    }

    /**
     * Test method for {@link xal.tools.math.r2.R3x3#newZero()}.
     */
    @Test
    public void testZero() {
        R3x3 matTest = R3x3.newZero();

    }

    /**
     * Test method for {@link xal.tools.math.r2.R3x3#getSize()}.
     */
    @Test
    public void testGetSize() {
        R3x3 matTest = new R3x3();

        int szMatrix = matTest.getSize();

        Assert.assertTrue(szMatrix == 3);

    }

    @Test
    public void testMatrixAddition() {
        R3x3 mat1 = R3x3.newIdentity();
        R3x3 mat2 = R3x3.newIdentity();

        R3x3 matSum = mat1.plus(mat2);

        Assert.assertTrue(matSum.isEquivalentTo(MAT_2));

    }

    @Test
    public void testMatrixInPlaceAddition() {
        R3x3 mat1 = R3x3.newIdentity();
        R3x3 mat2 = R3x3.newIdentity();

        mat1.plusEquals(mat2);

        Assert.assertTrue(mat1.isEquivalentTo(MAT_2));

    }

    @Test
    public void testMatrixMultiplication() {
        R3x3 mat1 = R3x3.newIdentity();
        R3x3 mat2 = R3x3.newIdentity();

        R3x3 matProd = mat1.times(mat2);

        Assert.assertTrue(matProd.isEquivalentTo(MAT_I));

    }

    @Test
    public void testMatrixInPlaceMultiplication() {
        R3x3 mat1 = R3x3.newIdentity();
        R3x3 mat2 = R3x3.newIdentity();

        mat1.timesEquals(mat2);

        Assert.assertTrue(mat1.isEquivalentTo(MAT_I));

    }

    @Test
    public void testMatrixDeterminant() {
        double dblDetRx = MAT_X.det();
        double dblDetId = MAT_I.det();
        double dblDetTst2 = MAT_2.det();

    }

    @Test
    public void testMatrixOperations() {
        R3x3 matTrn = MAT_X.transpose();
        R3x3 matInv = MAT_X.inverse();
        R3x3 matCjt = MAT_X.conjugateTrans(MAT_Z);

    }

    @Test
    public void testMatrixNorm() {
        double dblL1 = MAT_X.norm1();
        double dblL2 = MAT_X.norm2();
        double dblLinf = MAT_X.normInf();
        double dblFrob = MAT_X.normF();

    }

    @Test
    public void testJacobiDecomposition() throws IllegalArgumentException, InstantiationException {
        R3x3 matR = MAT_S;

        try {
            R3x3JacobiDecomposition jacR = new R3x3JacobiDecomposition(matR);

            R3x3 matO = jacR.getRotationMatrix();
            R3x3 matD = jacR.getDiagonalMatrix();

        } catch (IllegalArgumentException e) {

            fail("matrix not symmetric " + matR);

        }

    }

    @Test
    public void testEigenValueDecomposition() throws IllegalArgumentException, InstantiationException {
        R3x3 matT = MAT_S;

        try {
            R3x3EigenDecomposition decL = new R3x3EigenDecomposition(matT);

            R3x3 matE = decL.getEigenvalueMatrix();
            R3x3 matV = decL.getEigenvectorMatrix();

        } catch (IllegalArgumentException e) {

            fail("matrix not symmetric " + matT);

        }

    }

}
