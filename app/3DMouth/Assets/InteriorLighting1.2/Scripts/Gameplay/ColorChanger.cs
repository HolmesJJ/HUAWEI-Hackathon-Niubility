using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ColorChanger : MonoBehaviour {

	[Header("Enter Object ID For Saving")]
	[Space(3)]
	public int ObjectID;
	[Header("List Of The Colors")]
	[Space(3)]
	public Color[] colors;
	[Header("List Of The Target Renderers")]
	[Space(3)]
	public MeshRenderer[] targetRenderers;
	[Header(" Material Index ")]
	[Space(3)]
	public int materialIndex;

	TextureManager manager;


	void Start()
	{
		manager = GameObject.FindObjectOfType<TextureManager> ();


		if (PlayerPrefs.GetInt ("ColorFirst" + ObjectID.ToString ()) != 0) {
			for (int a = 0; a < targetRenderers.Length; a++)
				targetRenderers [a].materials [materialIndex].color = colors [PlayerPrefs.GetInt ("Color" + ObjectID.ToString ())];
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
		manager.textureButton.SetActive (false);
		manager.colorButton.SetActive (true);
		manager.lightButton.SetActive (false);
	}

	public void UpdateColor(int id)
	{
		for(int a = 0;a<targetRenderers.Length;a++)
			targetRenderers[a] .materials[materialIndex].color = colors [id];

		PlayerPrefs.SetInt ("Color" + ObjectID.ToString(), id);
		PlayerPrefs.SetInt ("ColorFirst" + ObjectID.ToString(), 3);





	}
}
