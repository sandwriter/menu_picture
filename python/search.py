import urllib

from googleapiclient import discovery
from PIL import Image


class ImageSearch(object):
  CUSTOM_SEARCH_API = 'AIzaSyBFoF4sDsSj6FV8O-cYsyHbU9stfIrACJg'
  MENU_PICTURE_CSE = '000057874177480001711:2ywzhtb3u6q'

  def __init__(self):
    self.service = discovery.build('customsearch',
                                   'v1',
                                   developerKey=self.CUSTOM_SEARCH_API)

  def Search(self, text, num=5):
    response = self.service.cse().list(q=text,
                                       cx=self.MENU_PICTURE_CSE,
                                       searchType='image',
                                       safe='high',
                                       num=num).execute()
    image_link_list = []
    if not response or 'items' not in response:
      return None

    for item in response['items']:
      if 'link' in item and 'image':
        image_link_list.append(item['link'])

    return image_link_list


def main():
  num = 5
  thumbnail_size = (400, 300)
  background = Image.new('RGBA', (thumbnail_size[0], thumbnail_size[1] * num),
                         (255, 255, 255, 255))
  image_search = ImageSearch()
  image_link_list = image_search.Search('grilled steak', num=num)
  for i, image_link in enumerate(image_link_list):
    tmp_image_file = '/tmp/img%d' % i
    urllib.urlretrieve(image_link, tmp_image_file)
    im = Image.open(tmp_image_file)
    im.thumbnail(thumbnail_size)
    offset = (0, thumbnail_size[1] * i)
    background.paste(im, offset)
  background.show()


if __name__ == '__main__':
  main()
