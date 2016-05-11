import unittest

import search


class TestImageSearch(unittest.TestCase):

  def __init__(self, *args, **kwargs):
    super(TestImageSearch, self).__init__(*args, **kwargs)

  def setUp(self):
    self.image_search = search.ImageSearch()

  def test_Search(self):
    self.assertEqual(1, len(self.image_search.Search('grilled steak', num=1)))
    self.assertEqual(5, len(self.image_search.Search('grilled steak', num=5)))


if __name__ == '__main__':
  unittest.main()
