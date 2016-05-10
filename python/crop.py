from PIL import Image
from PIL import ImageDraw

import ocr
import util


def ShowImageWithBoundingBoxsAndCrop(image_file, highlight):
  text_annotator = ocr.TextAnnotator()
  result = text_annotator.GetTextAnnotations(image_file)
  if not result.Parse():
    raise Exception('Picture does not contain valid text.')

  image = Image.open(image_file)
  draw = ImageDraw.Draw(image)
  draw.rectangle(result.GetBoundingBox(), outline='rgb(255,0,0)')
  # Draw bounding box for each word.
  for box in result.GetSubBoundingBoxes():
    draw.rectangle(box, outline='rgb(0,0,255)')

  draw.rectangle(highlight, fill='rgb(0,255,0)')

  del draw
  image.show()

  sub_bounding_boxes = result.GetSubBoundingBoxes()
  print(zip(result.GetSubBoundingBoxes(), result.GetSubTexts()))
  rect_list = util.IntersectMultipleWithRatio(highlight,
                                              sub_bounding_boxes,
                                              ratio=0.5)
  sub_box_text_map = result.GetSubBoxTextMap()

  print(' '.join([sub_box_text_map[rect] for rect in rect_list]))


def main():
  # image_file = '../data/two_line.png'
  # highlight = (200, 175, 350, 180)

  image_file = '../data/rotate.jpg'
  highlight = (200, 230, 700, 320)
  # image_file = '../data/roast_pork.png'
  # image_file = '../data/test1.jpg'

  ShowImageWithBoundingBoxsAndCrop(image_file, highlight)


if __name__ == '__main__':
  main()
