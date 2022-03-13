using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LightChanger : MonoBehaviour {


	public int ObjectID;
	public Light targetLight;
	TextureManager manager;


	void OnMouseDown()
	{
		if(GetComponent<TextureChanger>())
			manager.currentTextureChanger = GetComponent<TextureChanger>();
		if(GetComponent<ColorChanger>())
			manager.currentColorChanger = GetComponent<ColorChanger>();
		if(GetComponent<LightChanger>())
			manager.currentLightChanger = GetComponent<LightChanger>();
		manager.textureButton.SetActive (false);
		manager.colorButton.SetActive (false);
		manager.lightButton.SetActive (true);
	}


	void Start()
	{manager = GameObject.FindObjectOfType<TextureManager> ();
		if (PlayerPrefs.GetInt ("Light" + ObjectID.ToString ()) == 1)
			targetLight.enabled = true;
		else
			targetLight.enabled = false;
	}

	public void UpdateLight()
	{
		targetLight.enabled = !targetLight.enabled;

		if(targetLight.enabled == true)
			PlayerPrefs.SetInt ("Light" + ObjectID.ToString (), 1);
		else
			PlayerPrefs.SetInt ("Light" + ObjectID.ToString (), 0);
		
	}
}
