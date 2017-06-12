/*
	Copyright 2015-2017 Mario Pascucci <mpascucci@gmail.com>
	This file is part of LDPovRay

	LDPovRay is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	LDPovRay is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with LDPovRay.  If not, see <http://www.gnu.org/licenses/>.

*/


package bricksnspace.ldpovray;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bricksnspace.j3dgeom.Matrix3D;
import bricksnspace.ldrawlib.LDPrimitive;
import bricksnspace.ldrawlib.LDrawColor;
import bricksnspace.ldrawlib.LDrawPart;


/**
 * @author Mario Pascucci
 *
 */


/* 
 * a single LDraw part, identified from its part code, colored and placed in space 
 * with VBOs already full of triangles and lines.
 * VBOs:
 *  - triangles vertex with normals (floats)
 *  - color per vertex (bytes)
 *  - lines vertex (no normals, floats)
 *  - color per vertex (bytes)
 *  - aux lines vertex (no normals, floats)
 *  - color per vertex (bytes)
 *  Special VBO for bounding box
 *  - bb lines vertex (floats, no color, no normals)
 *  
 */
public class LDPOVRenderedPart {
	
	// Primitives in substitution file PRIMSUBST
	private static Set<String> substPrimitives = new TreeSet<String>(); 
	private static String PRIMSUBST = "jbbprimitives.inc";
	
	private static BufferedWriter povFile;
	
	// Special primitives, program generated when used 
	private static Set<String> genPrimitives = new TreeSet<String>(); 
	
	private static Pattern ringConePattern = Pattern.compile(
			"([0-9]+)"  // segment 
			+ "-"
			+ "([0-9]+)"  // total segments
			+ "([a-z]+)" // type
			+ "([0-9]+)"  // diameter/radius
			+ "[.]dat",
			Pattern.CASE_INSENSITIVE);
	
	private static Pattern cylPattern = Pattern.compile(
			"([0-9]+)"  // segment 
			+ "-"
			+ "([0-9]+)"  // total segments
			+ "(cyl[io])2?[.]dat",
			Pattern.CASE_INSENSITIVE);
	
	private static Pattern discPattern = Pattern.compile(
			"([0-9]+)"  // segment 
			+ "-"
			+ "([0-9]+)"  // total segments
			+ "(disc)[.]dat",
			Pattern.CASE_INSENSITIVE);
	
	private static Pattern torusPattern = Pattern.compile(
			"t([0-9]{2})"  // fraction 1/n 
			+ "([ioq])"			// inside, outside, complete
			+ "([0-9]{4})"  // minor radius 0.xxxx
			+ "[.]dat",
			Pattern.CASE_INSENSITIVE);
	
	private static boolean singular;

	
	static {
		InputStream is;
		try {
			is = new FileInputStream(PRIMSUBST);
			initPrimitives(is); 
		} catch (IOException e) {
			// ignored if no file
		}
	};

	
	/**
	 * Reads in primitives from external definition file
	 * @param a file to read
	 * @throws IOException
	 */
	private static void initPrimitives(InputStream a) throws IOException {
		
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(a));
		String s;
		substPrimitives.clear();
		while ((s = lnr.readLine()) != null) {
			String[] l = s.trim().split("\\s+");
			if (l.length < 3)
				continue;
			if (!l[0].equals("//"))
				continue;
			if (!l[1].equals("#JBB"))
				continue;
			substPrimitives.add(l[2]);
		}
	}
	
	
	
	private static void addGeneratedCylinder(String p, int fraction, int total) throws IOException {
		
		String declare = "LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_');
		if (total/fraction >= 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write("  cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }\n");
			povFile.write(String.format(Locale.US,"  clipped_by {\n    plane { -z, 0 }\n    plane { <%f,0,%f>, 0 }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else if (total == fraction) {
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write("  cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }\n}\n\n");
			genPrimitives.add(p);
		}
		else if (total/fraction < 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=union{\n  object {\n",declare));
			povFile.write("    cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }\n");
			povFile.write("    clipped_by {\n    plane { -z, 0 }\n    }\n  }\n");
			povFile.write("  object {\n    cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }\n");
			povFile.write(String.format(Locale.US,"    clipped_by { plane { z, 0 } plane { <%f,0,%f>, 0 } }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else {
			Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
		}
			
	}
	
	
	
	private static void addGeneratedDisc(String p, int fraction, int total) throws IOException {
		
		String declare = "LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_');
		if (total/fraction >= 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write("  disc { <0, 0, 0>, <0, 1, 0>, 1 }\n");
			povFile.write(String.format(Locale.US,"  clipped_by {\n    plane { -z, 0 }\n    plane { <%f,0,%f>, 0 }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else if (total == fraction) {
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write("  disc { <0, 0, 0>, <0, 1, 0>, 1 }\n}\n\n");
			genPrimitives.add(p);
		}
		else if (total/fraction < 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=union{\n  object {\n",declare));
			povFile.write("    disc { <0, 0, 0>, <0, 1, 0>, 1 }\n");
			povFile.write("    clipped_by {\n    plane { -z, 0 }\n    }\n  }\n");
			povFile.write("  object {\n    disc { <0, 0, 0>, <0, 1, 0>, 1 }\n");
			povFile.write(String.format(Locale.US,"    clipped_by { plane { z, 0 } plane { <%f,0,%f>, 0 } }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else {
			Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
		}
			
	}
	
	
	
	private static void addGeneratedTorus(String p, String type, int fraction, float rMinor) throws IOException {
		
		String declare = "LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_');
		if (fraction >= 2) {
			double angle = (Math.PI * 2 / fraction) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write(String.format(Locale.US,"  torus { 1, %f hollow }\n",rMinor));
			if (type.equalsIgnoreCase("i")) {
				povFile.write(String.format(Locale.US, "  clipped_by { plane { -y,0 } plane { <%f,0,%f>,0 } plane { -z,0} cylinder { <0,-1,0>,<0,1,0>, 1 } }\n}\n\n", vx,vz ));
			}
			else if (type.equalsIgnoreCase("o")) {
				povFile.write(String.format(Locale.US, "  clipped_by { plane { -y,0 } plane { <%f,0,%f>,0 } plane { -z,0} cylinder { <0,-1,0>,<0,1,0>, 1 inverse } }\n}\n\n", vx,vz ));
			}
			else if (type.equalsIgnoreCase("q")) {
				povFile.write(String.format(Locale.US, "  clipped_by { plane { <%f,0,%f>,0 } plane { -z,0} }\n}\n\n", vx,vz ));
			}
			else {
				Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
				return;
			}
			genPrimitives.add(p);
		}
		else if (fraction == 1) {
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write(String.format(Locale.US,"    torus { 1, %f hollow }\n",rMinor));
			if (type.equalsIgnoreCase("i")) {
				povFile.write("  clipped_by { plane { -y,0 } cylinder { <0,-1,0>,<0,1,0>, 1 } }\n}\n\n");
			}
			else if (type.equalsIgnoreCase("o")) {
				povFile.write("  clipped_by { plane { -y,0 } cylinder { <0,-1,0>,<0,1,0>, 1 inverse } }\n}\n\n");
			}
			else if (type.equalsIgnoreCase("q")) {
				povFile.write("}\n\n");
			}
			else {
				Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
				return;
			}
			genPrimitives.add(p);
		}
		else {
			Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
		}
			
	}
	
	
	
	private static void addGeneratedRing(String p, int internalRadius, int fraction, int total) throws IOException {
		
		String declare = "LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_');
		if (total/fraction >= 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write(String.format(Locale.US,"  disc { <0, 0, 0>, <0, 1, 0>, %d, %d }\n",internalRadius+1,internalRadius));
			povFile.write(String.format(Locale.US,"  clipped_by {\n    plane { -z, 0 }\n    plane { <%f,0,%f>, 0 }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else if (total == fraction) {
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write(String.format(Locale.US,"  disc { <0, 0, 0>, <0, 1, 0>, %d, %d }\n}\n\n",internalRadius+1,internalRadius));
			genPrimitives.add(p);
		}
		else if (total/fraction < 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=union{\n  object {\n",declare));
			povFile.write(String.format(Locale.US,"    disc { <0, 0, 0>, <0, 1, 0>, %d, %d }\n",internalRadius+1,internalRadius));
			povFile.write("    clipped_by {\n    plane { -z, 0 }\n    }\n  }\n");
			povFile.write(String.format(Locale.US,"  object {\n    disc { <0, 0, 0>, <0, 1, 0>, %d, %d }\n",internalRadius+1,internalRadius));
			povFile.write(String.format(Locale.US,"    clipped_by { plane { z, 0 } plane { <%f,0,%f>, 0 } }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else {
			Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
		}
			
	}
	
	
	
	private static void addGeneratedCone(String p, int topRadius, int fraction, int total) throws IOException {
		
		String declare = "LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_');
		if (total/fraction >= 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write(String.format(Locale.US,"  cone { <0, 0, 0>, %d, <0, 1, 0>, %d open }\n",topRadius+1,topRadius));
			povFile.write(String.format(Locale.US,"  clipped_by {\n    plane { -z, 0 }\n    plane { <%f,0,%f>, 0 }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else if (total == fraction) {
			povFile.write(String.format(Locale.US,"#declare %s=object {\n",declare));
			povFile.write(String.format(Locale.US,"  cone { <0, 0, 0>, %d, <0, 1, 0>, %d open }\n}\n\n",topRadius+1,topRadius));
			genPrimitives.add(p);
		}
		else if (total/fraction < 2) {
			double angle = (Math.PI * 2 * fraction / total) + (Math.PI / 2);
			double vx = Math.cos(angle);
			double vz = Math.sin(angle);
			povFile.write(String.format(Locale.US,"#declare %s=union{\n  object {\n",declare));
			povFile.write(String.format(Locale.US,"    cone { <0, 0, 0>, %d, <0, 1, 0>, %d open }\n",topRadius+1,topRadius));
			povFile.write("    clipped_by {\n    plane { -z, 0 }\n    }\n  }\n");
			povFile.write(String.format(Locale.US,"  object {\n    cone { <0, 0, 0>, %d, <0, 1, 0>, %d open }\n",topRadius+1,topRadius));
			povFile.write(String.format(Locale.US,"    clipped_by { plane { z, 0 } plane { <%f,0,%f>, 0 } }\n  }\n}\n\n",vx,vz));
			genPrimitives.add(p);
		}
		else {
			Logger.getGlobal().log(Level.WARNING, "Undefined primitive "+p);
		}
			
	}

	
	
	
	private LDPOVRenderedPart() {
		
//		pp = p;
//		generatePartVBOs();
	}
	
	
	
	/**
	 * nicely formats a float for LDraw files standard
	 * - no trailing decimal zeroes
	 * - max 5 decimals 
	 * @param n float to format
	 * @return
	 */
	private static String fF(float n) {
		
		String s = String.format(Locale.US, " %.5f", n);
		if (s.equals(" -0.00000")) return " 0";
		if (s.endsWith(".00000")) return s.substring(0, s.length()-6);
		if (s.endsWith("0000")) return s.substring(0, s.length()-4);
		if (s.endsWith("000")) return s.substring(0, s.length()-3);
		if (s.endsWith("00")) return s.substring(0, s.length()-2);
		if (s.endsWith("0")) return s.substring(0, s.length()-1);
		return s;
	}


	
	
	private static void addTriangle(float[] point1, float[] point2, float[] point3, int ldrcolor) throws IOException
	{
		// write a triangle primitive with LDraw material
		String material = LDrawColor.getById(ldrcolor).getName();
		povFile.write("triangle {\n");
		povFile.write(String.format(Locale.US, "  <%s,%s,%s>, <%s,%s,%s>, <%s,%s,%s>\n", 
				fF(point1[0]),fF(point1[1]),fF(point1[2]),
				fF(point2[0]),fF(point2[1]),fF(point2[2]),
				fF(point3[0]),fF(point3[1]),fF(point3[2])));
		povFile.write(String.format(Locale.US, "  material { %s }\n}\n\n", material));
	}


	
	private static void addPrimitive(String name, Matrix3D t, int ldrcolor) throws IOException 
	{
		String material = LDrawColor.getById(ldrcolor).getName();
		povFile.write(String.format("object { %s\n",name));
		povFile.write(String.format(Locale.US, "  matrix <%s,%s,%s, %s,%s,%s, %s,%s,%s, %s,%s,%s>\n", 
				fF(t.getA()),fF(t.getD()),fF(t.getG()),
				fF(t.getB()),fF(t.getE()),fF(t.getH()),
				fF(t.getC()),fF(t.getF()),fF(t.getI()),
				fF(t.getX()),fF(t.getY()),fF(t.getZ())));
		povFile.write(String.format(Locale.US, "  material { %s }\n}\n\n", material));
	}
	
	
//	private static void addLine(float[] point1, float[] point2, int ldrcolor) throws IOException 
//	{
//		// write a line primitive as a cylinder with LDraw material
//		String material = LDrawColor.getById(ldrcolor).getName();
//		povFile.write("cylinder {\n");
//		povFile.write(String.format(Locale.US, "  <%s,%s,%s>, <%s,%s,%s>, 0.1\n", 
//				fF(point1[0]),fF(point1[1]),fF(point1[2]),
//				fF(point2[0]),fF(point2[1]),fF(point2[2])));
//		povFile.write(String.format(Locale.US, "  material { %s }\n}\n\n", "Trans_Black"));		
//	}

	
	/**
	 * A really complex function that uses OpenGL Vertex Buffer Object
	 * specification to create arrays of float for a part vertex and
	 * arrays of byte for colors
	 *
	 * Triangles with attribute array:
	 *  - coordinate (x,y,z)
	 *  - normal (x,y,z)
	 *  for every vertex
	 *  Separate color attribute byte array:
	 *  - color (r,g,b,a)
	 *  for every vertex
	 *
	 * Lines with attribute array:
	 *  - coordinate (x,y,z)
	 *  for every vertex
	 *  Separate color attribute byte array:
	 *  - color (r,g,b,a)
	 *  for every vertex
	 * @throws IOException 
	 * 
	 */ 
	private static void renderPart(Collection<LDPrimitive> pt, int color, Matrix3D m, boolean invert) throws IOException {

		float[] p1, p2, p3;
		int pc;

		//System.out.println(pt);
		for (LDPrimitive prim : pt) {
			switch (prim.getType()) {
			case TRIANGLE:
			// triangle:
				//System.out.println("Triangle-"+invert);
				if (prim.getColorIndex() == LDrawColor.CURRENT) {
					// current color
					pc = color;
				}
				else if (prim.getColorIndex() == LDrawColor.EDGE) {
					// edge color
					pc = color;
				}
				else {
					// specific color
					pc = prim.getColorIndex();
				}
				p1 = m.transformPoint(prim.getPointsFV()[0], prim.getPointsFV()[1],prim.getPointsFV()[2]);
				p2 = m.transformPoint(prim.getPointsFV()[3], prim.getPointsFV()[4],prim.getPointsFV()[5]);
				p3 = m.transformPoint(prim.getPointsFV()[6], prim.getPointsFV()[7],prim.getPointsFV()[8]);
				// place every vertex with color and normal on array

				addTriangle(p1, p2, p3, pc);
				break;
			case QUAD:
			// quad, rendered as two adjacent triangles:
				//System.out.println("Quad-"+invert);
				if (prim.getColorIndex() == LDrawColor.CURRENT) {
					// current color
					pc = color;
				}
				else if (prim.getColorIndex() == LDrawColor.EDGE) {
					// edge color
					pc = color;
				}
				else {
					// specific color
					pc = prim.getColorIndex();
				}
				p1 = m.transformPoint(prim.getPointsFV()[0], prim.getPointsFV()[1],prim.getPointsFV()[2]);
				p2 = m.transformPoint(prim.getPointsFV()[3], prim.getPointsFV()[4],prim.getPointsFV()[5]);
				p3 = m.transformPoint(prim.getPointsFV()[6], prim.getPointsFV()[7],prim.getPointsFV()[8]);
				// place every vertex with color and normal on array

				addTriangle(p1, p2, p3, pc);
				
				// place every vertex with color and normal on array
				// now vertex 0,2,3
				p1 = m.transformPoint(prim.getPointsFV()[0], prim.getPointsFV()[1],prim.getPointsFV()[2]);
				p2 = m.transformPoint(prim.getPointsFV()[6], prim.getPointsFV()[7],prim.getPointsFV()[8]);
				p3 = m.transformPoint(prim.getPointsFV()[9], prim.getPointsFV()[10],prim.getPointsFV()[11]);

				addTriangle(p1, p2, p3, pc);
				
				break;
			case REFERENCE:
			// sub-part
				if (prim.getTransformation().determinant() == 0) {
					prim = prim.setTransform(prim.getTransformation().correctSingular());
					singular = true;
				}
				int localColor = 0;
				if (prim.getColorIndex() == LDrawColor.CURRENT) {
					// current color
					localColor = color;
				}
				else if (prim.getColorIndex() == LDrawColor.EDGE) {
					// edge color is illegal in sub-part!
					Logger.getGlobal().log(Level.WARNING,"[LDPOVRenderedPart] Illegal EDGE color in sub-part:\n"+prim.toString());
					localColor = color;
				}
				else {
					// specific color
					localColor = prim.getColorIndex();
				}
				String p = prim.getLdrawId().toLowerCase();
				//System.out.println(p);
				if (p.startsWith("8\\") || p.startsWith("48\\")) {
					p = p.substring(p.indexOf('\\')+1);
				}
				if (substPrimitives.contains(p) || genPrimitives.contains(p)) {
					//System.out.println("Present = "+p);
					addPrimitive("LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_'),prim.getTransformation().transform(m), localColor);
					break;
				}
		        Matcher ringConeMatch = ringConePattern.matcher(p);
		        if (ringConeMatch.lookingAt()) {
		        	if (ringConeMatch.groupCount() == 4) {
		        		if (ringConeMatch.group(3).equalsIgnoreCase("ring") 
		        				|| ringConeMatch.group(3).equalsIgnoreCase("rin")
		        				|| ringConeMatch.group(3).equalsIgnoreCase("ri")
		        				|| ringConeMatch.group(3).equalsIgnoreCase("r")) {
		        			// it is a ring
		        			int r = Integer.parseInt(ringConeMatch.group(4));
		        			int fraction = Integer.parseInt(ringConeMatch.group(1));
		        			int total = Integer.parseInt(ringConeMatch.group(2));
	        				addGeneratedRing(p,r,fraction,total);
		        		}
		        		else if (ringConeMatch.group(3).equalsIgnoreCase("con") 
		        				|| ringConeMatch.group(3).equalsIgnoreCase("co")) {
		        			// it is a ring
		        			int r = Integer.parseInt(ringConeMatch.group(4));
		        			int fraction = Integer.parseInt(ringConeMatch.group(1));
		        			int total = Integer.parseInt(ringConeMatch.group(2));
	        				addGeneratedCone(p,r,fraction,total);
		        		}
		        	}
		        }
		        Matcher cylMatch = cylPattern.matcher(p);
		        if (cylMatch.lookingAt()) {
		        	if (cylMatch.groupCount() == 3) {
		        		if (cylMatch.group(3).equalsIgnoreCase("cyli") 
		        				|| cylMatch.group(3).equalsIgnoreCase("cylo")) {
		        			// it is a cylinder
		        			int fraction = Integer.parseInt(cylMatch.group(1));
		        			int total = Integer.parseInt(cylMatch.group(2));
	        				addGeneratedCylinder(p, fraction, total);
		        		}
		        	}
		        }
		        Matcher discMatch = discPattern.matcher(p);
		        if (discMatch.lookingAt()) {
		        	if (discMatch.groupCount() == 3) {
		        		if (discMatch.group(3).equalsIgnoreCase("disc")) {
		        			// it is a disc
		        			int fraction = Integer.parseInt(discMatch.group(1));
		        			int total = Integer.parseInt(discMatch.group(2));
	        				addGeneratedDisc(p,fraction,total);
		        		}
		        	}
		        }
		        Matcher torusMatch = torusPattern.matcher(p);
		        if (torusMatch.lookingAt()) {
		        	if (torusMatch.groupCount() == 3) {
	        			int fraction = Integer.parseInt(torusMatch.group(1));
	        			float minorRadius = Integer.parseInt(torusMatch.group(3))/10000.0f;
        				addGeneratedTorus(p,torusMatch.group(2),fraction,minorRadius);
		        		
		        	}
		        }

				if (genPrimitives.contains(p)) {
					System.out.println("Generated = "+p);
					addPrimitive("LD"+p.substring(0, p.length()-4).toUpperCase().replace('-', '_'),prim.getTransformation().transform(m), localColor);
					break;
				}
				if (prim.getTransformation().determinant() < 0) {
					renderPart(LDrawPart.getPart(prim.getLdrawId()).getPrimitives(), 
							localColor, prim.getTransformation().transform(m), prim.isInvert()^(!invert));
				}
				else {
					renderPart(LDrawPart.getPart(prim.getLdrawId()).getPrimitives(), 
							localColor, prim.getTransformation().transform(m), prim.isInvert()^invert);
				}
				break;
			case LINE:
				// it is a line, no-op
				break;
			case AUXLINE:
				// it is an aux line, so no-op
				break;
			default:
				//System.out.println("[LDRenderedPart] Unknown primitive:\n"+p.toString());
				break;
			}
		}
	}

	
	public static void resetPrimitives() {
		
		try {
			genPrimitives.clear();
			InputStream is = new FileInputStream(PRIMSUBST);
			initPrimitives(is); 
		} catch (IOException e) {
			// ignored if no file
		}

	}

	
	
	
	public static void newRenderedPart(LDPrimitive p, BufferedWriter bw, Matrix3D viewMatrix) throws IOException {
		
		singular = false;
		povFile = bw;
		povFile.write("// Part: "+p.toString());
		povFile.newLine();
		renderPart(p.getPrimitives(),p.getColorIndex(),/*pp.getTransformation()*/ viewMatrix.scale(-1, -1, -1),false);
		if (singular) 
			Logger.getGlobal().log(Level.WARNING, "Singular matrix detected: "+p.toString());
	}
	
	
	

	
}
