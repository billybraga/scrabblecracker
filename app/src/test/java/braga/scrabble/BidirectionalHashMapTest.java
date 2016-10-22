package braga.scrabble;

import junit.framework.Assert;

import org.junit.Test;

import braga.utils.BidirectionalHashMap;

/**
 * Created by Billy on 10/21/2016.
 */

public class BidirectionalHashMapTest {
    @Test
    public void tests() {
        BidirectionalHashMap<String, String> hashMap = new BidirectionalHashMap<>();

        hashMap.put("1", "2");

        Assert.assertEquals("2", hashMap.get("1"));
        Assert.assertEquals("1", hashMap.getKey("2"));

        hashMap.remove("1");
        Assert.assertEquals(0, hashMap.size());
        Assert.assertNull(hashMap.getKey("2"));
    }
}
