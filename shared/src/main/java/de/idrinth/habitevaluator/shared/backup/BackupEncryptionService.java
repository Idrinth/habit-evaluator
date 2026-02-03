package de.idrinth.habitevaluator.shared.backup;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Provides AES-256-GCM encryption and decryption for backup data.
 * Uses PBKDF2WithHmacSHA256 for key derivation from a user-provided password.
 */
public class BackupEncryptionService {

    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_SPEC_ALGORITHM = "AES";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int PBKDF2_ITERATIONS = 210000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts the given plaintext bytes using AES-256-GCM with a key derived from the password.
     * The output format is: salt (16 bytes) + IV (12 bytes) + ciphertext (includes GCM tag).
     *
     * @param plaintext the data to encrypt
     * @param password  the user-provided password or passphrase
     * @return the encrypted data with salt and IV prepended
     * @throws BackupException if encryption fails
     */
    public byte[] encrypt(byte[] plaintext, String password) throws BackupException {
        try {
            byte[] salt = new byte[SALT_LENGTH_BYTES];
            secureRandom.nextBytes(salt);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            SecretKey key = deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plaintext);

            byte[] result = new byte[SALT_LENGTH_BYTES + IV_LENGTH_BYTES + ciphertext.length];
            System.arraycopy(salt, 0, result, 0, SALT_LENGTH_BYTES);
            System.arraycopy(iv, 0, result, SALT_LENGTH_BYTES, IV_LENGTH_BYTES);
            System.arraycopy(ciphertext, 0, result, SALT_LENGTH_BYTES + IV_LENGTH_BYTES, ciphertext.length);

            return result;
        } catch (Exception e) {
            throw new BackupException("Failed to encrypt backup data", e);
        }
    }

    /**
     * Decrypts the given encrypted data using AES-256-GCM with a key derived from the password.
     * Expects the input format: salt (16 bytes) + IV (12 bytes) + ciphertext (includes GCM tag).
     *
     * @param encryptedData the encrypted data with salt and IV prepended
     * @param password      the user-provided password or passphrase
     * @return the decrypted plaintext bytes
     * @throws BackupException if decryption fails (wrong password or corrupted data)
     */
    public byte[] decrypt(byte[] encryptedData, String password) throws BackupException {
        try {
            if (encryptedData.length < SALT_LENGTH_BYTES + IV_LENGTH_BYTES + GCM_TAG_LENGTH_BITS / 8) {
                throw new BackupException("Encrypted data is too short to be valid");
            }

            byte[] salt = new byte[SALT_LENGTH_BYTES];
            System.arraycopy(encryptedData, 0, salt, 0, SALT_LENGTH_BYTES);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(encryptedData, SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES);

            int ciphertextLength = encryptedData.length - SALT_LENGTH_BYTES - IV_LENGTH_BYTES;
            byte[] ciphertext = new byte[ciphertextLength];
            System.arraycopy(encryptedData, SALT_LENGTH_BYTES + IV_LENGTH_BYTES, ciphertext, 0, ciphertextLength);

            SecretKey key = deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            return cipher.doFinal(ciphertext);
        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            throw new BackupException("Failed to decrypt backup data. Wrong password or corrupted file.", e);
        }
    }

    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, KEY_SPEC_ALGORITHM);
    }
}
