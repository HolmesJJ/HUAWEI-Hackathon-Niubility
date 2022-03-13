using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TextureManager : MonoBehaviour {

	[HideInInspector]public TextureChanger currentTextureChanger;
	[HideInInspector]public ColorChanger currentColorChanger;
	[HideInInspector]public LightChanger currentLightChanger;
	int ObjectID;
	int itemID;
	public GameObject textureButton,colorButton,lightButton;



	public void NextTexture()
	{
		if (currentTextureChanger) {
			itemID++;

			if (itemID > currentTextureChanger.textures.Length - 1)
				itemID = 0;

			currentTextureChanger.UpdateTexture (itemID);
		}
	}
	public void NextColor()
	{
		if (currentColorChanger) {
			itemID++;

			if (itemID > currentColorChanger.colors.Length - 1)
				itemID = 0;

			currentColorChanger.UpdateColor (itemID);
		}
	}



	public void ToggleLight()
	{
		if (currentLightChanger) {
			currentLightChanger.UpdateLight ();
		}
	}


	public void ToggleObject(GameObject target)
	{
		target.SetActive (!target.activeSelf);
	}

	void Update()
	{
		// We want to clear save color data
		if (Input.GetKeyDown (KeyCode.E)) {
			PlayerPrefs.DeleteAll ();
			Debug.Log ("PlayerPrefs.DeleteAll ()");

		}
	}

}
