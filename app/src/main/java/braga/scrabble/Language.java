package braga.scrabble;

/**
 * Created by Billy on 10/20/2016.
 */

public class Language {
    private String key;
    private String name;

    public Language(String key, String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
