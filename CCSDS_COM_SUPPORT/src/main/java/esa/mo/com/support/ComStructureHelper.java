package esa.mo.com.support;

import org.ccsds.moims.mo.com.structures.ObjectType;

/**
 *
 */
public class ComStructureHelper
{
  /**
   * Generate a EntityKey sub key using fields as specified in COM STD 3.2.4.2b
   *
   * @param area
   * @param service
   * @param version
   * @param objectNumber
   * @return
   */
  static public Long generateSubKey(int area, int service, int version, int objectNumber)
  {
    long subkey = objectNumber;
    subkey = subkey | (((long) version) << 24);
    subkey = subkey | ((long) service << 32);
    subkey = subkey | ((long) area << 48);

    return subkey;
  }

  /**
   * Generate a EntityKey sub key using fields as specified in COM STD 3.2.4.2b
   *
   * @param objectType
   * @return
   */
  static public Long generateSubKey(ObjectType objectType)
  {
    return generateSubKey(objectType.getArea().getValue(),
            objectType.getService().getValue(),
            objectType.getVersion().getValue(),
            objectType.getNumber().getValue());
  }
}
