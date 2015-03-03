package se.lu.esss.ics.jels.smf.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class FieldProfile {
	private double length;
	private double[] field;

	private static Map<String, FieldProfile> instances = new HashMap<>();

	public FieldProfile(double length, double[] field) {
		this.length = length;
		this.field = field;
	}
	
	protected FieldProfile(String path)
	{
		try {
			loadFile(path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Static factory method to give field profile for a specific file
	 * @param path path to the field map file
	 * @return field profile
	 */
	public static FieldProfile getInstance(String path)
	{
		if (instances.containsKey(path)) {
			return instances.get(path);
		}
		FieldProfile fp = new FieldProfile(path);
		instances.put(path, fp);
		return fp;
	}
	
	
	public double[] getField()
	{
		return field;
	}
	
	public double getLength()
	{
		return length;
	}
	
	/************************** File manipulation ***************************/

	/**
	 * Loads field from a file.
	 * @param path path to the file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void loadFile(String path) throws IOException, URISyntaxException {
		BufferedReader br = new BufferedReader(new FileReader(new File(new URI(path))));

		// first line
		String line = br.readLine();
		String[] data = line.split(" ");

		int N = Integer.parseInt(data[0]);
		length = Double.parseDouble(data[1]);
		field = new double[N];

		line = br.readLine();
		@SuppressWarnings("unused")
		double norm = Double.parseDouble(line);

		int i = 0;
		while ((line = br.readLine()) != null && i<N) {
			field[i++] = Double.parseDouble(line)*1e6;
		}

		br.close();
	}

	/**
	 * Saves given field to a file 
	 * @param path path to the file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void saveFile(String path) throws IOException, URISyntaxException {
		PrintWriter pw = new PrintWriter(new FileWriter(new File(new URI(path))));
		double[] field = getField();
		double zmax = getLength();
		pw.printf("%d %E\n%E\n", field.length, zmax, 1.0);
		for (int i = 0; i<field.length; i++)
			pw.printf("%E\n", field[i]);
		pw.close();
	}
}
