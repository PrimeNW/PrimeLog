# PrimeLog - 1.8.x Spigot Plugin

Sunucu içi olayları Discord'a loglar ve Discord üzerinden konsol komutu çalıştırmanı sağlar.

## Özellikler
- Giriş/çıkış, chat, ölüm, komut kullanımı, kick/ban, opsiyonel blok kırma/koyma loglaması (embed olarak)
- Discord'daki belirli bir kanala yazılan mesajları sunucu konsolunda komut olarak çalıştırma
- Komut çıktısının Discord'a geri gönderilmesi
- Yalnızca izin verilen Discord kullanıcı ID'lerinin konsol komutu çalıştırabilmesi (güvenlik için)

## Derleme
```
mvn clean package
```
`target/PrimeLog.jar` dosyasını `plugins/` klasörüne atman yeterli. (JDA shade ile jar içine gömülüyor, ekstra bir şey yüklemene gerek yok.)

## Discord Bot Kurulumu
1. https://discord.com/developers/applications adresinden yeni bir uygulama oluştur.
2. "Bot" sekmesinden bot ekle, tokenı kopyala.
3. "MESSAGE CONTENT INTENT" seçeneğini AÇIK yap (konsol komutlarını okuyabilmesi için zorunlu).
4. OAuth2 > URL Generator'dan `bot` scope'u ve gerekli izinleri (Send Messages, Read Message History, Embed Links) seçip botu sunucuna davet et.
5. Discord'da log kanalı ve konsol kanalı oluştur, ikisinin de kanal ID'sini kopyala (Discord ayarlarından "Geliştirici Modu"nu aç, kanala sağ tık > ID Kopyala).

## config.yml Ayarları
- `bot-token`: Discord bot tokenın (KİMSEYLE PAYLAŞMA)
- `log-channel-id`: Olayların loglanacağı kanal ID
- `console-channel-id`: Komut yazılacak kanal ID
- `authorized-console-users`: Konsol komutu çalıştırabilecek Discord kullanıcı ID listesi
- `sadece-yetkili-idler-calissin`: true ise sadece listedeki kişiler komut çalıştırabilir (GÜVENLİK İÇİN true BIRAKMANI ÖNERİRİM)

## Güvenlik Notları (Prime için önemli)
- Botun tokenını asla paylaşma, sızarsa hemen Discord Developer Portal'dan reset'le.
- `sadece-yetkili-idler-calissin: true` bırak, sadece kendi Discord ID'ni ekle.
- Konsol kanalını gizli/özel bir kanal yap, herkesin görebileceği bir kanal olmasın.
- Bot'a Discord sunucusunda gereğinden fazla yetki verme (sadece mesaj gönderme/okuma yeterli).

## Komutlar
- `/primelog reload` - config'i ve bot bağlantısını yeniden yükler (yetki: `primelog.admin`, varsayılan: op)
