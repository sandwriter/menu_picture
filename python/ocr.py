import argparse
import base64
import httplib2

from googleapiclient import discovery
from oauth2client.client import GoogleCredentials

# The url template to retrieve the discovery document for trusted testers.
DISCOVERY_URL = 'https://{api}.googleapis.com/$discovery/rest?version={apiVersion}'


def main(photo_file):
  """Run a label request on a single image"""

  credentials = GoogleCredentials.get_application_default()
  service = discovery.build('vision',
                            'v1',
                            credentials=credentials,
                            discoveryServiceUrl=DISCOVERY_URL)

  with open(photo_file, 'rb') as image:
    image_content = base64.b64encode(image.read())
    service_request = service.images().annotate(body={
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
    response = service_request.execute()
    print('number of text entities: %d' %
          len(response['responses'][0]['textAnnotations']))
    content = response['responses'][0]['textAnnotations'][0]['description']
    print('Found content: %s for %s' % (content, photo_file))
    return 0


if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('image_file', help='The image you\'d like to label.')
  args = parser.parse_args()
  main(args.image_file)
