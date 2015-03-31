package se.lu.esss.ics.jels.model.elem.jels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.model.IProbe;
import xal.tools.beam.PhaseMatrix;

/**
 * Taking care of loading and interpolating transfer matrices from a file.
 * 
 * <ul>
 * <li> loader = TransferMapLoader.getInstance(URI tmFile) returns an instance while the file is being loaded
 * <li> el_tm = loader.prepare(double position, double length) prepares TransferMaps class for an element (lazily)
 * <li> el_tm.transferMap(IProbe p, double l) on first call loads all the elements
 *    It returns interpolated transfer map for specific range
 * </ul>
 *    
 * @author Ivo List <ivo.list@cosylab.com>
 */
public class TransferMapLoader {
	private static Map<URI, TransferMapLoader> loaders = new HashMap<>();
	
	
	private URI tmFile;
	protected List<TransferMaps> tms = new ArrayList<>();
	private double[] Es;
	private double[] E;
	private boolean loaded = false;
	
	/**
	 * Class models transfer matrices for one element.
	 *
	 */
	public class TransferMaps implements Comparable<TransferMaps>
	{
		private double[] positions;
		private PhaseMatrix[] transferMaps;
		private double position, length;
		
		public TransferMaps(double position, double length) {
			this.position = position;
			this.length = length;
		}
		
		public TransferMaps(double[] positions, PhaseMatrix[] transferMaps) {
			this.positions = positions;
			this.transferMaps = transferMaps;
		}

		public PhaseMatrix transferMap(IProbe p, double l) {
			if (!loaded) lazyLoader();
			
			int i0=-1, in=-1;
			double s0 = p.getPosition();
			double s1 = s0+l;
			for (int i = 0; i<positions.length; i++)
			{
				if (s0<=positions[i] && i0 == -1) i0 = i;
				if (positions[i]>s1) { 
					in = i; 
					break;
				}
			}
			if (i0 < 1) i0 = 1;
			if (in == -1) in = transferMaps.length-1;
			PhaseMatrix m0 = null,mn = null;
			m0 = interpolate(s0,i0-1,i0);
			mn = interpolate(s0+l,in-1,in);
			return TW2OX(mn.times(m0.inverse()),p,l);
		}
		

		public double energyGain(IProbe p, double l) {
			return getEnergy(p.getPosition()+l)-getEnergy(p.getPosition());
		}

		private PhaseMatrix TW2OX(PhaseMatrix r, IProbe p, double l) {
			double gamma_start = getEnergy(p.getPosition())/p.getSpeciesRestEnergy()+1;
			double gamma_end = getEnergy(p.getPosition()+l)/p.getSpeciesRestEnergy()+1;
			for (int i=0; i<6; i++) {
				r.setElem(i, 4, r.getElem(i,4)/gamma_start);
				r.setElem(i, 5, r.getElem(i,5)*gamma_start);
				r.setElem(4, i, r.getElem(4,i)*gamma_end);
				r.setElem(5, i, r.getElem(5,i)/gamma_end);			
			}	
			return r;
		}

		private PhaseMatrix interpolate(double s, int i0, int in) {
			double p0 = positions[i0];
			double pn = positions[in];
			PhaseMatrix m1 = transferMaps[i0];
			PhaseMatrix mn = transferMaps[in];
			PhaseMatrix m = new PhaseMatrix();
			for (int i=0; i<6; i++)
				for (int j=0; j<6; j++)
					m.setElem(i, j, (m1.getElem(i, j)*(pn-s) + mn.getElem(i, j)*(s-p0)) / (pn-p0));
			return transferMaps[in];
		}

		@Override
		public int compareTo(TransferMaps arg0) {
			return Double.compare(position, arg0.position);
		}
		
	}

	/**
	 * Prepares TransferMaps class. It's not loaded yet.
	 * @param position Start of the element
	 * @param length Length of the element
	 * @return TransferMaps file for the element
	 */
	public TransferMaps prepare(double position, double length) {
		if (loaded)
			throw new Error("Cannot prepare a matrix after thez are already loaded!");
		TransferMaps maps = new TransferMaps(position, length);
		tms.add(maps);
		return maps;
	}
	
	/**
	 * Returns interpolated energy (eV) at position s
	 * @param s the position
	 * @return the enery
	 */
	public double getEnergy(double s)
	{
		if (!loaded) lazyLoader();
		int i = Arrays.binarySearch(Es, s);
		if (i<0) i= -(i+1);
		return 1.e6 * (E[i-1]*(s-Es[i-1]) + E[i]*(Es[i]-s)) / (Es[i]-Es[i-1]);
	}
	
	/**
	 * loads all the matrices for TransferMap classes that were previously prepared
	 * loads the energies
	 */
	public void lazyLoader() {
		// remove ourselves from the list of loaders - to free the reference and 
		//   that the same file may be loaded more than once
		loaders.remove(tmFile);
		loaded = true;
		
		Collections.sort(tms);
		
		List<Double> positions = new ArrayList<>();
		List<PhaseMatrix> transferMaps = new ArrayList<>();
		
		int currentTmIndex = 0;
		TransferMaps currentTm = tms.get(currentTmIndex);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(tmFile)));
			br.readLine();
			String line;
			double pos = 0.;
			String[] data = "0 1 0 0 1 1 0 0 1 1 0 0 1".split(" ");
			while ((line = br.readLine()) != null) {
				String[] nextdata = line.split(" ");
				double nextpos = Double.parseDouble(nextdata[0]);
				
				if (nextpos>=currentTm.position)
				{
					positions.add(pos);
					transferMaps.add(extractMatrix(data));
				}
				if (pos > (currentTm.position + currentTm.length)) {
					currentTm.transferMaps = transferMaps.toArray(new PhaseMatrix[0]);
					double[] positionsarray = new double[positions.size()];
					for (int i=0; i<positionsarray.length; i++) positionsarray[i] = positions.get(i);
					currentTm.positions = positionsarray;
					
					currentTmIndex++;
					if (currentTmIndex >= tms.size()) break;
					currentTm = tms.get(currentTmIndex);
		
					positions = new ArrayList<>();
					transferMaps = new ArrayList<>();
				}
				data = nextdata;
				pos = nextpos;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		

		try {
			URI file = tmFile.resolve("e.txt");
		
			int n = countLines(file)-1;
					
			Es = new double[n];
			E = new double[n];
			
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			br.readLine();
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(" ");
				Es[i] = Double.parseDouble(data[0]);
				E[i] = Double.parseDouble(data[1]);
				i++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A helper procedure to count the lines in a file.
	 * @param file the file
	 * @return number of lines
	 * @throws IOException
	 */
	private static int countLines(URI file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		int i = 0;
		while (br.readLine()!=null) i++;;
		br.close();
		return i;
	}
	
	/**
	 * Parses a single matrix from an array of string containing T_xx', T_yy' and T_zz' components
	 * @param data string matrix data
	 * @return the transfer matrix
	 */
	private static PhaseMatrix extractMatrix(String[] data) {
		double[][] m = new double[7][7];
		m[0][0] = Double.parseDouble(data[1]);
		m[0][1] = Double.parseDouble(data[2]);
		m[1][0] = Double.parseDouble(data[3]);
		m[1][1] = Double.parseDouble(data[4]);
		m[2][2] = Double.parseDouble(data[5]);
		m[2][3] = Double.parseDouble(data[6]);
		m[3][2] = Double.parseDouble(data[7]);
		m[3][3] = Double.parseDouble(data[8]);
		m[4][4] = Double.parseDouble(data[9]);
		m[4][5] = Double.parseDouble(data[10]);
		m[5][4] = Double.parseDouble(data[11]);
		m[5][5] = Double.parseDouble(data[12]);
		m[6][6] = 1.;
		return new PhaseMatrix(m);
	}

	/**
	 * Creates the loader for specific file
	 * @param tmFile the file
	 */
	public TransferMapLoader(URI tmFile) {
		this.tmFile = tmFile;
	}
	
	/**
	 * Gets an instance for a specific file
	 * 
	 * @param tmFile path to the file
	 * @return transfer map loader
	 */
	public static TransferMapLoader getInstance(URI tmFile) {
		if (loaders.containsKey(tmFile)) return loaders.get(tmFile);
		TransferMapLoader loader = new TransferMapLoader(tmFile);
		loaders.put(tmFile, loader);
		return loader;
	}
	
	
	/**
	 * A test program, that reads the file and outputs transfer matrices.
	 * @param args file name
	 */
	public static void main(String args[])
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			br.readLine();
			String line;
			double pos = 0.;
			PhaseMatrix data = PhaseMatrix.identity();
			while ((line = br.readLine()) != null) {
				String[] nextdata = line.split(" ");
				double nextpos = Double.parseDouble(nextdata[0]);
				
				PhaseMatrix nextmatrix = extractMatrix(nextdata);

				PhaseMatrix t =	nextmatrix.times(data.inverse());
				System.out.printf("%E ", pos);
				for (int j=0; j<6; j++)
					for (int k=0; k<6; k++)
						System.out.printf("%E ", t.getElem(j, k));
				System.out.println();


				data = nextmatrix;
				pos = nextpos;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
