using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Rotate : MonoBehaviour
{
    public Transform aim;
    public float speed = 5;

    private Vector3 oldPos;

    public void OnDragStart()
    {
        oldPos = Input.mousePosition;
    }

    public void OnDrag()
    {
        aim.localEulerAngles += (Input.mousePosition.x - oldPos.x) * Time.deltaTime * Vector3.up * speed;
        oldPos = Input.mousePosition;
    }
}
