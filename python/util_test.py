import unittest

import util


class TestUtil(unittest.TestCase):

  def __init__(self, *args, **kwargs):
    super(TestUtil, self).__init__(*args, **kwargs)

  def test_Area(self):
    rect1 = (0, 0, 10, 10)
    self.assertEqual(100.0, util.Area(rect1))

    rect2 = (10, 20, 30, 50)
    self.assertEqual(600.0, util.Area(rect2))

  def test_Intersect(self):
    rect1 = (0, 0, 100, 100)
    rect2 = (10, 20, 30, 40)
    rect3 = (5, 25, 35, 35)
    rect4 = (20, 30, 40, 50)
    rect5 = (40, 50, 60, 70)
    rect6 = (30, 20, 50, 40)
    self.assertEqual((10, 20, 30, 40), util.Intersect(rect1, rect2))
    self.assertEqual((10, 25, 30, 35), util.Intersect(rect2, rect3))
    self.assertEqual((20, 30, 30, 40), util.Intersect(rect2, rect4))
    self.assertEqual(None, util.Intersect(rect2, rect5))
    self.assertEqual(None, util.Intersect(rect2, rect6))

  def test_IntersectMultipleWithRatio(self):
    highlight = (0, 15, 70, 25)
    rect1 = (10, 10, 20, 30)
    rect2 = (30, 10, 40, 30)
    rect3 = (50, 10, 60, 30)
    rect4 = (70, 10, 80, 30)
    rect_list1 = [rect1, rect2, rect3]
    rect_list2 = [rect3, rect4]

    self.assertEqual([rect1, rect2, rect3],
                     util.IntersectMultipleWithRatio(highlight,
                                                     rect_list1,
                                                     ratio=0.0))
    self.assertEqual([rect1, rect2, rect3],
                     util.IntersectMultipleWithRatio(highlight,
                                                     rect_list1,
                                                     ratio=0.5))
    self.assertEqual([],
                     util.IntersectMultipleWithRatio(highlight,
                                                     rect_list1,
                                                     ratio=0.51))
    self.assertEqual([rect3],
                     util.IntersectMultipleWithRatio(highlight,
                                                     rect_list2,
                                                     ratio=0.5))


if __name__ == '__main__':
  unittest.main()
