
/////////////////////////////////
//
//   Primitive substitution
//
/////////////////////////////////

// rules:
// syntax COMMENT TAG LDraw_name
// where COMMENT=//
// TAG=#JBB (uppercase)
// LDraw_name=filename of primitive to subst with POVRay primitives
// follow by a declare where name is 'LD'+LDraw filename without extension (.dat)
//    all uppercase, with '-' changed to '_'

// NB: substitution is operated on normal 8\... and 48\... primitives with same name
//       i.e. 4-4cyli.dat, 8\4-4cyli.dat and 48\4-4cyli.dat are all changed to LD4_4CYLI 



// #JBB 1-4ccyli.dat
#declare LD1_4CCYLI=object {
  torus { 2.5,1 hollow }
  clipped_by {
    plane { -x,0 }
    plane { -z,0 }
  }  
}



// #JBB 1-4chrd.dat
#declare LD1_4CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by { plane { <-1,0,-1>, -1/sqrt(2) } }
}




// #JBB 1-4cyls.dat
#declare LD1_4CYLS=object {
  cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }
  clipped_by {  
    box { <0, 0, 0>, <1, 1, 1> }
    plane { <1,1,0>, 1/sqrt(2) } 
  }
}


// #JBB 1-4cyls2.dat
#declare LD1_4CYLS2=object {
  cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }
  clipped_by {  
    box { <0, 0, 0>, <-1, 1, 1> }
    plane { <1,1,0>, 0 } 
  }
}


// #JBB 1-4ndis.dat
#declare LD1_4NDIS=difference {
  polygon {
    5,
    <0,0,0>,<1,0,0>,<1,0,1>,<0,0,1>,<0,0,0>
  }
  cylinder { <0, -1, 0>, <0, 1, 0>, 1 } 
}



// #JBB 1-8chrd.dat
#declare LD1_8CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by {
    prism {
      -1,1,4,
      <1,0>,<sin(pi/4),cos(pi/4)>,<1,1>,<1,0>
    } 
    //plane { <-cos(pi/8),0,-sin(pi/8)>, -(1+1/sqrt(2))/2 } 
  }
}


// #JBB 1-8ndis.dat
#declare LD1_8NDIS=difference {
  polygon {
    4,
    <0,0,0>,<1,0,0>,<1,0,1>,<0,0,0>
  }
  cylinder { <0, -1, 0>, <0, 1, 0>, 1 } 
}


// JBB 1-8ring3.dat
//#declare LD1_8RING3=object {
//  disc { <0, 0, 0>, <0, 1, 0>, 4, 3  }
//  clipped_by { 
//    plane { <-1,0,1>,0 }
//	plane { <0,0,-1>,0 } 
//  }
//}


// #JBB 1-8sphe.dat
#declare LD1_8SPHE=object {
  sphere { <0, 0, 0>, 1 hollow }
  clipped_by {
    plane { -z,0 }
    plane { -y,0 }
    plane { -x,0 }
  }
}


// #JBB 1-12chrd.dat
#declare LD1_12CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by {
    prism {
      -1,1,4,
      <1,0>,<cos(pi/6),sin(pi/6)>,<2,1>,<1,0>
    } 
  }
}


// #JBB 1-16chrd.dat
#declare LD1_16CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by {
    prism {
      -1,1,4,
      <1,0>,<cos(pi/8),sin(pi/8)>,<2,1>,<1,0>
    } 
    //plane { <-cos(pi/8),0,-sin(pi/8)>, -(1+1/sqrt(2))/2 } 
  }
}


// #JBB 1-16ndis.dat
#declare LD1_16NDIS=difference {
  polygon {
    4,
    <1,0,0>,<1,0,1>,<0.875,0,0>,<1,0,0>
  }
  cylinder { <0, -1, 0>, <0, 1, 0>, 1 } 
}


// #JBB 2-4chrd.dat
#declare LD2_4CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by {  box { <-1, -1, 0>, <1, 1, 1> } }
}


// #JBB 2-4cyls.dat
#declare LD2_4CYLS=object {
  cylinder { <0, 0, 0>, <0, 2, 0>, 1 open }
  clipped_by {  
    plane { -z,0 }
    plane { <1,1,0>, 1/sqrt(2) } 
  }
}


// #JBB 2-4ndis.dat
#declare LD2_4NDIS=difference {
  polygon {
    5,
    <-1,0,0>,<1,0,0>,<1,0,1>,<-1,0,1>,<-1,0,0>
  }
  cylinder { <0, -1, 0>, <0, 1, 0>, 1 } 
}



// #JBB 2-8sphe.dat
#declare LD2_8SPHE=object {
  sphere { <0, 0, 0>, 1 hollow }
  clipped_by {
    plane { -z,0 }
    plane { -y,0 }
  }
}


// #JBB 3-4ndis.dat
#declare LD3_4NDIS=object {
  polygon {
    7,
    <0,0,0>,<0,0,-1>,<-1,0,-1>,<-1,0,1>,<1,0,1>,<1,0,0>,<0,0,0>
  }
  clipped_by { 
    cylinder { <0, -1, 0>, <0, 1, 0>, 1 inverse }
  } 
}


// #JBB 3-8chrd.dat
#declare LD3_8CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by { plane { <-sin(pi/8),0,-cos(pi/8)>, -sin(pi/8) } }
}


// #JBB 3-8ndis.dat
#declare LD3_8NDIS=object {
  polygon {
    5,
    <-1,0,1>,<0,0,0>,<1,0,0>,<1,0,1>,<-1,0,1>
  }
  clipped_by { 
    cylinder { <0, -1, 0>, <0, 1, 0>, 1 inverse }
  } 
}


// #JBB 3-16chrd.dat
#declare LD3_16CHRD=object {
  disc { <0, 0, 0>, <0, 1, 0>, 1 }
  clipped_by { 
    prism {
      -1,1,4,
      <1,0>,<sin(pi/8),cos(pi/8)>,<1,1>,<1,0>
    } 
  }
}


// #JBB 3-16cyls.dat
#declare LD3_16CYLS=object {
  cylinder { <0, 0, 0>, <0, 1, 0>, 1 open }
  clipped_by {  
    plane { -z,0 }
    plane { <-cos(pi/8),0,sin(pi/8)>, 0 } 
    plane { <1,1,0>, 1/sqrt(2) } 
  }
}


// #JBB 3-16ndis.dat
#declare LD3_16NDIS=difference {
  polygon {
    4,
    <1,0,0>,<1,0,1>,<sin(pi/8),0,cos(pi/8)>,<1,0,0>
  }
  cylinder { <0, -1, 0>, <0, 1, 0>, 1 } 
}



// #JBB 4-4cyls.dat
#declare LD4_4CYLS=object {
  cylinder { <0, 0, 0>, <0, 2, 0>, 1 open }
  clipped_by {  
    plane { <1,1,0>, 1/sqrt(2) } 
  }
}


// #JBB 4-4ndis.dat
#declare LD4_4NDIS=difference {
  polygon {
    5,
    <-1,0,-1>,<1,0,-1>,<1,0,1>,<-1,0,1>, <-1,0,-1>
  }
  cylinder { <0, -1, 0>, <0, 1, 0>, 1 } 
}


// #JBB 4-8sphe.dat
#declare LD4_8SPHE=object {
  sphere { <0, 0, 0>, 1 hollow }
  clipped_by {  box { <-1, 0, -1>, <1, 1, 1> } }
}



// #JBB 8-8sphe.dat
#declare LD8_8SPHE=object {
  sphere { <0, 0, 0>, 1 }
}


////////////////////////////////
//
//   torus
//
////////////////////////////////


// #JBB t04ounit.dat
#declare LDT04OUNIT=object {
  torus { 1, 1 hollow }
  clipped_by {
    plane { -y,0 }
    plane { -x,0 }
    plane { -z,0 }
    cylinder { <0,-1,0>,<0,1,0>, 1  inverse }
  }
}



/////////////////////////////
//
//   Complex parts (studs)
//
/////////////////////////////



// #JBB stud.dat
#declare LDSTUD=union {
  cylinder {<0,0,0>,<0,-3.8,0>,6 open}
  torus { 
    5.8, 0.2 
    translate -3.8*y
  }
  //cylinder {<0,-4,0>,<0,-3.6,0>,5.8 open }
  disc { <0,-4,0>, y, 5.8, 0 }
  text {
    ttf "lego-font.ttf" "LEGO" .06,-0.01*x
    matrix <0.7, 0, 0,
      .2, 1, 0,
      0, 0, 1,
      0, 0, 0 >
    rotate 90*x
    rotate -90*y
    scale 6.7
    translate <2.5,-3.85,-4.5>
  }
}


// #JBB stud2.dat
#declare LDSTUD2=union {
  cylinder {<0,0,0>,<0,-3.8,0>,6 open}
  torus { 
    5.8, 0.2 
    translate -3.8*y
  }
  cylinder {<0,0,0>,<0,-3.8,0>,4 open}
  torus {
    4.2, 0.2
    translate -3.8*y
  }
  disc { <0,-4,0>, y, 5.8,4.2}
}


// JBB stud3.dat
#declare LDSTUD3=union {
  cylinder {<0,0,0>,<0,-4,0>,4 open}
  disc { <0,-4,0>,y,4 }
}


// #JBB stud4.dat
#declare LDSTUD4=difference {
  cylinder { <0,0,0>,<0,-4,0>, 8 }
  cylinder { <0,0.1,0>,<0,-4.1,0>, 6 }
}



/////////////////////////////////////
//
//   Junctions, special elements...
//
/////////////////////////////////////


// #JBB cylj4x8.dat
#declare LDCYLJ4X8=difference {
  cylinder { <0,0,0>,<0,-2,0>, 4 }
  cylinder { <0,-8,-10>,<0,-8,10>, 8 }
}


///////////
//  END  //
///////////