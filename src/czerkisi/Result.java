package czerkisi;

public enum Result {
    /**
     * if the letter is in the right spot
     */
    EXACT,

    /**
     * if the letter is in a different spot
     */
    CONTAINS,

    /**
     * if the letter is not in the word
     */
    MISS
}
