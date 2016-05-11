# menu_picture
Show pictures for menu item

# TODO
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
12. safety(no gross stuff)

# Bugs
1. ../data/roast_pork.png. Background black* color result in rgb(255,0,0) exception.

# Reference
1. [PIL](https://pillow.readthedocs.io/en/latest/handbook/index.html)
2. [PIL Annotation](http://effbot.org/imagingbook/imagedraw.htm)