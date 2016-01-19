# MorphingProject-4932
Morphing project for COMP4932
---
This is my morphing android application. This will take two images and morph (or change) the first image into the second image.

This is a project completed for my COMP4932 class.
---

#To Do:
-------
- Setup new activity for once morph is done
    - scroll bar
    - image view
    - play slideshow
- Save instance state
	- this will be for going back after the morphing
- Implement forward warping
	- frame
	- multiple lines
	- warp

#Testing:
---------
- Testing GitFlow
- I have to morph pictures inside of MainActivity, save the photos, then pass them to the new activity


#Missing:
---------

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