package edu.nyp.deafapp.Model;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Ipa {

    private int ipaId;
    private String ipaContent;

    public Ipa(int ipaId, String ipaContent) {
        this.ipaId = ipaId;
        this.ipaContent = ipaContent;
    }

    public int getIpaId() {
        return ipaId;
    }

    public void setIpaId(int ipaId) {
        this.ipaId = ipaId;
    }

    public String getIpaContent() {
        return ipaContent;
    }

    public void setIpaContent(String ipaContent) {
        this.ipaContent = ipaContent;
    }
}
