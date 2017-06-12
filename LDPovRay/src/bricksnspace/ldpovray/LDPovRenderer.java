/**
	Copyright 2017 Mario Pascucci <mpascucci@gmail.com>
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import bricksnspace.j3dgeom.Matrix3D;
import bricksnspace.ldrawlib.LDPrimitive;
import bricksnspace.ldrawlib.LDrawCommand;

/**
 * 
 * A renderer for Povray (generate a .pov file)
 * 
 * @author Mario Pascucci
 *
 */
public class LDPovRenderer {

	private final static String POVHEADER = "jbbheader.inc";
	private final static String POVPRIMITIVES = "jbbprimitives.inc";
	private final static String POVFONT = "lego-font.ttf";
	
	BufferedWriter buffWriter;
	boolean perspective = true;
	float zoomFactor = 1;
	Matrix3D viewMatrix = new Matrix3D();
	//String lightSource = "light_source { <-5000, 9000, 10000> color rgb <0.8,0.8,0.8>  area_light <500, 0, 0>, <0, 0, 500>, 3, 3 adaptive 1 jitter }"; 
	
	private LDPovRenderer() {} ;
	
		
	
	public static LDPovRenderer getRenderer(File path) throws IOException {
		
		LDPovRenderer r = new LDPovRenderer();
		r.buffWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(path),"UTF-8"));
		File outputDir = path.getParentFile();
		r.generateHeaders(outputDir);
		LDPOVRenderedPart.resetPrimitives();
		return r; 
	}
	
	
	
	public void generateHeaders(File path) throws IOException {
		
		Files.copy(this.getClass().getResourceAsStream("data/"+POVHEADER),Paths.get(path.getCanonicalPath(),POVHEADER) ,StandardCopyOption.REPLACE_EXISTING);
		Files.copy(this.getClass().getResourceAsStream("data/"+POVPRIMITIVES),Paths.get(path.getCanonicalPath(),POVPRIMITIVES) ,StandardCopyOption.REPLACE_EXISTING);
		Files.copy(this.getClass().getResourceAsStream("data/"+POVFONT),Paths.get(path.getCanonicalPath(),POVFONT) ,StandardCopyOption.REPLACE_EXISTING);
		LDMaterials.generateMaterials(path);
	}
	
	
	
	public void setPerspective(float zoomFactor) throws IOException {
		
		perspective = true;
		this.zoomFactor = zoomFactor; 
	}
	
	
	public void setViewMatrix(Matrix3D m) {
		
		viewMatrix = m.getCopy();
	}
	
	

	public void addModel(List<LDPrimitive> l) throws IOException {
		
		for (LDPrimitive p:l){
			float x = (float) (Math.random()*0.6 - 0.3);
			float y = (float) (Math.random()*0.6 - 0.3);
			float z = (float) (Math.random()*0.6 - 0.3);
			if (p.getType() == LDrawCommand.REFERENCE) {
				LDPOVRenderedPart.newRenderedPart(p.moveTo(x, y, z), buffWriter,viewMatrix);
			}
		}
		buffWriter.flush();
		buffWriter.close();
	}
	
	
	
	public void startRender() throws IOException {
		
		buffWriter.write("#version 3.7;\n#include \"jbbheader.inc\"\n");
		buffWriter.write('\n');
		if (perspective) {
			buffWriter.write(
					"camera {\n" +
					"  perspective\n" +
					"  location <0,0,"+800.0*zoomFactor + ">\n" +
					"  look_at <0,0,0>\n" +
					"  up <0,1,0>\n" +
					"}\n\n");			
		}
	}
	
}
