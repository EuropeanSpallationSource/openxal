/**
 * TestR2x2.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 27, 2013
 */
package xal.tools.math.r2;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * JUnit test cases for class <code>xal.math.r2.R2x2</code>.
 *
 *
 * @author Christopher K. Allen
 * @since Sep 27, 2013
 */
@RunWith(JUnit4.class)
public class TestR2x2 {

    /*
     * Global Attributes
     */
    /**
     * Static identity matrix
     */
    private static R2x2 MAT_I;

    /**
     * static symplectic matrix
     */
    private static R2x2 MAT_J;

    /**
     * Static testing matrix
     */
    private static R2x2 MAT_2;

    /**
     * static testing matrix - a rotation
     */
    private static R2x2 MAT_R;

    @BeforeClass
    public static void buildTestingResources() {

        MAT_I = R2x2.newIdentity();
        MAT_J = R2x2.newSymplectic();
        MAT_2 = MAT_I.plus(MAT_I);
        MAT_R = R2x2.newRotation(Math.PI / 2.0);
    }

    /**
     * Test method for {@link xal.tools.math.r2.R2x2#newZero()}.
     */
    @Test
    public void testZero() {
        R2x2 matTest = R2x2.newZero();

//		fail("Not able to create a zero matrix");
    }

    /**
     * Test method for {@link xal.tools.math.r2.R2x2#getSize()}.
     */
    @Test
    public void testGetSize() {
        R2x2 matTest = new R2x2();

        int szMatrix = matTest.getSize();

        Assert.assertTrue(szMatrix == 2);

//		LOGGER.log(Level.INFO, "\nTest matrix dynamic size = " + szMatrix);
    }

    @Test
    public void testMatrixAddition() {
        R2x2 mat1 = R2x2.newIdentity();
        R2x2 mat2 = R2x2.newIdentity();

        R2x2 matSum = mat1.plus(mat2);

        Assert.assertTrue(matSum.isEquivalentTo(MAT_2));

//	    LOGGER.log(Level.INFO, "\nThe matrix addition test");
//	    LOGGER.log(Level.INFO,  matSum.toString() );
    }

    @Test
    public void testMatrixInPlaceAddition() {
        R2x2 mat1 = R2x2.newIdentity();
        R2x2 mat2 = R2x2.newIdentity();

        mat1.plusEquals(mat2);

        Assert.assertTrue(mat1.isEquivalentTo(MAT_2));

//        LOGGER.log(Level.INFO, "\nThe matrix in place addition test");
//        LOGGER.log(Level.INFO,  mat1.toString() );
    }

    @Test
    public void testMatrixMultiplication() {
        R2x2 mat1 = R2x2.newIdentity();
        R2x2 mat2 = R2x2.newIdentity();

        R2x2 matProd = mat1.times(mat2);

        Assert.assertTrue(matProd.isEquivalentTo(MAT_I));

//        LOGGER.log(Level.INFO, "\nThe matrix multiplication test");
//        LOGGER.log(Level.INFO,  matProd.toString() );
    }

    @Test
    public void testMatrixInPlaceMultiplication() {
        R2x2 mat1 = R2x2.newIdentity();
        R2x2 mat2 = R2x2.newIdentity();

        mat1.timesEquals(mat2);

        Assert.assertTrue(mat1.isEquivalentTo(MAT_I));

//        LOGGER.log(Level.INFO, "\nThe matrix in place multiplication test");
//        LOGGER.log(Level.INFO,  mat1.toString() );
    }

    @Test
    public void testMatrixDeterminant() {
        double dblDetSp2 = MAT_J.det();
        double dblDetId = MAT_I.det();
        double dblDetTst2 = MAT_2.det();

//        LOGGER.log(Level.INFO, "\nDeterminant Function");
//        LOGGER.log(Level.INFO, "|I|  = " + dblDetId);
//        LOGGER.log(Level.INFO, "|2I| = " + dblDetTst2);
//        LOGGER.log(Level.INFO, "|J|  = " + dblDetSp2);
    }

    @Test
    public void testMatrixOperations() {
        R2x2 matTrn = MAT_J.transpose();
        R2x2 matInv = MAT_J.inverse();
        R2x2 matCjt = MAT_J.conjugateTrans(MAT_R);

//        LOGGER.log(Level.INFO, "\nMatrix Operations");
//        LOGGER.log(Level.INFO, "Sp(2) matrix J = " + MAT_J);
//        LOGGER.log(Level.INFO, "transpose of J = " + matTrn);
//        LOGGER.log(Level.INFO, "inverse of J   = " + matInv);
//        LOGGER.log(Level.INFO, "CT of J w/ Rot = " + matCjt);
    }

    @Test
    public void testMatrixNorm() {
        double dblL1 = MAT_J.norm1();
        double dblL2 = MAT_J.norm2();
        double dblLinf = MAT_J.normInf();
        double dblFrob = MAT_J.normF();

//        LOGGER.log(Level.INFO, "\nNorms of the Symplectic Matrix");
//        LOGGER.log(Level.INFO, "||J||_1   = " + dblL1);
//        LOGGER.log(Level.INFO, "||J||_2   = " + dblL2);
//        LOGGER.log(Level.INFO, "||J||_inf = " + dblLinf);
//        LOGGER.log(Level.INFO, "||J||_F   = " + dblFrob);
    }

    @Test
    public void testRandom() {
    }

}
