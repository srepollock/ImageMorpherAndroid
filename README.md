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
- Crop photos
- weight calculation
- x' (point) = P' (point) + % * vector(P'Q') - d * (vector(N) / |vector(N)|) SUMMATION OF THE DELTA
- Sum(wi*∆i) / Sum(wi) for weighted changes
    - ∆i * wi = ((x0, y0) - (orgx, orgy)) * w(x, y)
- weight = ((line length)^P / a + distance)^b
    - a > 0
    - 1 <= b <= 2
    - 0 <= p <= 1