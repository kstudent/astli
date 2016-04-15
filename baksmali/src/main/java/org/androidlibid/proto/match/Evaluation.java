package org.androidlibid.proto.match;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class Evaluation {
    
    private Position position = Position.OK;
    private Classification classification = Classification.TRUE_POSITIVE;
    
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }
    
     /**
    
    |-------------------+------------------------------------------------------|
    | OK                | if match by name was found and position = 1          |
    |-------------------+------------------------------------------------------|
    | NOT FIRST         | if match by name was found and position > 1          |
    |-------------------+------------------------------------------------------|
    | NOT IN CANDIDATES | if match by name was found, but not in candidate set |
    |-------------------+------------------------------------------------------|
    | NO MATCH BY NAME  | if match by name was not found. reasons:             |
    |                   | - lib of this package was not fingerprinted          |
    |                   | - package contains application code                  |
    |-------------------+------------------------------------------------------|
     
     */    

    public enum Position {
        OK,
        NOT_FIRST,
        NOT_IN_CANDIDATES,
        NO_MATCH_BY_NAME;
    }
    
    //first                                                                     -> true  positive
    //(not_first || not_in_candidates) &&  matchInDB() &&  thereAreCandidates() -> false positive
    //(not_first || not_in_candidates) && !matchInDB() && !thereAreCandidates() -> true  negative
    //(not_first || not_in_candidates) &&  matchInDB() && !thereAreCandidates() -> false negative
    public enum Classification {
        TRUE_POSITIVE,
        TRUE_NEGATIVE,
        FALSE_POSITIVE,
        FALSE_NEGATIVE;
    }
}
