using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ScaleMana : MonoBehaviour
{
    Vector2 OldPos1;
    Vector2 OldPos2;


    float xSpeed = 150;

    private void Update()
    {
        //放大缩小
        if (Input.touchCount == 2)
        {
            if (Input.GetTouch(0).phase == TouchPhase.Moved || Input.GetTouch(1).phase == TouchPhase.Moved)
            {
                Vector2 temPos1 = Input.GetTouch(0).position;
                Vector2 temPos2 = Input.GetTouch(1).position;
                if (isEnlarge(OldPos1, OldPos2, temPos1, temPos2))
                {
                    float oldScale = transform.localScale.x;
                    float newScale = oldScale * 1.025f;
                    transform.localScale = new Vector3(newScale, newScale, newScale);

                }
                else
                {
                    float oldScale = transform.localScale.x;
                    float newScale = oldScale / 1.025f;
                    transform.localScale = new Vector3(newScale, newScale, newScale);
                }

                OldPos1 = temPos1;
                OldPos2 = temPos2;
            }
        }
    }

    /// <summary>
    /// 手势判断函数
    /// </summary>
    /// <param name="oP1"></param>
    /// <param name="oP2"></param>
    /// <param name="nP1"></param>
    /// <param name="nP2"></param>
    /// <returns></returns>
    bool isEnlarge(Vector2 oP1, Vector2 oP2, Vector2 nP1, Vector2 nP2)
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
