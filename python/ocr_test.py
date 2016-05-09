import unittest

import ocr


class TestOcr(unittest.TestCase):

  def __init__(self, *args, **kwargs):
    super(TestOcr, self).__init__(*args, **kwargs)

  def setUp(self):
    self.text_annotator = ocr.TextAnnotator()

  def test_GetBoundingBox(self):
    result = self.text_annotator.GetTextAnnotations('../data/two_line.png')
    self.assertTrue(result.Parse())
    all_bounding_box = result.GetBoundingBox()
    self.assertEqual([258, 171, 346, 205], all_bounding_box)

  def test_GetSubBoundingBoxes(self):
    result = self.text_annotator.GetTextAnnotations('../data/two_line.png')
    self.assertTrue(result.Parse())
    sub_bounding_boxes = result.GetSubBoundingBoxes()
    self.assertEqual(
        [[258, 171, 287, 188], [295, 171, 334, 188], [258, 191, 302, 205],
         [310, 191, 346, 205]], sub_bounding_boxes)


if __name__ == '__main__':
  unittest.main()
