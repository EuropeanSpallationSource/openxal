package xal.extension.jels.smf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xal.extension.jels.model.elem.FieldMapPoint;
import xal.extension.jels.smf.impl.FieldMapFactory.FieldType;

/**
 * Loading and interpolation of field maps, using small field map files written to a temporary folder.
 */
public class FieldMapFilesTest {

    private static final double TOLERANCE = 1e-12;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String path() {
        return folder.getRoot().toURI().toString();
    }

    private void write(String name, String content) throws IOException {
        Files.writeString(new File(folder.getRoot(), name).toPath(), content, StandardCharsets.UTF_8);
    }

    /**
     * A 3D ASCII field map with 2 points along z (length 1 m) and an n x n transverse grid spanning [-half, half] in x
     * and y. The value at each grid point is given by {@code field}.
     */
    private void write3D(String name, int n, double half, Field3D field) throws IOException {
        write3D(name, n, half, n, half, field);
    }

    /**
     * A 3D ASCII field map in the TraceWin layout: header lines "nz zmax", "nx xmin xmax", "ny ymin ymax" and the norm,
     * then the values with x varying fastest and z slowest.
     */
    private void write3D(String name, int nx, double halfX, int ny, double halfY, Field3D field) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("1 1.0\n");
        content.append(nx - 1).append(" ").append(-halfX).append(" ").append(halfX).append("\n");
        content.append(ny - 1).append(" ").append(-halfY).append(" ").append(halfY).append("\n");
        content.append("1.0\n");
        double spacingX = 2 * halfX / (nx - 1);
        double spacingY = 2 * halfY / (ny - 1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nx; k++) {
                    content.append(field.at(-halfX + k * spacingX, -halfY + j * spacingY)).append("\n");
                }
            }
        }
        write(name, content.toString());
    }

    @FunctionalInterface
    private interface Field3D {

        double at(double x, double y);
    }

    /**
     * With a +-30 mm grid of 15 points, -min / spacing evaluates to 6.999..., so truncating it picks the grid line 4.3 mm
     * off axis. Bx = y and By = x must vanish on the axis.
     */
    @Test
    public void test3DFieldIsSampledOnAxis() throws IOException {
        write3D("quad.bsx", 15, 0.03, (x, y) -> y);
        write3D("quad.bsy", 15, 0.03, (x, y) -> x);
        write3D("quad.bsz", 15, 0.03, (x, y) -> 0.0);

        FieldMapPoint point = new MagFieldMap3D(path(), "quad", 0).getFieldAt(0.5);

        assertEquals(0.0, point.getBx(), TOLERANCE);
        assertEquals(0.0, point.getBy(), TOLERANCE);
        assertEquals(1.0, point.getdBydx(), 1e-9);
        assertEquals(1.0, point.getdBxdy(), 1e-9);
    }

    /**
     * Saving a 3D map writes the TraceWin header order (x before y), so that a map with different x and y grids reloads
     * unchanged.
     */
    @Test
    public void test3DSaveAndReload() throws Exception {
        write3D("asym.bsx", 5, 0.02, 7, 0.03, (x, y) -> y);
        write3D("asym.bsy", 5, 0.02, 7, 0.03, (x, y) -> 2 * x);
        write3D("asym.bsz", 5, 0.02, 7, 0.03, (x, y) -> 1.0);

        File saved = folder.newFolder("saved");
        String savedPath = saved.toURI().toString();
        new MagFieldMap3D(path(), "asym", 0).saveFieldMap(savedPath, "asym");

        List<String> header = Files.readAllLines(new File(saved, "asym.bsx").toPath()).subList(0, 3);
        assertEquals("4 -0.020000 0.020000", header.get(1));
        assertEquals("6 -0.030000 0.030000", header.get(2));

        FieldMapPoint point = new MagFieldMap3D(savedPath, "asym", 0).getFieldAt(0.5);
        assertEquals(1.0, point.getdBxdy(), 1e-6);
        assertEquals(2.0, point.getdBydx(), 1e-6);
        assertEquals(1.0, point.getBz(), 1e-6);
    }

    /**
     * The derivative at the first and last points is a one-sided difference, also when the field does not vanish at
     * the edges.
     */
    @Test
    public void test1DEdgeDerivatives() throws IOException {
        write("cav.edz", "3 0.3\n1.0\n0.0\n1.0\n2.0\n3.0\n");
        RfFieldMap1D fieldMap = new RfFieldMap1D(path(), "cav", 0);

        // The field is normalized by sum(|f|) * length / n.
        double integral = 6.0 * 0.3 / 4;
        double spacing = 0.1;

        assertEquals((1.0 - 0.0) / integral / spacing, fieldMap.getFieldAt(0.0).getdEzdz(), 1e-9);
        assertEquals((3.0 - 2.0) / integral / spacing, fieldMap.getFieldAt(0.3).getdEzdz(), 1e-9);
    }

    @Test
    public void testMissingFileNamesTheFile() {
        try {
            new RfFieldMap1D(path(), "missing", 0);
            fail("Expected a FieldMapException");
        } catch (FieldMapException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("missing.edz"));
        }
    }

    @Test
    public void testTruncatedFileIsRejected() throws IOException {
        write("short.edz", "3 0.3\n1.0\n0.0\n1.0\n");
        try {
            new RfFieldMap1D(path(), "short", 0);
            fail("Expected a FieldMapException");
        } catch (FieldMapException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("short.edz"));
        }
    }

    @Test
    public void testWhitespaceIsFlexible() throws IOException {
        write("multi.edz", "  3\t 0.3\n1.0\n\n0.0 1.0\n2.0\t3.0\n");
        write("single.edz", "3 0.3\n1.0\n0.0\n1.0\n2.0\n3.0\n");

        FieldMapPoint multi = new RfFieldMap1D(path(), "multi", 0).getFieldAt(0.15);
        FieldMapPoint single = new RfFieldMap1D(path(), "single", 0).getFieldAt(0.15);

        assertEquals(single.getEz(), multi.getEz(), 0.0);
    }

    @Test
    public void testFactoryKeysIncludeFieldType() throws IOException {
        write("map.edz", "3 0.3\n1.0\n0.0\n1.0\n2.0\n3.0\n");
        write3D("map.bsx", 3, 0.01, (x, y) -> 0.0);
        write3D("map.bsy", 3, 0.01, (x, y) -> 0.0);
        write3D("map.bsz", 3, 0.01, (x, y) -> 1.0);

        FieldMap rf = FieldMapFactory.getInstance(path(), "map", true, FieldType.ELECTRIC, 1, 0);
        FieldMap magnet = FieldMapFactory.getInstance(path(), "map", false, FieldType.MAGNETIC, 3, 0);

        assertTrue(rf instanceof RfFieldMap1D);
        assertTrue(magnet instanceof MagFieldMap3D);
        assertSame(rf, FieldMapFactory.getInstance(path(), "map", true, FieldType.ELECTRIC, 1, 0));
        assertNotSame(rf, magnet);
    }

    @Test
    public void testFactoryLoadsOnceUnderConcurrency() throws Exception {
        write("shared.edz", "3 0.3\n1.0\n0.0\n1.0\n2.0\n3.0\n");

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<FieldMap>> tasks = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                tasks.add(() -> FieldMapFactory.getInstance(path(), "shared", true, FieldType.ELECTRIC, 1, 0));
            }
            List<Future<FieldMap>> results = executor.invokeAll(tasks);
            FieldMap first = results.get(0).get();
            for (Future<FieldMap> result : results) {
                assertSame(first, result.get());
            }
        } finally {
            executor.shutdown();
        }
    }
}
