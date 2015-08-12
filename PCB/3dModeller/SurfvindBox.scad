include <Surfvind_measures.scad>
color([1,0,1])
cube([width,length,thickness],center=false);
translate([10,60,1.8])
linear_extrude(height = 1, center = false){
text("TNA 2015");
}
translate([20,80,1.8])
linear_extrude(height = 1, center = false){
text("V 1.1");
    
    
}
//Left edge
color([1,0,1])
difference(){
  cube([thickness,length, height], center = false);
  //holes for sensors
  for(i=[0,1,2,3]){
	translate([0,start_offset+thickness+ i*rj45_width,skruv_hojd])
   cube([thickness,rj45_width, height], center = false);
 }
}

//Far short edge
translate([0,length-thickness,0])
cube([width,thickness, height], center = false);

//Near short edge
difference(){
  cube([width,thickness, height], center = false);
  {
      //Power connector hole
  translate([thickness+power_offset,0,skruv_hojd])
  cube([power_width,thickness, height] ,center =false);
      //Usb
  translate([width -(thickness+usb_offset+usb_width),0,skruv_hojd-2])
  cube([usb_width,thickness, height] ,center =false);
      
  }
}

//Right edge
translate([width-thickness,0,0])
cube([thickness,length, height], center = false);

radie = 4;
inner_radie=1.7;



difference(){
    translate([hole1x,hole1y,0])
    difference(){
      cylinder(skruv_hojd,radie,radie,center=false);
      cylinder(skruv_hojd,inner_radie,inner_radie,center=false);
    }
    translate([hole1x+2,hole1y-radie,0])
    cube(skruv_hojd);
}

translate([hole2x,hole2y,0])
difference(){
cylinder(skruv_hojd,radie,radie,center=false);
cylinder(skruv_hojd,inner_radie,inner_radie,center=false);
}

translate([hole3x,hole3y,0])
difference(){
cylinder(skruv_hojd,radie,radie,center=false);
cylinder(skruv_hojd,inner_radie,inner_radie,center=false);
}

translate([hole4x,hole4y,0])
difference(){
cylinder(skruv_hojd,radie,radie,center=false);
cylinder(skruv_hojd,inner_radie,inner_radie,center=false);
}     
