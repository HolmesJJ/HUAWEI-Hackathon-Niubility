using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;


public class Settings : MonoBehaviour {



	public Dropdown quality;
	public Toggle effect,bloom,showFPS;
	public GameObject selections;

	IEnumerator Start()
	{

		if (PlayerPrefs.GetInt ("FirstRun") != 1) {
			PlayerPrefs.SetInt ("PostEffect", 1);
			PlayerPrefs.SetInt ("FirstRun", 1);
		}


		if (PlayerPrefs.GetInt ("PostEffect") == 1) {
			
			Camera.main.GetComponent<MobileColorGrading> ().enabled = true;
			effect.isOn = true;
		} else {
			Camera.main.GetComponent<MobileColorGrading> ().enabled = false;
			effect.isOn = false;
		}

		if (PlayerPrefs.GetInt ("Bloom") == 1) {
			Camera.main.GetComponent<Kino.Bloom> ().enabled = true;
			bloom.isOn = true;
		} else {
			Camera.main.GetComponent<Kino.Bloom> ().enabled = false;
			bloom.isOn = false;
		}

		if (PlayerPrefs.GetInt ("FPS") == 1) {
			Camera.main.GetComponent<FPS> ().enabled = true;
			showFPS.isOn = true;
		} else {
			Camera.main.GetComponent<FPS> ().enabled = false;
			showFPS.isOn = true;
		}

		quality.value = QualitySettings.GetQualityLevel();

		yield return new WaitForEndOfFrame ();selections.SetActive (false);
	}


	public void SetEffect()
	{
		Camera.main.GetComponent<MobileColorGrading>().enabled = effect.isOn;

		if (effect.isOn)
			PlayerPrefs.SetInt ("PostEffect", 1);
		else
			PlayerPrefs.SetInt ("PostEffect", 0);
	}

	public void SetQuality()
	{
		QualitySettings.SetQualityLevel(quality.value);
	}

	public void SetBloom()
	{
		Camera.main.GetComponent<Kino.Bloom>().enabled = bloom.isOn;
		if (bloom.isOn)
			PlayerPrefs.SetInt ("Bloom", 1);
		else
			PlayerPrefs.SetInt ("Bloom", 0);
	}
	public void SetFPS()
	{
		Camera.main.GetComponent<FPS>().enabled = showFPS.isOn;
		if (showFPS.isOn)
			PlayerPrefs.SetInt ("FPS", 1);
		else
			PlayerPrefs.SetInt ("FPS", 0);
	}
	public void ToggleObject(GameObject target)
	{
		target.SetActive(!target.activeSelf);
	}


	public void LoadURL(string name)
	{
		Application.OpenURL (name);
	}
}
