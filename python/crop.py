from PIL import Image
from PIL import ImageDraw

import ocr


def ShowImageWithBoundingBoxs(image_file):
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

  del draw
  image.show()


def main():
  # image_file = '../data/two_line.png'
  # image_file = '../data/rotate.jpg'
  # image_file = '../data/roast_pork.png'
  image_file = '../data/test1.jpg'
  ShowImageWithBoundingBoxs(image_file)


if __name__ == '__main__':
  main()
