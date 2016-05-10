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
  return (right - left) * (bottom - top)
