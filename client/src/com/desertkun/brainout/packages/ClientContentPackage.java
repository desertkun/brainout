package com.desertkun.brainout.packages;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.utils.Base64;
import com.desertkun.brainout.utils.StringFunctions;

import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.ZipEntry;

public class ClientContentPackage extends ZipContentPackage
{
    protected String textures;
    protected String localization;
    protected String skin;

    public String getSkin()
    {
        return skin;
    }

    public String getTextures()
    {
        return textures;
    }

    public String getLocalization()
    {
        return localization;
    }

    public ClientContentPackage(String name) throws ValidationException
    {
        super(name);
    }

    @Override
    public void completeLoad(ContentBoundAssetManager bound)
    {
        super.completeLoad(bound);

        if (getSkin() != null)
        {
            JsonValue value = loadItem(getSkin());
            BrainOutClient.Skin.load(value);
        }
    }

    public void loadTextures(AssetManager assetManager)
    {
        if (getTextures() != null)
        {
            JsonValue value = loadItem(getTextures());
            BrainOutClient.TextureMgr.loadAtlases(value, assetManager);
        }
    }

    @Override
    protected int getContentRank(Content c)
    {
        if (c instanceof SoundEffect)
        {
            return 5;
        }

        return super.getContentRank(c);
    }

    @Override
    public boolean validate()
    {
        if (zip == null)
            return false;

        if (BrainOutClient.getInstance().unsafe)
            return true;

        ZipEntry hashesEntry = zip.getEntry("__H");
        ZipEntry signatureEntry = zip.getEntry("__S");

        if (hashesEntry == null || signatureEntry == null)
            return false;

        InputStream hashesStream;
        InputStream signatureStream;

        try
        {
            hashesStream = zip.getInputStream(hashesEntry);
            signatureStream = zip.getInputStream(signatureEntry);
        }
        catch (Exception e)
        {
            return false;
        }

        KeyFactory kf;

        try
        {
            kf = KeyFactory.getInstance("RSA");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return false;
        }

        PublicKey publicKey;
        try
        {
            publicKey = kf.generatePublic(
                new X509EncodedKeySpec(Base64.decode(ClientConstants.Security.PUBLIC_KEY.getBytes())));

        } catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
            return false;
        }

        String hashes = StringFunctions.StringFromInputStream(hashesStream);
        String b64Signature = StringFunctions.StringFromInputStream(signatureStream);

        Signature signature;
        try
        {
            signature = Signature.getInstance("SHA256withRSA");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return false;
        }

        try
        {
            signature.initVerify(publicKey);
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
            return false;
        }

        try
        {
            signature.update(hashes.getBytes());
        }
        catch (SignatureException e)
        {
            e.printStackTrace();
            return false;
        }

        boolean result;

        byte[] b64sign = Base64.decode(b64Signature.getBytes());

        if (b64sign == null)
            return false;

        try
        {
            result = signature.verify(b64sign);
        }
        catch (SignatureException e)
        {
            e.printStackTrace();
            return false;
        }

        if (!result)
            return false;

        JsonReader jsonReader = new JsonReader();
        JsonValue hashesValue;

        try
        {
            hashesValue = jsonReader.parse(hashes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        if (!hashesValue.isObject())
            return false;

        for (JsonValue file: hashesValue)
        {
            String fileName = file.name();
            long fileHash = file.asLong();

            ZipEntry entry = zip.getEntry(fileName);

            if (entry == null)
                return false;

            if (entry.getCrc() != fileHash)
                return false;
        }

        return super.validate();
    }

    public void texturesLoaded(AssetManager assetManager)
    {
        if (getTextures() != null)
        {
            JsonValue value = loadItem(getTextures());

            if (value != null)
            {
                BrainOutClient.TextureMgr.registerAtlases(value, assetManager);
            }
        }
    }

    private static Json JSON;

    static
    {
        JSON = new Json();
        BrainOut.R.tag(JSON);
    }

    @Override
    public void loadContentHeader()
    {
        super.loadContentHeader();

        if (contentValue != null)
        {
            textures = contentValue.getString("textures", null);
            localization = contentValue.getString("localization", null);
            skin = contentValue.getString("skin", null);

            if (contentValue.has("localization"))
            {
                String localizationFile = contentValue.getString("localization");
                JsonValue value = loadItem(localizationFile);

                if (value != null)
                {
                    BrainOut.LocalizationMgr.read(JSON, value);
                    BrainOut.LocalizationMgr.update();
                }
            }
        }
    }
}
