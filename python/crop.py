from PIL import Image


def main():
  image = Image.open('../data/test1.jpg')
  image.show()


if __name__ == '__main__':
  main()
