using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class kongZhiJingTou : MonoBehaviour {

    public ObjectInteraction oi;

    public Vector3 oldV3a, oldV3b;
    public int oldLe=0;
    public float oldDis=0;
    public float speed = 0.001f;
    public float speedRot = 0.1f;
    public Vector2 clx = new Vector2(-13.5f, -12f),
        cly = new Vector2(-3f, -1f), 
        clz = new Vector2(11.5f, 13f);

    // Update is called once per frame
    void Update () {

        //if (Input.GetMouseButtonDown(0))
        //{
        //    oldV3a = Input.mousePosition;

        //}
        //if (Input.GetMouseButton(0))
        //{
        //    transform.Rotate((oldV3a - (Vector3)Input.mousePosition).x*Vector3.down * speedRot);
        //    oldV3a = Input.mousePosition;
        //}

        if (oi.enabled)
            return;



        switch (Input.touchCount)
        {
            case 1:
                if(oldLe!= Input.touchCount)
                {
                    oldV3a = Input.touches[0].position;
                }
                transform.Translate((oldV3a - (Vector3)Input.touches[0].position)* speed);



                oldV3a = Input.touches[0].position;
                break;
            case 2:
                if (oldLe != Input.touchCount)
                {
                    oldV3a = Input.touches[0].position;
                    oldV3b = Input.touches[1].position;
                    oldDis = Vector2.Distance(Input.touches[0].position, Input.touches[1].position);
                }

                if (Mathf.Abs( oldDis - Vector2.Distance(Input.touches[0].position, Input.touches[1].position)) < 8)
                {
                    transform.Rotate((oldV3a - (Vector3)Input.touches[0].position).x * Vector3.down * speedRot);
                }
                else
                {
                    transform.Translate(Vector3.back * speed *(oldDis - Vector2.Distance(Input.touches[0].position, Input.touches[1].position)));
                }



                oldV3a = Input.touches[0].position;
                oldV3b = Input.touches[1].position;
                oldDis = Vector2.Distance(Input.touches[0].position, Input.touches[1].position);
                break;
        }
        oldLe = Input.touchCount;
        transform.position = new Vector3(
           Mathf.Clamp(transform.position.x, clx.x, clx.y),
           Mathf.Clamp(transform.position.y, cly.x, cly.y),
           Mathf.Clamp(transform.position.z, clz.x, clz.y)
           );
    }
}
