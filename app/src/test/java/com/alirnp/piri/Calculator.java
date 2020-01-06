package com.alirnp.piri;

import org.junit.Test;

import static org.junit.Assert.*;

public class Calculator {


    @Test
    public void testCalculatorForDiv(){

        assertEquals(2,div(4,2));

        try{
            int d = div(6,0);
            fail();
        }catch (Exception e){
            assertTrue(e instanceof ArithmeticException);
        }
    }

    private int div(int num1 , int num2){
        return num1 / num2 ;
    }
}
