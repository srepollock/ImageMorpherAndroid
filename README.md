# MorphingProject-4932
Morphing project for COMP4932
---
This is my morphing android application. This will take two images and morph (or change) the first image into the second image.

This is a project completed for my COMP4932 class.
---

#To Do:
-------
- warp images (both ways) // should be done
- cross-dissolve images // should be easy, just ask Dennis or someone about hwo to do it
- load in images from the app context. Then when going back delete the images from the context

#Testing:
---------
- Test the warp that the values are correct. Double check this by doing some math

#Warping:
---------
- for each pixel (x,y)
    - for each line
        - find distance to line from pixel
        - find fractional %
        - find new position (SAVE)
        - find average weight of new position (SAVE)
- source position
    - for each weight & position
        Sum(Wi∆i) / Sum(Wi)
        
#Need to Finish ***
---------------
- MorphDisplayActivity.LoadBitmaps() // this loads the bitmaps from all the ones created and saved to the application
- MorphDisplayActivity.CrossDissolve() // cross dissolves the images created from the intermediate warping of both functions
- MorphDisplayActivity.onStop() // deletes all the images from the application save
- WarpImage.intermediate() // needs to warp intermediate frames