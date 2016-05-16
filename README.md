# menu_picture
Show pictures for menu item

# TODO(Python)
1. ~~clip rectangle.~~
2. ~~multiple connecting clip rectangle.~~ Currently use a single clipping rectangle to decide connectivity.
3. handle empty/error text annotations.
4. multiple response
5. language
6. ~~Show cropped picture.~~
7. ~~picture rotate.~~ Bounding box is always parallel to x, y axis.
8. ~~Annotate bounding box.~~ Basic rectangle annotation for the biggest bounding box.
9. Multiple sub bounding box annotation.
10. location info search
11. Image type.
12. ~~safety(no gross stuff)~~
13. ~~Search Image.~~
14. ~~Render Image.~~
15. ~~Combine Image.~~

# TODO(Android)
1. ~~Highlight~~
2. ~~Bounding Highlight.~~
3. ~~Canvas view with image background.~~
4. ~~align image to top.~~ aligh/scale with aspect ratio to the center
5. ~~highlight marker.~~
6. ~~add a imageview with modified pciture.~~ a view is good
7. ~~Highlight on top of picture.~~
8. pan move picture.
9. hidding button to control edit/move mode.
10. vision api.
11. crop
12. search
13. hidding tab to show search result.

# Bugs
1. ../data/roast_pork.png. Background black* color result in rgb(255,0,0) exception.

# Reference
1. [PIL](https://pillow.readthedocs.io/en/latest/handbook/index.html)
2. [PIL Annotation](http://effbot.org/imagingbook/imagedraw.htm)