
//////////////////////////////
//
//   LDPovRay header file
//
//////////////////////////////

global_settings { assumed_gamma 2.2 }


// Finish for LDraw materials

// normal ABS shiny plastic
#declare SOLID = finish {
  specular 0.2
  roughness 0.001
  reflection 0.025 
  //ambient 0
  diffuse 0.6
  conserve_energy
}

// transparent polycarbonate parts
#declare TRANSPARENT = finish{
  //specular 0.1
  //ambient 0
  //diffuse 0.4
  reflection 0.02
  phong 0.3
  phong_size 120
  conserve_energy
}

// metallic
#declare METAL = finish {
  ambient 0.3
  diffuse 0.7
  reflection 0.05
  brilliance 8
  specular 0.8
  roughness 0.1
}

// chrome
#declare CHROME = finish {
  metallic
  ambient 0.2
  diffuse 0.7
  brilliance 6
  reflection 0.25
  phong 0.75
  phong_size 80
}

// pearl metal
#declare PEARL = finish {
  ambient 0.3
  diffuse 1.0
  brilliance 15
  phong 0.41
  phong_size 5
}

// rubber
#declare RUBBER = finish {
  specular 0.1
  roughness 0.5
}

#declare INTERIOR_TRANS = interior { ior 1.2 }


// default light sources
light_source { z*1e6 color rgb 0.6 shadowless } 
light_source { <-50000, 100000, 100000> color rgb <1,1,1>  area_light <500, 0, 0>, <0, 0, 500>, 3, 3 adaptive 1 jitter }


// sky sphere
sky_sphere {
  pigment {
    gradient y
    color_map {
      [0 color rgb <0.7,0.7,.9>]
      [1 color rgb <0.9,0.9,1>]
    }
    scale 2
    translate -1
  }
}


#include "jbbmaterials.inc"
#include "jbbprimitives.inc"
