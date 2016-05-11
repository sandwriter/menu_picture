from googleapiclient import discovery


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
  image_search = ImageSearch()
  print(image_search.Search('grilled steak', num=5))


if __name__ == '__main__':
  main()
