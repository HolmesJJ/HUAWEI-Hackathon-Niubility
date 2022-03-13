using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TextureChanger : MonoBehaviour {

	[Header("Enter Object ID For Saving")]
	[Space(3)]
	public int ObjectID;
	[Header("List Of The Textures")]
	[Space(3)]
	public Texture2D[] textures;
	[Header("List Of The Target Renderers")]
	[Space(3)]
	public MeshRenderer[] targetRenderers;
	[Header("Material Index")]
	[Space(3)]
	public int materialIndex;

	TextureManager manager;


	void Start()
	{
		manager = GameObject.FindObjectOfType<TextureManager> ();


		if (PlayerPrefs.GetInt ("TextureFirst" + ObjectID.ToString ()) != 0) {
			for (int a = 0; a < targetRenderers.Length; a++)
				targetRenderers [a].materials [materialIndex].mainTexture = textures [PlayerPrefs.GetInt ("Texture" + ObjectID.ToString ())];

		}


	}



	void OnMouseDown()
	{
		if(GetComponent<TextureChanger>())
			manager.currentTextureChanger = GetComponent<TextureChanger>();
		if(GetComponent<ColorChanger>())
			manager.currentColorChanger = GetComponent<ColorChanger>();
		if(GetComponent<LightChanger>())
			manager.currentLightChanger = GetComponent<LightChanger>();

		manager.textureButton.SetActive (true);
		manager.colorButton.SetActive (false);
		manager.lightButton.SetActive (false);
	}

	public void UpdateTexture(int id)
	{
		for(int a = 0;a<targetRenderers.Length;a++)
			targetRenderers[a] .materials[materialIndex].mainTexture = textures [id];
		PlayerPrefs.SetInt ("Texture" + ObjectID.ToString(), id);
		PlayerPrefs.SetInt ("TextureFirst" + ObjectID.ToString(), 3);
	}
}
