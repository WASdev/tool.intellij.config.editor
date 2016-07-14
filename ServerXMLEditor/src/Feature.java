/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

import java.util.ArrayList;

/**
 * Created by John Collier & Logan Kember on 5/31/2016.
 * Last updated: 6/2/2016
 */
public class Feature {
    private String featureName;
    private String name;
    private String description;
    private ArrayList<String> enables = new ArrayList<>();
    private ArrayList<String> enabledBy = new ArrayList<>();

    /**
     * Creates a feature object with the given name and description
     * @param featureName The name of the feature
     * @param description The description of the feature
     */
    public Feature(String featureName, String description) {
        this.featureName = featureName;
        this.description = description;
    }

    public String getFeatureName() {
        return this.featureName;
    }

    public String getName() { return this.name; }

    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Feature){
            Feature toCompare = (Feature) o;
            return this.featureName.equals(toCompare.featureName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(featureName);
    }

    public void addEnables(String enables) {
        this.enables.add(enables);
    }

    public void addEnabledBy(String enabledBy) {
        this.enabledBy.add(enabledBy);
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getEnabledBy() { return this.enabledBy; }

    public ArrayList<String> getEnables() { return this.enables; }

    /**
     * Retyrbs a string that shows all of the features that are enabled by this feature
     * @return a String representation of the features stored in the "enables" ArrayList
     */
    public String enablesToString() {
        if (enables.size() == 0) {
            return "Does not enable any other features.";
        }
        String output = enables.get(0);
        for (int i=1; i<enables.size(); i++) {
            output = output.concat(", " + enables.get(i));
        }
        return output;
    }

    /**
     * Returns a string that shows all of the features that enable this feature
     * @return a String representation of the features that are stored in the "enabledBy" ArrayList
     */
    public String enabledByToString() {
        if(enabledBy.size() == 0) {
            return "Not enabled by any other features.";
        }
        String output = enabledBy.get(0);
        for (int i=1; i<enabledBy.size();i++) {
            output = output.concat(", "+enabledBy.get(i));
        }
        return output;
    }
}
