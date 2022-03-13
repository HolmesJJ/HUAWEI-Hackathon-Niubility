package edu.nyp.deafapp.Model;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class AudioUploadResult {

    private static boolean isUploadResult = false;

    public static boolean isUploadResult() {
        return isUploadResult;
    }

    public static void setUploadResult(boolean upload) {
        isUploadResult = upload;
    }
}
