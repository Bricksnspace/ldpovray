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
import java.io.IOException;
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

	
	BufferedWriter buffWriter;
	boolean perspective = true;
	float zoomFactor = 1;
	Matrix3D viewMatrix = new Matrix3D();
	//String lightSource = "light_source { <-5000, 9000, 10000> color rgb <0.8,0.8,0.8>  area_light <500, 0, 0>, <0, 0, 500>, 3, 3 adaptive 1 jitter }"; 
	
	private LDPovRenderer() {} ;
	
		
	
	public static LDPovRenderer getRenderer(BufferedWriter bw) throws IOException {
		
		LDPovRenderer r = new LDPovRenderer();
		r.buffWriter = bw;
		LDPOVRenderedPart.resetPrimitives();
		return r; 
	}
	
	
	
	
	public void setPerspective(float zoomFactor) throws IOException {
		
		perspective = true;
		this.zoomFactor = zoomFactor; 
	}
	
	
	public void setViewMatrix(Matrix3D m) {
		
		viewMatrix = m.getCopy();
	}
	
	
//	public void addPart(LDPrimitive p) throws IOException {
//		
//		LDPOVRenderedPart.newRenderedPart(p, buffWriter,viewMatrix);
//	}
	
	
	public void addModel(List<LDPrimitive> l) throws IOException {
		
		for (LDPrimitive p:l){
			float x = (float) (Math.random()*0.3 - 0.15);
			float y = (float) (Math.random()*0.3 - 0.15);
			float z = (float) (Math.random()*0.3 - 0.15);
			if (p.getType() == LDrawCommand.REFERENCE) {
				LDPOVRenderedPart.newRenderedPart(p.moveTo(x, y, z), buffWriter,viewMatrix);
			}
		}
	}
	
	
	
	public void startRender() throws IOException {
		
		buffWriter.write("#version 3.7;\n#include \"jbbheader.inc\"\n");
		//buffWriter.write(lightSource);
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
