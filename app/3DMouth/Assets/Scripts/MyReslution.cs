using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MyReslution : MonoBehaviour
{
    public ScreenOrientation orientation;
    private void Start()
    {
        Screen.orientation = orientation;
    }
}
