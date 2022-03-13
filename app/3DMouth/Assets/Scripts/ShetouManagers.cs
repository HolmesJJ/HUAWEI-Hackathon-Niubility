using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
public class ShetouManagers : MonoBehaviour
{
    public void SprictEnter()
    {
        Debug.Log("开启脚本");
        gameObject.GetComponent<ObjectInteraction>().enabled = true;
    }

    public void SprictExit()
    {
        Debug.Log("关闭脚本");
        gameObject.GetComponent<ObjectInteraction>().enabled = false;
    }


    public void IntoButton()
    {
        SceneManager.LoadScene("VedioScene");
    }

    public void QuitButton()
    {
        SceneManager.LoadScene("FaYin_2");
    }
}
