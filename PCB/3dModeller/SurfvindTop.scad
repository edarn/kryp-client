include <Surfvind_measures.scad>;

radie = 4;
inner_radie=1.7;
inner_distance = 5;
stod = 4;
skrew_head=3.5;

translate([10,60,1.8])
linear_extrude(height = 1, center = false){
text("TNA 2015");
}
translate([20,80,1.8])
linear_extrude(height = 1, center = false){
text("V 1.1");
    
    
}

color([1,0,1])
difference(){
    union(){
        cube([width,length,thickness],center=false);
          translate([hole1y,hole1x,thickness-0.01])
        difference(){
        cylinder(stod,r=radie,center=false);
        cylinder(stod,r=inner_radie,center=false);
        }


        translate([width-hole2y,width-hole2x,thickness-0.01])
        difference(){
        cylinder(stod,radie,radie,center=false);
        cylinder(stod,inner_radie,inner_radie,center=false);
        }

        translate([hole3x,hole3y,thickness-0.01])
        difference(){
        cylinder(stod,radie,radie,center=false);
        cylinder(stod,inner_radie,inner_radie,center=false);
        }

        translate([hole4x,hole4y,thickness-0.01])
        difference(){
        cylinder(stod,radie,radie,center=false);
        cylinder(stod,inner_radie,inner_radie,center=false);
        }       
    }
    union(){
        translate([hole1y,hole1x,0])
        cylinder(thickness+2,skrew_head,inner_radie,center=false);
        translate([width-hole2y,width-hole2x,0])
        cylinder(thickness+2,skrew_head,inner_radie,center=false);
        translate([hole3x,hole3y,0])
        cylinder(thickness+2,skrew_head,inner_radie,center=false);
        translate([hole4x,hole4y,0])
        cylinder(thickness+2,skrew_head,inner_radie,center=false);
    }
}
difference(){
translate([thickness+0.5,thickness+0.5,thickness])
cube([width-2*thickness-1,length-2*thickness-1,thickness],center=false);
translate([thickness+0.5+1,thickness+0.5+1,thickness])
cube([width-2*thickness-1-2,length-2*thickness-1-2,thickness],center=false);
}



antal_sensorer=4;
//Left edge
color([1,0,1])
translate([width-thickness,start_offset+thickness+0.5,0])
cube([thickness,rj45_width*antal_sensorer-1, height-(skruv_hojd+rj45_height)], center = false);

//Short edge
translate([thickness+usb_offset+0.5,0,thickness])
cube([usb_width-1,thickness, height-(skruv_hojd+usb_height)], center = false);

translate([width-(thickness+power_offset+power_width)+0.5,0,thickness])
cube([power_width-1,thickness, height-(skruv_hojd+power_height)], center = false);

