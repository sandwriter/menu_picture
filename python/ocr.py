import argparse
import base64
import httplib2

from googleapiclient import discovery
from oauth2client.client import GoogleCredentials


class AnnotationResult(object):

  def __init__(self, json_response):
    self.json_response = json_response
    self._parsed = False

  def __str__(self):
    return str(self.json_response)

  def Parse(self):
    if not self.json_response:
      return False
    if ('responses' not in self.json_response) or len(self.json_response[
        'responses']) == 0:
      return False
    response = self.json_response['responses'][0]
    if ('textAnnotations' not in
        response) or len(response['textAnnotations']) == 0:
      return False

    self.token_count = len(response['textAnnotations']) - 1
    self.all_annotation = response['textAnnotations'][0]
    if ('description' not in self.all_annotation) or (
        not self.all_annotation['description']) or (
            'boundingPoly' not in self.all_annotation):
      return False

    self.all_bounding_box = self.BoundingBoxJson2List(self.all_annotation[
        'boundingPoly']['vertices'])
    self.all_text = self.all_annotation['description']

    self._parsed = True
    return True

  def GetBoundingBox(self):
    assert self._parsed
    return self.all_bounding_box

  @staticmethod
  def BoundingBoxJson2List(json_box):
    '''Return a list of (x, y) sorted by x then y in ascending order.'''
    return [json_box[0]['x'], json_box[0]['y'], json_box[2]['x'],
            json_box[2]['y']]


class TextAnnotator(object):
  # The url template to retrieve the discovery document for trusted testers.
  DISCOVERY_URL = 'https://{api}.googleapis.com/$discovery/rest?version={apiVersion}'

  def __init__(self):
    self.credentials = GoogleCredentials.get_application_default()
    self.service = discovery.build(
        'vision',
        'v1',
        credentials=self.credentials,
        discoveryServiceUrl=TextAnnotator.DISCOVERY_URL)

  def GetTextAnnotations(self, image_file):
    with open(image_file, 'rb') as image:
      image_content = base64.b64encode(image.read())
      service_request = self.service.images().annotate(body={
          'requests': [{
              'image': {
                  'content': image_content.decode('UTF-8')
              },
              'features': [{
                  'type': 'TEXT_DETECTION',
                  'maxResults': 1
              }]
          }]
      })
    response = AnnotationResult(service_request.execute())
    return response


def main():
  """Run a label request on a single image"""
  # parser = argparse.ArgumentParser()
  # parser.add_argument('image_file', help='The image you\'d like to label.')
  # args = parser.parse_args()
  # image_file = args.image_file

  text_annotator = TextAnnotator()
  # image_file = '../data/test1.jpg'
  # image_file = '../data/roast_pork.png'
  image_file = '../data/two_line.png'
  # image_file = '../data/rotate.jpg'

  result = text_annotator.GetTextAnnotations(image_file)
  if result.Parse():
    print(result.GetBoundingBox())

  # print('number of text entities: %d' %
  #       len(response['responses'][0]['textAnnotations']))
  # content = response['responses'][0]['textAnnotations'][0]['description']
  # print('Found content: %s for %s' % (content, image_file))
  # return 0


if __name__ == '__main__':
  main()
