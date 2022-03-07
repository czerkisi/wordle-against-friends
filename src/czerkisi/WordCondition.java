package czerkisi;

@FunctionalInterface
public interface WordCondition {
    boolean testCondition(String word);
}
