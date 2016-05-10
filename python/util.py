def Intersect(rect1, rect2):
  '''Return the intersecting rectangle or None.'''
  l1, t1, r1, b1 = rect1
  l2, t2, r2, b2 = rect2

  left = max(l1, l2)
  top = max(t1, t2)
  right = min(r1, r2)
  bottom = min(b1, b2)

  if left >= right or top >= bottom:
    return None

  return (left, top, right, bottom)


def Area(rect):
  left, top, right, bottom = rect
  return float(right - left) * (bottom - top)


def IntersectMultipleWithRatio(highlight, rect_list, ratio=0.0):
  '''Return a list of rectangles with overlapping area larger than ratio of target rectangle.'''
  result = []
  for rect in rect_list:
    overlap = Intersect(highlight, rect)
    if overlap is not None and Area(overlap) / Area(rect) >= ratio:
      result.append(rect)

  return result
