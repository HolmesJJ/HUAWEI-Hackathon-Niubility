using System.Collections;
using System.Collections.Generic;
using UnityEngine;


//物体交互
public class ObjectInteraction : MonoBehaviour {

    Vector2 OldPos1;
    Vector2 OldPos2;


    public float xSpeed = 150;


    private Vector3 startFingerPos;
    private Vector3 nowFingerPos;
    private float xMoveDistance;
    private float yMoveDistance;
    private int backValue = 0;


    public int Bool;
    // Use this for initialization
    void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {

        //旋转
        if (Input.GetMouseButton(0))
        {
            if (Input.touchCount == 1)
            {
                if (Input.GetTouch(0).phase == TouchPhase.Moved)
                {
                    if (Bool==1)
                    {
                        transform.Rotate(Vector3.left * Input.GetAxis("Mouse X") * -xSpeed * Time.deltaTime, Space.World);
                        transform.Rotate(Vector3.left * Input.GetAxis("Mouse Y") * -xSpeed * Time.deltaTime, Space.World);
                    }
                    else if (Bool==2)
                    {
                        transform.Rotate(Vector3.up * Input.GetAxis("Mouse X") * -xSpeed * Time.deltaTime, Space.World);
                        transform.Rotate(Vector3.up * Input.GetAxis("Mouse Y") * -xSpeed * Time.deltaTime, Space.World);
                    }
                    else if (Bool==3)
                    {
                        transform.Rotate(Vector3.forward * Input.GetAxis("Mouse X") * -xSpeed * Time.deltaTime, Space.World);
                        transform.Rotate(Vector3.forward * Input.GetAxis("Mouse Y") * -xSpeed * Time.deltaTime, Space.World);
                    }
                    
                }
            }
           

        }


        if (Input.GetMouseButton(0))
        {
            if (Bool == 1)
            {
                transform.Rotate(Vector3.left * Input.GetAxis("Mouse X") * -xSpeed * Time.deltaTime, Space.World);
                transform.Rotate(Vector3.left * Input.GetAxis("Mouse Y") * -xSpeed * Time.deltaTime, Space.World);
            }
            else if (Bool == 2)
            {
                transform.Rotate(Vector3.up * Input.GetAxis("Mouse X") * -xSpeed * Time.deltaTime, Space.World);
                transform.Rotate(Vector3.up * Input.GetAxis("Mouse Y") * -xSpeed * Time.deltaTime, Space.World);
            }
            else if (Bool == 3)
            {
                transform.Rotate(Vector3.forward * Input.GetAxis("Mouse X") * -xSpeed * Time.deltaTime, Space.World);
                transform.Rotate(Vector3.forward * Input.GetAxis("Mouse Y") * -xSpeed * Time.deltaTime, Space.World);
            }
        }
        
        ////放大缩小
        //if (Input.touchCount==2)
        //{
        //    if (Input.GetTouch(0).phase==TouchPhase.Moved||Input.GetTouch(1).phase==TouchPhase.Moved)
        //    {
        //        Vector2 temPos1 = Input.GetTouch(0).position;
        //        Vector2 temPos2 = Input.GetTouch(1).position;
        //        if (isEnlarge(OldPos1,OldPos2,temPos1,temPos2))
        //        {
        //            float oldScale = transform.localScale.x;
        //            float newScale = oldScale * 1.025f;
        //            transform.localScale = new Vector3(newScale, newScale, newScale);
                        
        //        }
        //        else
        //        {
        //            float oldScale = transform.localScale.x;
        //            float newScale = oldScale / 1.025f;
        //            transform.localScale = new Vector3(newScale, newScale, newScale);
        //        }

        //        OldPos1 = temPos1;
        //        OldPos2 = temPos2;
        //    }
        //}
	}



    /// <summary>
    /// 手势判断函数
    /// </summary>
    /// <param name="oP1"></param>
    /// <param name="oP2"></param>
    /// <param name="nP1"></param>
    /// <param name="nP2"></param>
    /// <returns></returns>
    bool isEnlarge(Vector2 oP1,Vector2 oP2,Vector2 nP1,Vector2 nP2)
    {
        float Length1 = Mathf.Sqrt((oP1.x - oP2.x) * (oP1.x - oP2.x) + (oP1.y - oP2.y) * (oP1.y - oP2.y));
        float Length2 = Mathf.Sqrt((nP1.x - oP2.x) * (nP1.x - oP2.x) + (nP1.y - nP2.y) * (nP1.y - nP2.y));
        if (Length1 < Length2)
        {
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
}
