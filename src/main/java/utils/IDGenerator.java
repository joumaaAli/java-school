/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author jouma
 */
import java.util.UUID;

public class IDGenerator {
    public static String generateID() {
        return UUID.randomUUID().toString();
    }
}