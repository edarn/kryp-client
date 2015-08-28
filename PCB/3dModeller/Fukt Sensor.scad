width = 65;
length = 80;
height = 45;
thickness = 2;




difference(){
rotate([90,0,0])
{
difference(){
    union(){
        //Main outer cube which contains the sensor.
        cube(size = [width, length, height],center=false);
        
        //Infästningen vänster sida
        difference(){
           translate(v=[0,2*length/3,0])
            
           rotate([0,0,-45])
           cube(size = [19, 19, thickness],center=true);
           //hole
           translate(v=[-4,2*length/3,0])
           cylinder(h = thickness+1, r=3, center = true);
        }

        //Infästning höger sida
        difference(){
            translate(v=[width,2*length/3,0])
            rotate([0,0,-45])
            cube(size = [19,19, thickness],center=true);
            //hole
            translate(v=[width+4,2*length/3,0])
            cylinder(h = thickness+1, r=3, center = true);
        }
    }
    translate(v = [thickness,0,thickness], center = false)
    cube(size = [width-2*thickness, length, height-2*thickness],center=false);
}

    //translate(v=[0,-29,length+23])
 /*   
rotate([-90,0,40])
translate(v=[width/2,-height/2,length])
   linear_extrude(height = 1, center = true){
text("V 1.2", center=true);

   }*/
altWidth=2*width/3+2;
translate(v=[width/2,length,0])
 translate(v=[0,0,height/2])
{
    rotate([0,0,-90]){
        union(){
            difference(){
                rotate([0,0,45]){
                    difference(){
                        cube(size =[altWidth, altWidth,height], center = true);
                        cube(size =[altWidth -2, altWidth-2,height-2], center = true);
                    }
                }
 union(){
    translate(v=[width/2,0,0])
    cube(size =[width, width,height], center = true);
    translate(v=[-width/2-30,0,0])
    cube(size =[width, width,height], center = true);
   }
   
 }
 
 
 
 
 
 
 translate(v=[-8,0,0])
 rotate([0,0,45])
    {
     difference()
     {
        union(){
             //Top cube witch creates a ventilation shaft.
             cube(size =[altWidth, altWidth,height], center = true);
             //Text
             translate(v=[-altWidth/4,altWidth/2,5])
             rotate([-90,0,0])
             linear_extrude(height = 2, center = true){
                 text("V 1.2", center=true);
             }
             



         }
         translate(v=[2,-2,0])
         difference(){
         cube(size =[altWidth , altWidth,height-2], center = true);
             //Support for ventilation holes - right side
            translate(v=[altWidth/2-3,altWidth/2,0])
            cube(size = [3, 7, 3],center=true);
            //Support for ventilation holes - left side
            translate(v=[-altWidth/2,-altWidth/2-3,0])
            cube(size = [7, 13, 3],center=true);
         }
     }
    }
 }
 }
}









// Pinnarna i boxen
translate(v=[width/4,length/5,thickness])
rotate([-45,0,0])
cube(size=[5,5,height-15], center = false);
 
translate(v=[3*width/4-5,length/5,thickness])
rotate([-45,0,0])
cube(size=[5,5,height-15], center = false);
 
/*
translate(v=[width/2-10,5,thickness])
rotate([-45,45,0])
cube(size=[3,2,15], center = false);
 
translate(v=[width/2+10,15,0])
rotate([-45,-45,0])
cube(size=[3,2,15], center = false);
*/

translate(v=[4*width/5,4,thickness])
rotate([90,90,0])
difference(){
difference(){
    cylinder(h=4,r=6, center = false);
    cylinder(h=4,r=4, center = false);
}
union(){
translate(v=[0,-width/2,0])
cube(size=[width,width,10], center =false);

rotate(-45,0,0)
translate(v=[-width/2-2,0,0])
cube(size=[width,width,10], center =false);

}
}




}
translate(v=[0,5,0])
 cube(size = [100,10,100],center=true);
}
 