using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class XuanZhuan : MonoBehaviour
{
    public GameObject MoXin;
  
    public void 左转()
    {
     transform.Rotate(Vector3.up * 5);
     print(0);
    }
   public void 右转()
   {
    transform.Rotate(Vector3.down * 5);
   }
    // Start is called before the first frame update
    void Start()
    {
      
    }
    public float _左右_;
    public void _设置左右_(float a)
    {
        _左右_ = a;
    }
    // Update is called once per frame
    void Update()
    {
        transform.Rotate(_左右_ * Vector3.up);
    }
}
