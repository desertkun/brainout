package com.desertkun.brainout;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.KryoNetworkClient;
import com.desertkun.brainout.online.NetworkClient;
import com.desertkun.brainout.online.NetworkConnectionListener;
import com.esotericsoftware.kryo.Kryo;

import java.io.File;

public class ServerEnvironment extends Environment
{
    private static final Array<String> BOT_NAMES;

    static
    {
        BOT_NAMES = new Array<>(new String[]{
            "Сергей", "Петр", "Александр", "Алексей", "Никита", "Иван", "Глеб ", "Тимур ", "Владимир", "Всеволод",
            "Юрий", "Константин", "Вова Вист", "Шустрый", "Толян", "Клык", "Косяк", "Гера", "Кастет", "Череп",
            "Григорий Гиря", "Санитар", "Псих", "Флюгер", "Молодой", "Ватсон", "Туз", "Витя", "Батон", "Кривой",
            "Кабан", "Пахан", "Аркашка", "Баклан", "Бивень", "Борода", "Бугор", "Валет", "Гаррик", "Дотман",
            "Дядька Яр", "Жиган", "Жоржик", "Зелень", "Калган", "Карась", "Катала", "Козырь", "Кольщик",
            "Крутой", "Малой", "Мокрушник", "Мутный", "Орешек", "Орел", "Парафин", "Пастух", "Пес", "Поросенок",
            "Порученец", "Ржавый", "Седой", "Следак", "Сохатый", "Сявка", "Фазан", "Фраер", "Фуфлыжник", "Шнырь",
            "Шпалер", "Щегол", "Юрик", "Якорник",

            "Kurz", "Winston", "Matthew", "Jan", "Curt", "Eric", "Henry", "Walter", "John", "Michael", "Arnold",
            "Kyle", "Jacob", "Joshua", "Matthew", "Andrew", "William", "Ryan", "Nicholas", "Alexander", "Tyler",
            "James", "Nathan", "Samuel", "Logan", "Justin", "Kevin", "Thomas", "Connor", "Jack", "Cameron", "Luis",
            "Eric", "Jesus", "Cody", "Jake", "Patrick", "Jackson", "Jason"
        });
    }

    public Array<String> getBotNames()
    {
        return BOT_NAMES;
    }

    @Override
    public String getUniqueId()
    {
        return null;
    }

    @Override
    public String getExternalPath(String from)
    {
        return from;
    }

    @Override
    public File getFile(String path)
    {
        return new File(path);
    }

    @Override
    public NetworkClient newNetworkClient(Kryo kryo, NetworkConnectionListener listener)
    {
        return new KryoNetworkClient(kryo, listener);
    }

    public boolean checkPurchaseLimit(PlayerClient playerClient)
    {
        return true;
    }

    public void confirmPurchase(PlayerClient playerClient)
    {
        //
    }
}
