using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Video;
public class VideoManager : MonoBehaviour
{
    private VideoPlayer video;

    public GameObject PlayButton;

    public float time1, time2;
    private float timeNow;

    public Transform hong, lv,hong1;
    public float speed;

    public Animator yaAni;

    private bool isPlay = false;

    void Awake()
    {
        video = GetComponent<VideoPlayer>();
        Debug.Log(video.length);
        speed = 30 / (float)video.length;
    }
    void OnEnable()
    {
        video.loopPointReached += EndVideoTwo;
    }
    
    void Update()
    {
        if (isPlay)
        {
            timeNow += Time.deltaTime;
            if(video.time < time1)
            {
                hong.transform.localScale += Time.deltaTime * speed * Vector3.right;
            }
            if (video.time > time1 && video.time < time2)
            {
                lv.position = hong.transform.GetChild(0).GetChild(0).position;
                if (!yaAni.GetBool("open"))
                {
                    yaAni.SetBool("open", true);
                }
                lv.transform.localScale += Time.deltaTime * speed * Vector3.right;
            }
            else if(video.time > time2 && timeNow > time1)
            {
                hong1.position = lv.transform.GetChild(0).GetChild(0).position;
                if (yaAni.GetBool("open"))
                {
                    yaAni.SetBool("open", false);
                }
                hong1.transform.localScale += Time.deltaTime * speed * Vector3.right;
            }
        }
        
    }

    public void PlayVideo()
    {
        video.Play();
        isPlay = true;
        PlayButton.SetActive(false);
        hong.localScale = new Vector3(0, 1, 1);
        lv.localScale = new Vector3(0, 1, 1); 
        hong1.localScale = new Vector3(0, 1, 1);
        timeNow = 0;
    }

    void EndVideoTwo(VideoPlayer video)
    {
        PlayButton.SetActive(true);
        isPlay = false;
        Debug.Log("视频播放结束");
    }
}
