from PIL import Image
from PIL import ImageDraw

import ocr


def ShowImageWithBoundingBox(image_file):
  text_annotator = ocr.TextAnnotator()
  result = text_annotator.GetTextAnnotations(image_file)
  if not result.Parse():
    raise Exception('Picture does not contain valid text.')

  image = Image.open(image_file)
  draw = ImageDraw.Draw(image)
  draw.rectangle(result.GetBoundingBox(), outline=128)
  del draw
  image.show()


def main():
  image_file = '../data/two_line.png'
  # image_file = '../data/rotate.jpg'
  # image_file = '../data/roast_pork.png'
  ShowImageWithBoundingBox(image_file)


if __name__ == '__main__':
  main()
