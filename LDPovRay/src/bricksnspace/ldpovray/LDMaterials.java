/**
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import bricksnspace.ldrawlib.LDrawColor;

/**
 * @author Mario Pascucci
 * 
 * Generates a PovRay include from LDraw color list.
 *
 */ 
public class LDMaterials {

	private static String VERSION = "// LDraw to POVRay color and materials (autogenetated) v20170438\n\n";
	
//	private static final String FINISH = 
//			"\n\n// normal ABS shiny plastic\n"
//			+ "#declare "+LDrawColorType.SOLID.name()+" = finish {\n"
//			+ "  specular 0.5\n"
//			+ "  roughness 0.001\n"
//			+ "  reflection 0.2 \n"
//			+ "  ambient 0\n"
//			+ "  diffuse 0.6\n"
//			+ "  conserve_energy\n"
//			+ "}\n\n"
//			+ "// transparent polycarbonate parts\n"
//			+ "#declare "+LDrawColorType.TRANSPARENT.name()+" = finish{\n"
//			+ "  ambient 0\n"
//			+ "  diffuse 0.4\n"
//			+ "  reflection 0.3\n"
//			+ "  phong 0.3\n"
//			+ "  phong_size 60\n"
//			+ "}\n\n"
//			+ "// chrome metal\n"
//			+ "#declare "+LDrawColorType.METAL.name()+" = finish {\n"
//			+ "  ambient 0.3\n"
//			+ "  diffuse 0.7\n"
//			+ "  reflection 0.15\n"
//			+ "  brilliance 8\n"
//			+ "  specular 0.8\n"
//			+ "  roughness 0.1\n"
//			+ "}\n\n"
//			+ "// metallic\n"
//			+ "#declare "+LDrawColorType.CHROME.name()+" = finish {\n"
//			+ "  metallic\n"
//			+ "  ambient 0.2\n"
//			+ "  diffuse 0.7\n"
//			+ "  brilliance 6\n"
//			+ "  reflection 0.25\n"
//			+ "  phong 0.75\n"
//			+ "  phong_size 80\n"
//			+ "}\n\n"
//			+ "// pearl metal\n"
//			+ "#declare "+LDrawColorType.PEARL.name()+" = finish {\n"
//			+ "  ambient 0.35\n"
//			+ "  diffuse 1.0\n"
//			+ "  brilliance 15\n"
//			+ "  phong 0.41\n"
//			+ "  phong_size 5\n"
//			+ "}\n\n"
//			+ "// rubber\n"
//			+ "#declare "+LDrawColorType.RUBBER.name()+" = finish {\n"
//			+ "  specular 0.1\n"
//			+ "  roughness 0.5\n"
//			+ "}\n\n"
//			+ "#declare INTERIOR_TRANS = interior { ior 1.1 }\n\n\n"; 





	
	private LDMaterials() {
		// unused constructor
	}
	
	static public void generateMaterials() {

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File("jbbmaterials.inc")),"UTF-8"));
			bw.write(VERSION);
			//bw.write(FINISH);
			List<Integer> l = new ArrayList<Integer>(LDrawColor.getAllColors());
			Collections.sort(l);
			for (int i:l) {
				LDrawColor c = LDrawColor.getById(i);
				bw.write(String.format(Locale.US,"// LDraw %s color ID %d\n",c.getType().toString(),i));
				switch (c.getType()) {
				case GLITTER:
					break;
				case MILKY:
					break;
				case RUBBER:
					if (c.getColor().getAlpha() == 255) {
						bw.write(String.format(Locale.US,
								"#declare %s=material{texture {pigment { rgb <%f,%f,%f>} finish {RUBBER} }}\n\n",
						c.getName(), 
						c.getColor().getRed()/255.0f,
						c.getColor().getGreen()/255.0f,
						c.getColor().getBlue()/255.0f
						));
					}
					else {
						bw.write(String.format(Locale.US,
								"#declare %s=material{texture {pigment { rgbf <%f,%f,%f,%f>} finish {RUBBER}} }\n\n",
						c.getName(), 
						c.getColor().getRed()/255.0f,
						c.getColor().getGreen()/255.0f,
						c.getColor().getBlue()/255.0f,
						1.0f-c.getColor().getAlpha()/255.0f
						));
					}
					break;
				case CHROME:
				case METAL:
				case PEARL:
				case SOLID:
					bw.write(String.format(Locale.US,
							"#declare %s=material{texture {pigment { rgb <%f,%f,%f>} finish {%s} }}\n\n",
					c.getName(), 
					c.getColor().getRed()/255.0f,
					c.getColor().getGreen()/255.0f,
					c.getColor().getBlue()/255.0f,
					c.getType().name()
					));
					break;
				case TRANSPARENT:
					float f = 0.9f;
					float delta = 1f; 
					double lum = (c.getColor().getRed()*0.21+c.getColor().getGreen()*0.71+c.getColor().getBlue()*0.07)/255.0;
					//double lum = Math.max(c.getColor().getRed()/255.0, Math.max(c.getColor().getGreen()/255.0,c.getColor().getBlue()/255.0));
					//System.out.println("C:"+c.getName()+" lum:"+lum);
					if (lum < 0.2) {
						f = 1.0f;
						delta = 1.4f;
					}
					else if (lum < 0.4) {
						f = 1.0f;
						delta = 1.2f;						
					}
					else if (lum > 0.8) {
						f = 0.8f;
						delta = 1f;
					}
					bw.write(String.format(Locale.US,
							"#declare %s=material{texture {pigment { rgbf <%f,%f,%f,%f>} finish {TRANSPARENT} } interior {INTERIOR_TRANS}}\n\n",
					c.getName(), 
					c.getColor().getRed()/255.0f*delta,
					c.getColor().getGreen()/255.0f*delta,
					c.getColor().getBlue()/255.0f*delta,
					f
					));
					break;
				case INTERNAL:
				case USERDEF:
				default:
					if (c.getColor().getAlpha() == 255) {
						bw.write(String.format(Locale.US,
								"#declare %s=material{texture {pigment { rgb <%f,%f,%f>} finish {SOLID} }}\n\n",
						c.getName(), 
						c.getColor().getRed()/255.0f,
						c.getColor().getGreen()/255.0f,
						c.getColor().getBlue()/255.0f
						));
					}
					else {
						bw.write(String.format(Locale.US,
								"#declare %s=material{texture {pigment { rgbf <%f,%f,%f,%f>} finish {TRANSPARENT}} interior {INTERIOR_TRANS}}\n\n",
						c.getName(), 
						c.getColor().getRed()/255.0f,
						c.getColor().getGreen()/255.0f,
						c.getColor().getBlue()/255.0f,
						1.0f-c.getColor().getAlpha()/255.0f
						));
					}
					break;
				
				}
//				if (c.getColor().getAlpha() == 255) {
//				    bw.write(String.format(Locale.US,
//				    	"// LDraw color ID %d\n#declare %s=material{texture {pigment { rgb <%f,%f,%f>} finish {BrickPlastic} }}\n\n",
//				    	i,
//						c.getName(), 
//						c.getColor().getRed()/255.0f,
//						c.getColor().getGreen()/255.0f,
//						c.getColor().getBlue()/255.0f
//						));
//				}
//				else {
//					bw.write(String.format(Locale.US,
//				    	"// LDraw color ID %d\n#declare %s=material{texture {pigment { rgbf <%f,%f,%f,%f>} finish {BrickPlastic} }}\n\n",
//						i,
//				    	c.getName(), 
//						c.getColor().getRed()/255.0f,
//						c.getColor().getGreen()/255.0f,
//						c.getColor().getBlue()/255.0f,
//						1.0f-c.getColor().getAlpha()/255.0f
//						));
//				}
			}
			bw.close();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
