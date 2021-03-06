package org.languagetool.rules.uk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.languagetool.AnalyzedToken;

/**
 * @since 3.6
 */
class InflectionHelper {

  private InflectionHelper() {
  }

  static class Inflection implements Comparable<Inflection> {
    final String gender;
    final String _case;
    final String animTag;
  
    Inflection(String gender, String _case, String animTag) {
      this.gender = gender;
      this._case = _case;
      this.animTag = animTag;
    }
  
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_case == null) ? 0 : _case.hashCode());
      result = prime * result + ((animTag == null) ? 0 : animTag.hashCode());
      result = prime * result + ((gender == null) ? 0 : gender.hashCode());
      return result;
    }
  
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
  
      Inflection other = (Inflection) obj;
      return gender.equals(other.gender)
          && _case.equals(other._case)
          && (animTag == null || other.animTag == null 
          || ! animMatters() || ! other.isAnimalSensitive() || animTag.equals(other.animTag));
    }
  
    public boolean equalsIgnoreGender(Inflection other) {
      return //gender.equals(other.gender)
          _case.equals(other._case)
          && (animTag == null || other.animTag == null 
          || ! animMatters() || animTag.equals(other.animTag));
    }
  
    boolean animMatters() {
      return _case.equals("v_zna") && isAnimalSensitive();
    }
  
    private boolean isAnimalSensitive() {
      return "mp".contains(gender);
    }
  
    @Override
    public String toString() {
      return ":" + gender + ":" + _case
          + (animMatters() ? "_"+animTag : "");
    }

    @Override
    public int compareTo(Inflection o) {
      int compared = GEN_ORDER.get(gender).compareTo(GEN_ORDER.get(o.gender));
      if( compared != 0 )
        return compared;
      
      compared = VIDM_ORDER.get(_case).compareTo(VIDM_ORDER.get(o._case));
      return compared;
    }
  
  }

  static List<Inflection> getAdjInflections(List<AnalyzedToken> adjTokenReadings) {
    List<Inflection> masterInflections = new ArrayList<>();
    for (AnalyzedToken token: adjTokenReadings) {
      String posTag = token.getPOSTag();
  
      if( posTag == null || ! posTag.startsWith("adj") )
        continue;
  
      Matcher matcher = TokenInflectionAgreementRule.ADJ_INFLECTION_PATTERN.matcher(posTag);
      matcher.find();
  
      String gen = matcher.group(1);
      String vidm = matcher.group(2);
      String animTag = null;
      if (matcher.group(3) != null) {
        animTag = matcher.group(3).substring(2);	// :rinanim/:ranim
      }
  
      masterInflections.add(new Inflection(gen, vidm, animTag));
    }
    return masterInflections;
  }

  static List<Inflection> getNounInflections(List<AnalyzedToken> nounTokenReadings) {
    List<Inflection> slaveInflections = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 == null )
        continue;
  
      Matcher matcher = TokenInflectionAgreementRule.NOUN_INFLECTION_PATTERN.matcher(posTag2);
      if( ! matcher.find() ) {
        //  			System.err.println("Failed to find slave inflection tag in " + posTag2 + " for " + nounTokenReadings);
        continue;
      }
      String gen = matcher.group(2);
      String vidm = matcher.group(3);
      String animTag = matcher.group(1);
  
      slaveInflections.add(new Inflection(gen, vidm, animTag));
    }
    return slaveInflections;
  }

  private static final Map<String,Integer> GEN_ORDER = new HashMap<>();
  private static final Map<String,Integer> VIDM_ORDER = new HashMap<>();
  
  static {
    GEN_ORDER.put("m", 0);
    GEN_ORDER.put("f", 1);
    GEN_ORDER.put("n", 3);
//    GEN_ORDER.put("s", 4);
    GEN_ORDER.put("p", 5);

    VIDM_ORDER.put("v_naz", 10);
    VIDM_ORDER.put("v_rod", 20);
    VIDM_ORDER.put("v_dav", 30);
    VIDM_ORDER.put("v_zna", 40);
    VIDM_ORDER.put("v_oru", 50);
    VIDM_ORDER.put("v_mis", 60);
    VIDM_ORDER.put("v_kly", 70);
  }

}
