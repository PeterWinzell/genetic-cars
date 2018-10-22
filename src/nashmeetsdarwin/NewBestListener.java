/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nashmeetsdarwin;

import java.util.ArrayList;

/**
 *
 * @author pwinzell
 */
public interface NewBestListener {
    void setNewBest(ArrayList<Sprite> sprites,long generation,double fitnessvalue);
}
