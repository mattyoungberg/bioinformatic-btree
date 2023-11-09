package cs321.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class DNASerializerTest {

    @Test
    public void testEncode_A() {
        assertEquals(0b00, DNASerializer.encode('A'));
    }

    @Test
    public void testEncode_a() {
        assertEquals(0b00, DNASerializer.encode('a'));
    }

    @Test
    public void testEncode_T() {
        assertEquals(0b11, DNASerializer.encode('T'));
    }

    @Test
    public void testEncode_t() {
        assertEquals(0b11, DNASerializer.encode('t'));
    }

    @Test
    public void testEncode_C() {
        assertEquals(0b01, DNASerializer.encode('C'));
    }

    @Test
    public void testEncode_c() {
        assertEquals(0b01, DNASerializer.encode('c'));
    }

    @Test
    public void testEncode_G() {
        assertEquals(0b10, DNASerializer.encode('G'));
    }

    @Test
    public void testEncode_g() {
        assertEquals(0b10, DNASerializer.encode('g'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncode_invalid() {
        DNASerializer.encode('N');
    }

    @Test
    public void testDecode_A() {
        assertEquals('a', DNASerializer.decode((byte) 0b00));
    }

    @Test
    public void testDecode_T() {
        assertEquals('t', DNASerializer.decode((byte) 0b11));
    }

    @Test
    public void testDecode_C() {
        assertEquals('c', DNASerializer.decode((byte) 0b01));
    }

    @Test
    public void testDecode_G() {
        assertEquals('g', DNASerializer.decode((byte) 0b10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_invalid() {
        DNASerializer.decode((byte) 0b100);
    }
}
