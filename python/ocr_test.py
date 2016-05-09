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


if __name__ == '__main__':
  unittest.main()
