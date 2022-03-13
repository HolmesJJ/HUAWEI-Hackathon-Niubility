using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Billboard : MonoBehaviour {

	Transform target;
	void Start () {
			target = Camera.main.transform;


	}
	

	void Update () {
		transform.LookAt (target.position);
	}
}
