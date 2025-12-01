package com.connectasistemas.framework.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 
 * Hash de senhas para armazenamento, e teste de senhas contra tokens de senha.
 *
 * Instâncias desta classe podem ser usadas simultaneamente por múltiplas
 * threads.
 * 
 * OBS: Código retirado de https://stackoverflow.com/a/2861125/3474
 *
 * @author erickson
 * @see <a href="http://stackoverflow.com/a/2861125/3474">StackOverflow</a>
 */
public class PasswordAuthentication {

    // Cada token produzido por esta classe usa este identificador como prefixo.
    public static final String ID = "$31$";

    // O custo mínimo recomendado, usado por padrão
    public static final int DEFAULT_COST = 16;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    private static final int SIZE = 128;

    private static final Pattern layout = Pattern.compile("\\$31\\$(\\d\\d?)\\$(.{43})");

    private final SecureRandom random;

    private final int cost;

    // Cria um autenticador de senha com o custo padrão
    public PasswordAuthentication() {
        this(DEFAULT_COST);
    }

    /**
     * Cria um gerenciador de senhas com um custo especificado
     *
     * @param cost o custo computacional exponencial do hash da senha, de 0 a 30
     */
    public PasswordAuthentication(int cost) {
        iterations(cost); /* Valida o custo */
        this.cost = cost;
        this.random = new SecureRandom();
    }

    private static int iterations(int cost) {
        if ((cost < 0) || (cost > 30))
            throw new IllegalArgumentException("cost: " + cost);
        return 1 << cost;
    }

    /**
     * Gera o hash de uma senha para armazenamento.
     *
     * @return um token seguro de autenticação para ser armazenado e usado depois
     */
    public String hash(char[] password) {
        byte[] salt = new byte[SIZE / 8];
        random.nextBytes(salt);
        byte[] dk = pbkdf2(password, salt, 1 << cost);
        byte[] hash = new byte[salt.length + dk.length];
        System.arraycopy(salt, 0, hash, 0, salt.length);
        System.arraycopy(dk, 0, hash, salt.length, dk.length);
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        return ID + cost + '$' + enc.encodeToString(hash);
    }

    /**
     * Autentica comparando uma senha com o token armazenado.
     *
     * @return true se a senha e o token corresponderem
     */
    public boolean authenticate(char[] password, String token) {
        Matcher m = layout.matcher(token);
        if (!m.matches())
            throw new IllegalArgumentException("Invalid token format");
        int iterations = iterations(Integer.parseInt(m.group(1)));
        byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
        byte[] salt = Arrays.copyOfRange(hash, 0, SIZE / 8);
        byte[] check = pbkdf2(password, salt, iterations);
        int zero = 0;
        for (int idx = 0; idx < check.length; ++idx)
            zero |= hash[salt.length + idx] ^ check[idx];
        return zero == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
            return f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Invalid SecretKeyFactory", ex);
        }
    }

    /**
     * Gera o hash de uma senha vinda de uma {@code String} imutável.
     *
     * <p>
     * Senhas devem ser armazenadas em um {@code char[]} para que possam ser
     * preenchidas com zeros após o uso, evitando ficarem na heap ou em outros
     * lugares.
     *
     * @deprecated Use {@link #hash(char[])} no lugar
     */
    @Deprecated
    public String hash(String password) {
        return hash(password.toCharArray());
    }

    /**
     * Autentica usando uma senha em {@code String} e um token armazenado.
     *
     * @deprecated Use {@link #authenticate(char[],String)} no lugar.
     * @see #hash(String)
     */
    @Deprecated
    public boolean authenticate(String password, String token) {
        return authenticate(password.toCharArray(), token);
    }

}
