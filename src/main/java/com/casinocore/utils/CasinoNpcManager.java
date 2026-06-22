package com.casinocore.utils;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.games.CasinoGame;
import com.casinocore.gui.GameBetGUI;
import com.casinocore.integrations.citizens.CasinoGameTrait;
import net.kyori.adventure.text.Component;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CasinoNpcManager {

    private static final Map<String, NpcPreset> CITIZENS_PRESETS = Map.ofEntries(
        Map.entry("coinflip", new NpcPreset("Coin Flip Dealer", "coinflip", "", "")),
        Map.entry("dice", new NpcPreset("Dice Dealer", "dice", "ewogICJ0aW1lc3RhbXAiIDogMTYyMTI4MDYyMDc0MSwKICAicHJvZmlsZUlkIiA6ICJiMGQ3MzJmZTAwZjc0MDdlOWU3Zjc0NjMwMWNkOThjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJPUHBscyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83ZTU5Njg2OGM2YjExM2IxMTAxZmIyMmU4NGFjMGQ1NjQwZmM2OTlkNWIyOWI0ODU5NTE2ZDJmZjFkZDNmYTcwIgogICAgfQogIH0KfQ==", "F9CtolPqRM49CBKZNpc80FdGNbgxHhnRr5tWEky0qCYgyNHXjCQaDR7FrbchFMzxz9+YoiAmkGSmVqzfviInLCeogsYYhPj+wu7eXnuNqK1QYXbx4tMnsNbdcOxw9e3HGphJ1fZmEHqLVHVUEcLo2cPs5HUGH/4kxYxKOsiE8Jb0ZHSGutw12GBX3hPsl+ihQ5u/kxwk+Q17RrttjLbg1ou3e3Qqhk6sugIujJyph6MIMX+O2pdGB6kWRJcMkTmAnhJD7nkkxPu0/FqfQDSVA0KLEas4WvzYtX/0VKBiIZ4SJ2OEySNq5jv8vUT24Sy5CnUFtZjIt9xBUTTAybw/kHImBLW9rayDi6o5FV8ZdCBb7vhQ2YKi0N4eNsM1Ttg562EoPzDiZbyS/A6/qjC4he2f2trO+vTIwNoVxkHAKOwkp7FtYg0i/8gtzhfreNtZtUqoS3Yft0iderwshrM02EJWWZqcq21O4iA8h1pB7QXmK2kL8sO+YmeZCJf6oBlo4IiKZkVIQkIUsnlabOBHZW0jBsmCDJsRWi4OAq1XYGd6/ThZeYpGbFxpCaq75VyrabwViR6WBg1wHzzegRHpfdGGf605WOLiaOISr9ROMrnq0l208h8dV33w1XdTTCrO+5/nGxzriyIfLUGx57MMV+65vSZvJc8mLFpuIHnZwdM=")),
        Map.entry("lottery", new NpcPreset("Lottery Dealer", "lottery", "ewogICJ0aW1lc3RhbXAiIDogMTYyNDMyNTg4MTYxNywKICAicHJvZmlsZUlkIiA6ICI4YmM3MjdlYThjZjA0YWUzYTI4MDVhY2YzNjRjMmQyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3RpbnZlbnRpdmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjI3ODRiZWJkMGYxMjc2N2M4MWI5YjE3MGE0YTFhOWY1ZjM4MDE5MjY1NWI2MTVkZWY3MzZlZGExZTk5MTI0YiIKICAgIH0KICB9Cn0=", "RzLw/FUGxWDbY8qcMGM5+Akcua1h81Itz6xEWQWlbJGpJNbXHJcCaT3W0u2muVgBctjsInz2vTRxtKMF+BJOyo7m+Uipd0jzVyvzpbeRRNio2JPGWlOluUqaCdYkzPTtBLgyaoU93pHiuAhkkqFwwvJquhXQA2znk0ANlAJShUCBfvYXihDpo8RV/gnFUVa0nQUYt58Iq0ghhXMJ3a75l1J/ErDhZfr/LrLG78CUt49Y+BAAkEkpbNATBYPdXxOI9E1PHoF6VDLfc90M4N3ufIHbhDldATkmGCWoHfe7tg/F5rMjrrkCq5mbu6PRyBCqSY0SIVkqpEiqBj10ZHx43W1LiSV1T0JwrmvAuI2XGUQAeibsZh56k0A4kyRSYQFSnjc3lhwFVb6zxqkafgnEPaiSsQrKE2YfyEF+JwPe6UQuGsRyxwCb91GUijCH/Cjj2njFGjbO/HsSL9Lyid5Rb3db6/krZwSXkcL8Jl2d4BK/TtsPspNEBjHfTyH5baI+8idMzGJxgS70fPqmegseGfLocnn25VyyB4aO0xFIiIZMke30TAhiHecI3FoljPZoI3y7BuxQoVQQ4h/TqNDAw5RbOW6mLAgpa+dFAzkQvOyLoG45X9uABmWb5ueUriQmiUjzmvSL2/gBEegJdSzEaS4eoh90YSfc4hlh4O2AKpA=")),
        Map.entry("blackjack", new NpcPreset("Blackjack Dealer", "blackjack", "ewogICJ0aW1lc3RhbXAiIDogMTc0NTk3NDQ3NjMxNiwKICAicHJvZmlsZUlkIiA6ICJmYjZkM2E5Zjk3MWY0ZTdlYmQ0MjE2Yjk0MjE5NDA3NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkZW5BcHBsZUdIRyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNDRhMDA2YmVkZDNmOWQ0NTZmMGFlOTAxMTRjMDEwOTg0NWYxZWM1YWI1Yzg5MzI3YjZmYmMxZWNhYmRkNTQ3IgogICAgfQogIH0KfQ==", "w9ikgneDSqao+J8Kk4lIOy/cLUXso3lD3660t80r2Sd+HYJFRrKoKnx3mA4eNLbGLcmcUXLBqap3dbxojwweW12W5in2yLiLEjABx+6Awz62YJgAnf8+rHSLkFN3JDTiSRLY6rGraPP1cJMxFy6pKmYNyw4hfqN90WPoK1jSgzDsOvwHPtSCoJcf48Nw+1Q6vl2K6DXM9xMUVh6+BX/LSirsWMbCeUKq708RBaYvj4efdCW9jApsBTUsNrZjXsdkfMjccsjBCbc31qsJZWFvJMi/ol7S0HGPJZBgS8N+d7WHPEknJbjRRitfHAI2vOSDdOpJMEaNjXaUz12fipNrayrFMfS8Q5RSeNPAYTJMWqRUExSer4wT8XL3QtI1tvAQG1vNYlkihb46wmi48jlnFB+t6BCmLx3mkIDCqgE9MLmc5q+88+YEgjMC9BEg3YnHvCP4zhYCj/8PjcSKxn6bQclNxvrD87vJRIDHfNKFeDTDtdYWJFVBgh6A1HmYI0nd+cQDfF/9rE46oio+qsGh97Ea1ZUEaNxXJz1HT6Hs8KkiQlPcDbfWKJOrUeO3CuogZCCoWTCzDzKj4jZ/XD/RmXvgV5M3ZXcUSeUOrXb2WS5ilpA0/ZnC+nY38JjdfuXEkYiLH78VGq7SnI6RwL0ZgNncZMB7FPusrrZM9JK9ouk=")),
        Map.entry("roulette", new NpcPreset("Roulette Dealer", "roulette", "ewogICJ0aW1lc3RhbXAiIDogMTYxNzk5MDQ5NjQ3NCwKICAicHJvZmlsZUlkIiA6ICJiMWMyNWQ0YjMwZDU0N2Y4YTk3NmZlYTllOGU1YzBjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJvd29FbmRlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83MTU5ODU0MzU1OTE0NmQ2ZmQ1ZmU3NzFhNTVlYjc0ZjQ3YjYwNTliOTczNzQwOGVmZWRmNDY2MDFhOGE5YzVlIgogICAgfQogIH0KfQ==", "jVwQkp6x/sQE4n2C+zDaJ3aOKdZc/evSVxXqHG9TxMLrV+z/Uf3hXlr8WwPYQ2KnCoJwGpFI/gVq6eir0SOcAOZq6trVY7dbcW1/HZkMBJ+dVmqpKzcfHV+yQtUBkEkSFNUDSQVsbvOmoYW/WA/RH24/bDfUxUnVbnxWrVm8M0efdBjgM/JinEfHCERuHXofY7ttEFuIEYHEcFZ+RyhsWX+co0RnpBN52BNPMG8gkcrs2hrdhTiz2L9xEYdlKO7bdQFZZDBcjdPgCZtqsQOHxVb34AXnQUmIQcG3x4SUlkkfPZhdbanMK95igfiowKPpkh1gkRfWvt59K0JxnVrwYXkwSFbDc5VKEoXHTfS8+nn9VNpmT4D8BVKMexa4KS5dhhQt41xZ1i4HXhMsAVe8qjG5KVhXxSgYN2aIIF3lIDGPjs0squTFkNmuhqaIZl1tkA3HU/MivY59ilT7Wy3ch8Rtt8pHSjwDQn1nwRku1Kzgob1hz5n/hCpmQiGC7C4v/peOMD6nusmaBL+vZsgRuuuvLKQl8rPI2USVs2eXz7JxPivtjgjij6hmhrai6lUPuMccF7txRPcbUFc9d/EROBZ1cwkZfeR0GEl5loddgiO5KIMyoMzQR+NMFYlJVocDAUg3nFWQuRW1hTzIMQRo5K8N5EKVFHbKxhjLlR1rfaw=")),
        Map.entry("slots", new NpcPreset("Slots Dealer", "slots", "", "")),
        Map.entry("horserace", new NpcPreset("Horse Race Dealer", "horserace", "ewogICJ0aW1lc3RhbXAiIDogMTYyNTYxNzk0NjY5NywKICAicHJvZmlsZUlkIiA6ICI0OWIzODUyNDdhMWY0NTM3YjBmN2MwZTFmMTVjMTc2NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJiY2QyMDMzYzYzZWM0YmY4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I0ZDllN2RhYWM4NTI0MWVlZDUzMzZkYzcxOWFkMTM4OTY2MWJhNWQ3NjY3YjNlMDViOTgwYWYzZjlmMDEyMjMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "l4yvwqSVuezFH1oreWhlgzfgS5D0C8LfN8RXNX8xliqlmRtiF5OniWrCxxXG2Shqj2d+ozHC95FXGfHcXtAH0++XPD04frfTjt1s/1XulN9YiOD3gzNKq9Inkua33PszRPrJ1D57FF6Rf6ffJlM3J0yKVJ8UyJmxEreST3N1XWYXWKewygeR2UOX8b0uz7L9/EhiFrZ4dpU66enKKxoe6vwWxt0bWXawTYpPaIPOTY9YCD2xfBllSE3XSXcWtDTnSx3EwpmaAHxLB7xNUYrVcbEm52PaYBQl9Qf2TGua/gXk/MMaHai0I1xiHdmqacP812qxg6Nla7CLamOh/Z6+Acva+H5XYfGZBKXIiwzeNe2zHL7LMWfSHxTMMQpFgWEGKuV+8HSuNXk2EX/WE+61ofIFeeKR6MneHmLMYJrLukvxd6uMcYbuEH0/UjC8TT0o1XKujwrPpqClCKvXrhD4JiyTJ1Rooh5DfrQ9jk3VPuSTi+kJw/CEnhGXcaK8uezWRxC5yQzEkMmXXvml0rp3dPkTUWpqH4zfzA5hNAsHmXev7nSIeByTZnolgQG2E98DQBolC3SvDjAiCyepw6r7EW2ZknluS/ubcXWaQG3TELt30XqNAXtj1gxFGMVooJ4BG7ij9M/qPUyD3iv8kfcIdS6ExPoQW5jpS2qM3izX/b4=")),
        Map.entry("wheel", new NpcPreset("Lucky Wheel Dealer", "wheel", "ewogICJ0aW1lc3RhbXAiIDogMTY2MTM4Njg0MjY0NSwKICAicHJvZmlsZUlkIiA6ICIxOTQzY2VmYzM4NWM0YTJjYWJiZGViODBjZTIwM2RjZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzb29vb29vb29vb29vb29wIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYyZTk2OWY0Y2ZjOTc3ZWY0NDdiMTgwOTE0NmJkNjBjZDRiZDU2ZTk4ZTU0MDQ1NTBmMDE2YWYzMTFjMDMwZSIKICAgIH0KICB9Cn0=", "v6YD+Noh0Uqz/aX9icN5BZfSVM7HjiF8Dvuq+S3L8GneAApqqdJUHcx6KOOGS5DTPuCa4aI54lUvcqSeG6KCIJgPtN8/HLrpF++SIwnlYBzLNBd2GBfGiCHEKtGS9xH1VHCe3Qpr8eGe04NlLSVkDmoPn5mp6c10eg6T9KQzeCdxIlh/2jxMTEgari75nPHVtgbNkSaFQj3IrgjxzZtKrK5O7jWxl9CdjTdP3NoiTPfgzuLb1te7++IiEAcWqzYaGDFzH/FulxNnQRJ3BtB3F2fqDvXD/J0D0el1SKVllUZdT+s7Q4ICs0Qedh+rKkVLmhfd5EtbuQmyB5knaXC+SHK6ZhBk5JNGbdKRsqUZKtWVmp85/qD993SrNeQdefySgeq6kqDAQR056/PZR+tuMMaPzohKsbL5I+j7lJpJFm8GC0azNjbetfQu5vFAlEfLEe6odZYQ3FFe9r6zJZuYV0kKutGVIRhjjU46U1jPU+IGwssTSuTLvHwsWqf7chZUeT8QPbSnzo1vYF6k58Uq29lCbCGLW77gnb3KHT/1PZEfW8CwPyRr7Q/7+ziu11lGx2O2rx7OU70lB27x8zq/2DfcZ7I3dmAlxgVnmQJ2i94H0pKOL0Te/FdPwnJZ9Ps4uyjetNIYsrzE3yFqNRtnR/33DqqwELNNGdwPsMnHm2U=")),
        Map.entry("highlow", new NpcPreset("High Low Dealer", "highlow", "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQzNDI5NDM0MCwKICAicHJvZmlsZUlkIiA6ICJmYjZkM2E5Zjk3MWY0ZTdlYmQ0MjE2Yjk0MjE5NDA3NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkZW5BcHBsZUdIRyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMGRkM2Q1MzNiMjYyM2Q0M2Y4YTAyNjg4YzRiNmIwMmFlYTM1N2E5YjEyYmI3N2UzMDlmODI5ZGY5NDFmY2ZjIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", "mGiB8OnnfiGStWYa4quCPs7FbHUfTJ/iM+/m5+EGqZnsoTfDFzxwFbAZc0hSpoSQRdpBK4IyjjLQB46jPd4WHsIHihdpwY1SJ8lKfnRz+GRZAPjkyWdYu8tdGyeWz539d0x4RbKCsguIHlaA7zewjaaiMjDB3ORcpMKxU7SRiiH/4nu1EYHZWhIDjv0+EWQRhyn23GDYe9dMjcJFkKZ0lXnBOEAGQG/Ys0zMJniUuBjMJ04Lk4aCXQcXKcBSn2RcKVs0D8qKZIWjdkeSCoWVnSVa0DhtDnWaaQOW8AoQ192j6C1B2AVew90jHcAHY2DS9ekLXiPkv/ddDe/qawANXOjRfq2vNDNA2xvSDbHTQn/OOzQ+YBlA+b7+6DBOQh+8tVPTuHXJlj8+y4lfX7pRYb+NOD2FP6WyUDKAyHRuN3Ilne9bBdyc/PPQJunVbO2wcFa1yLkCDMJEBthm2Q4fRIVEhRURapJCp7iBUsA9tASIhmIXczTu5J824ri8jnETzAAhpxwZZ4w6QnyP08lcVmCU2uGm/GnpVWln2Cx6Ik4qqsPvsLdoSuWWsK3GiMyL6r8GdFGZyTYVpP0m+3bYR00rbnsk0De33QZKUrc0f29HIT+FD3hgUaP2UIn9OPQocF3QYAH1D8nti32lWRoUgj+4cOA/a6clyZthJBoOoaE=")),
        Map.entry("doubleup", new NpcPreset("Double Up Dealer", "doubleup", "ewogICJ0aW1lc3RhbXAiIDogMTc2NjQ2NDI3NDU1MCwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2FkN2VmMGZmOTRjMWRmZGVjODAxNTAwMzIwMWVlZTRiMDI0YzBmNTA3ZDQ3ZWRiM2EzMGJmN2QyNzQyYzg1MyIKICAgIH0KICB9Cn0=", "SO83jQPZh0SETKdSliycF/lCZB1PpsJ7Uq2MlkBmwn5oMAUAB8OryPFFjT4KT+gxRGpp+8AIgUvPfs0vsJ5yYAFOvU8+RC3VZeSPo6TE1Lms2Tp94hr9Z5Osdlsf9LxZiDTkcZYcfJqsUkKjbW5wJDmWfW/Q219JgPX1LTWr/J7Xdxs/0S5j5X7cfU3ywKpuBTPPUtCeKbRIOXxUQolMRqJx7SLH+v96mc+gehp3rb+CNkmGtjbfE/5ihUXo7HxrZkj6vr+89KIa0pCoQqiUkW0rO072gY2R3gL7zMOnqa2Yb9qftZD+vicaOhVf/RRh8bh8eKB+82CAZkKc2epognMjhec/kfa0RccTN8jG5JgyxHB9Tvw8RZM+CWfm6fVt+xageahrBZJgAGMqEuwgQ7v7L9sXq/pBZG9fMBufbwicW6/CSZ41vVVdrYkqjULg/wurRwZrhzcQc/7rllHXXbXhAjZW/uNJ1t/7yU1rwkoaRxi/f0ayRc/kW/4Fl9VPC7uLdqY3RKQ/RGfnZgG3Eo/azB1DobJqqN1OWflI8lVzfR3RZ3emwUPUrJW3f5BF09yrfmR4om8qKEW1uIsHB5aypFU/PxhIJq3i1UxP6CJTEwbn1uh1XD3hRHRFHN/9tms0OI3ZBUNiAXUQEzJP8uIO0ZXImjkIXu3gwMOOt1A=")),
        Map.entry("treasure", new NpcPreset("Treasure Dealer", "treasure", "ewogICJ0aW1lc3RhbXAiIDogMTc3NDEwNDUyODY3OCwKICAicHJvZmlsZUlkIiA6ICI3MzFiOTdlYTI1MWM0ZjNmYTk0OTEwY2RkMmQwOTU4YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJJbU5vdEFDYXQ2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVhNzc2NDVlYTQxNjJkYTUzODExOGEwZjZiZjlmOTk4YTg0OGVkMzQwMjYxZWI1YTY5ZmE1NjE1Y2M2NTM1ZCIKICAgIH0KICB9Cn0=", "qBFgd9gGcTEHTV8GghBzdun+Aok2qz1/j3qqEsGgt3VRu9wHRhKXtrFFb8nalUMYA34G9KFV3ORLRVYAizOPW4RaOJFQi3crljTi3PRB6HYaMPFIdAmLjeCMoptfZzPqy1kNj5zjY/8Yf/owvFl87vTQ2T5Hs5vBqCbQCi9Y72Ga9szevrb1ChMYsp40VpahSok8FndiDcoxHMNNHsMZb3Zxm2OGO29XJFW90GceEOVEVgc/XJ/EuKxbHdjnbVOE1TpdHshC3+mpPA/2RGuUhtwIsJbt7F94Q981OvcDv3Zepxzkzg84U78ZhjxDnrAUBCJbAUDHxYTh101iORxFNI/HJ1f7lQCjb11gO2LDwT29nGUxeUl96vVmBPzEI0pZgez1dX7+InYqYVLTzYPFrE0XPJnZ7AvYYtxokYSoy4vv4JWF714ncZnJsV7wEriTvrm3mXi5/sLaebLQvSp58V73VqsmZZwB7/XS4hoXcWjcc5O3g/naFfRS2N33x9oR/9uCbykUoYCp2vA0R6lcq3P3Qj7BFbBnFIsTqbq4B/ymPwxc/Gzq6KeD6UtKwBXtHrZzCUmf8gB9YkeY46LfltZaVGX5/zvOV3cxdwOn8IJuaiurH5k4bOCMAUajbSPiFwByrUW6gY2O0D/ORZ6mk25aybKSb9Jl+DpxddV+h6c=")),
        Map.entry("ridethebus", new NpcPreset("Ride the Bus Dealer", "ridethebus", "ewogICJ0aW1lc3RhbXAiIDogMTczNjMxMTg0MzgwMywKICAicHJvZmlsZUlkIiA6ICIzZDU1OGQ3Y2NmZjk0ODdkYWE1MzhkMjM4NGE3OWFkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcnlwdGljTG9zZXIxMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hY2NlYTJhZmQ4NGFhMmIzODNjZDFhNDY3NDhmNmZmYTJlYThlOWIxMjQ0MTRiNGZmNjQ5M2U1ZWJhMDJlMDFlIgogICAgfQogIH0KfQ==", "vErpKjiEtUABLcenHI6kOdLz9ng6+koMI1pvGzhrKi+8xSlAYbzFhT5CpuYo4m6E4FM+scpXJeosvdQxu2GVkipJ7jNJHfZzpAI8ewiBsneIYnoTO4pYxX/Mc0GeCxP5LRsT7tH7JFHKDL8Gf0cPOeE56zvG6hJvkh+xhVgkPVKmyGWFU+8g7DXceWt9VV7/9riw46lyW1Qw9rOClW5FWz7gWj+wiAshUYQPiNPllU3xBQtW+MbFylmX/58fM6wwSuQDlRV06SA/Qbs1RYo5dDVbe4gKeqAntGYOcZ/+CNaAc5HkcHwBK2vbpMESqVpuZ91WsDMuL3Z0pN1soweNxsCvIJzNdgqMGRbp+O9BFDpp+6pJaDkzBrhm15qnnyfYgAZ834x4FOVQhsRoRyKG9XXeIASkH4oTXtsnT+B95NlXsHq/yNERbTkiNudu1WJzjDDSi3BZm0b69KeMLGHFm+FiWaQ9aL2zlL7qQC92hrBTE96d+7lVK+B3FQdPKrEszSnSMIwctY5i+/b9LzxdE/jlqgJ9jdVZS1bHFr6Jizsv+JPCjlI3I9Vh0fcfELAXDYEvgiB7UKN+mD8X3W+zx6I3RWKg/7jFJp1P3j8tBa9mIy2g10wcE/VbgC8zcb63WNLWwBsoHpfQaaVrs8/L7iGrjGEIn1GEGcG+VQ7VsTM="))
    );

    private final CasinoPlugin plugin;
    private final NamespacedKey npcKey;
    private final NamespacedKey gameKey;
    private final NamespacedKey groupKey;

    public CasinoNpcManager(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.npcKey = new NamespacedKey(plugin.getPlugin(), "casino_npc");
        this.gameKey = new NamespacedKey(plugin.getPlugin(), "casino_npc_game");
        this.groupKey = new NamespacedKey(plugin.getPlugin(), "casino_npc_group");
    }

    public boolean createNpc(Player player, String gameName) {
        CasinoGame game = plugin.getGameManager().getCasinoGameDirect(normalizeGameName(gameName));
        if (game == null) {
            plugin.getMessageManager().send(player, "<red>Unknown game: <white>" + gameName + "</white></red>");
            return false;
        }

        if ("coinflip".equals(game.getName())) {
            plugin.getMessageManager().send(player, "<red>Coin Flip does not support NPC creation.</red>");
            return false;
        }

        if ("slots".equals(game.getName())) {
            return createSlotsMachine(player, game);
        }

        if (plugin.getPlugin().getServer().getPluginManager().isPluginEnabled("Citizens")) {
            return createCitizensNpc(player, game);
        }

        Location spawn = findNpcSpawnLocation(player, 2.0);

        Villager villager = (Villager) player.getWorld().spawnEntity(spawn, EntityType.VILLAGER);
        villager.customName(title(game));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setCollidable(false);
        villager.setSilent(true);
        villager.setGravity(false);
        villager.setProfession(Villager.Profession.NONE);
        villager.setVillagerLevel(1);
        villager.setVillagerExperience(0);
        villager.setPersistent(true);
        PersistentDataContainer data = villager.getPersistentDataContainer();
        data.set(npcKey, PersistentDataType.BYTE, (byte) 1);
        data.set(gameKey, PersistentDataType.STRING, game.getName());

        plugin.getMessageManager().send(player,
            "<green>Created casino NPC for </green><gold>" + game.getDisplayName() + "</gold><gray> at your position.</gray>");
        return true;
    }

    public boolean removeTargetedNpc(Player player) {
        Entity target = getTargetedNpc(player, 6.0);
        if (target == null) {
            if (plugin.getPlugin().getServer().getPluginManager().isPluginEnabled("Citizens")) {
                NPC selected = CitizensAPI.getDefaultNPCSelector().getSelected(player);
                if (selected != null && selected.hasTrait(CasinoGameTrait.class)) {
                    String gameName = selected.getTrait(CasinoGameTrait.class).getGameName();
                    CitizensAPI.getNPCRegistry().deregister(selected);
                    plugin.getMessageManager().send(player,
                        "<yellow>Removed casino NPC</yellow>" +
                            (gameName == null ? "." : " <gray>for</gray> <gold>" + resolveDisplayName(gameName) + "</gold>"));
                    return true;
                }
            }
            plugin.getMessageManager().send(player, "<red>Look at a casino NPC within 6 blocks to remove it.</red>");
            return false;
        }

        String gameName = getNpcGameName(target);
        if (isCitizensCasinoNpc(target)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(target);
            if (npc != null) {
                CitizensAPI.getNPCRegistry().deregister(npc);
            }
        } else if (hasNpcGroup(target)) {
            removeNpcGroup(target.getWorld(), target.getPersistentDataContainer().get(groupKey, PersistentDataType.STRING));
        } else {
            target.remove();
        }
        plugin.getMessageManager().send(player,
            "<yellow>Removed casino NPC</yellow>" +
                (gameName == null ? "." : " <gray>for</gray> <gold>" + resolveDisplayName(gameName) + "</gold>"));
        return true;
    }

    public void openNpcGame(Player player, Entity entity) {
        String gameName = getNpcGameName(entity);
        if (gameName == null) {
            return;
        }
        openNpcGameByName(player, gameName);
    }

    public void openNpcGameByName(Player player, String gameName) {
        if (!plugin.getRegionAccessManager().canUseCasino(player)) {
            plugin.getRegionAccessManager().sendBlockedMessage(player);
            return;
        }

        CasinoGame game = plugin.getGameManager().getCasinoGameDirect(gameName);
        if (game == null || !game.isEnabled()) {
            plugin.getMessageManager().send(player, "<red>This game NPC is unavailable right now.</red>");
            return;
        }

        Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> new GameBetGUI(plugin, player, game, game.getMinBet()).open());
    }

    public boolean bindSelectedCitizensNpc(Player player, String gameName) {
        if (!plugin.getPlugin().getServer().getPluginManager().isPluginEnabled("Citizens")) {
            plugin.getMessageManager().send(player, "<red>Citizens is not installed or not enabled.</red>");
            return false;
        }

        CasinoGame game = plugin.getGameManager().getCasinoGameDirect(normalizeGameName(gameName));
        if (game == null) {
            plugin.getMessageManager().send(player, "<red>Unknown game: <white>" + gameName + "</white></red>");
            return false;
        }

        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
        if (npc == null) {
            plugin.getMessageManager().send(player, "<red>Select a Citizens NPC first with Citizens.</red>");
            return false;
        }

        CasinoGameTrait trait = npc.getOrAddTrait(CasinoGameTrait.class);
        trait.setGameName(game.getName());
        plugin.getMessageManager().send(player,
            "<green>Bound selected Citizens NPC to </green><gold>" + game.getDisplayName() + "</gold><gray>.</gray>");
        return true;
    }

    public boolean unbindSelectedCitizensNpc(Player player) {
        if (!plugin.getPlugin().getServer().getPluginManager().isPluginEnabled("Citizens")) {
            plugin.getMessageManager().send(player, "<red>Citizens is not installed or not enabled.</red>");
            return false;
        }

        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
        if (npc == null) {
            plugin.getMessageManager().send(player, "<red>Select a Citizens NPC first with Citizens.</red>");
            return false;
        }

        if (!npc.hasTrait(CasinoGameTrait.class)) {
            plugin.getMessageManager().send(player, "<yellow>The selected Citizens NPC is not bound to a casino game.</yellow>");
            return false;
        }

        String gameName = npc.getTrait(CasinoGameTrait.class).getGameName();
        npc.removeTrait(CasinoGameTrait.class);
        plugin.getMessageManager().send(player,
            "<yellow>Unbound selected Citizens NPC</yellow>" +
                (gameName == null ? "." : " <gray>from</gray> <gold>" + resolveDisplayName(gameName) + "</gold>"));
        return true;
    }

    public boolean isCasinoNpc(Entity entity) {
        return isNativeCasinoNpc(entity) || isCitizensCasinoNpc(entity);
    }

    public String getNpcGameName(Entity entity) {
        if (isNativeCasinoNpc(entity)) {
            return entity.getPersistentDataContainer().get(gameKey, PersistentDataType.STRING);
        }
        if (isCitizensCasinoNpc(entity)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            return npc == null ? null : npc.getTrait(CasinoGameTrait.class).getGameName();
        }
        return null;
    }

    public List<String> getSupportedGames() {
        List<CasinoGame> games = new ArrayList<>(plugin.getGameManager().getAllCasinoGames().values());
        games.sort(Comparator.comparing(CasinoGame::getName));
        List<String> result = new ArrayList<>();
        for (CasinoGame game : games) {
            if ("coinflip".equals(game.getName())) {
                continue;
            }
            result.add(game.getName());
        }
        return result;
    }

    private Component title(CasinoGame game) {
        return plugin.getMessageManager().parse("<gold><bold>" + game.getDisplayName() + "</bold></gold>");
    }

    private Entity getTargetedNpc(Player player, double range) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            range,
            0.4,
            entity -> isCasinoNpc(entity) || isCitizensCasinoNpc(entity)
        );
        return result == null ? null : result.getHitEntity();
    }

    private boolean createCitizensNpc(Player player, CasinoGame game) {
        Location spawn = findNpcSpawnLocation(player, 2.0);
        spawn.setYaw(player.getLocation().getYaw() + 180.0f);
        spawn.setPitch(0.0f);

        NpcPreset preset = CITIZENS_PRESETS.getOrDefault(game.getName(), new NpcPreset(game.getDisplayName() + " Dealer", game.getName(), "", ""));
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, preset.displayName(), spawn);
        CasinoGameTrait trait = npc.getOrAddTrait(CasinoGameTrait.class);
        trait.setGameName(game.getName());

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        if (!preset.texture().isBlank() && !preset.signature().isBlank()) {
            skinTrait.setSkinPersistent(preset.skinId(), preset.signature(), preset.texture());
            skinTrait.setShouldUpdateSkins(false);
            skinTrait.setFetchDefaultSkin(false);
        }

        plugin.getMessageManager().send(player,
            "<green>Created Citizens casino NPC for </green><gold>" + game.getDisplayName() + "</gold><gray>.</gray>");
        return true;
    }

    private boolean createSlotsMachine(Player player, CasinoGame game) {
        Location base = findNpcSpawnLocation(player, 2.5);
        base.setYaw(player.getLocation().getYaw() + 180.0f);
        base.setPitch(0.0f);
        String groupId = UUID.randomUUID().toString();

        Location anchor = base.clone().toBlockLocation().add(0.5, 0.0, 0.5);

        spawnBlock(anchor.clone().add(0.0, 0.0, 0.0), Material.POLISHED_BLACKSTONE, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.0, 1.0, 0.0), Material.POLISHED_BLACKSTONE_BRICKS, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.0, 2.0, 0.0), Material.BLACKSTONE, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.0, 1.0, 0.12), Material.TINTED_GLASS, game.getName(), groupId);
        spawnBlock(anchor.clone().add(-0.32, 1.0, 0.13), Material.YELLOW_CONCRETE, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.0, 1.0, 0.13), Material.LIME_CONCRETE, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.32, 1.0, 0.13), Material.RED_CONCRETE, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.58, 1.15, 0.0), Material.LEVER, game.getName(), groupId);
        spawnBlock(anchor.clone().add(0.0, 2.32, 0.0), Material.GLOWSTONE, game.getName(), groupId);

        Interaction interaction = (Interaction) anchor.getWorld().spawnEntity(anchor.clone().add(0.0, 1.0, 0.55), EntityType.INTERACTION);
        interaction.setInteractionWidth(1.4f);
        interaction.setInteractionHeight(2.4f);
        tagEntity(interaction, game.getName(), groupId);

        plugin.getMessageManager().send(player,
            "<green>Created block-built slots machine NPC.</green> <gray>Use </gray><white>/casino admin npc remove</white><gray> while looking at it to remove it.</gray>");
        return true;
    }

    private void spawnBlock(Location location, Material material, String gameName, String groupId) {
        Location snapped = location.toBlockLocation().add(0.5, 0.0, 0.5);
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(snapped, EntityType.BLOCK_DISPLAY);
        BlockData blockData = Bukkit.createBlockData(material);
        display.setBlock(blockData);
        display.setPersistent(true);
        tagEntity(display, gameName, groupId);
    }

    private Location findNpcSpawnLocation(Player player, double maxDistance) {
        Location eye = player.getEyeLocation();
        RayTraceResult result = player.getWorld().rayTraceBlocks(
            eye,
            eye.getDirection(),
            maxDistance,
            FluidCollisionMode.NEVER,
            true
        );

        if (result != null && result.getHitBlock() != null && result.getHitBlockFace() != null) {
            Location surface = result.getHitBlock().getLocation().add(0.5, 1.0, 0.5);
            surface.setYaw(player.getLocation().getYaw() + 180.0f);
            surface.setPitch(0.0f);
            return surface;
        }

        Location fallback = player.getLocation().clone().add(player.getLocation().getDirection().normalize().multiply(maxDistance));
        fallback.setY(player.getLocation().getY());
        fallback = fallback.toBlockLocation().add(0.5, 1.0, 0.5);
        fallback.setYaw(player.getLocation().getYaw() + 180.0f);
        fallback.setPitch(0.0f);
        return fallback;
    }

    private void tagEntity(Entity entity, String gameName, String groupId) {
        PersistentDataContainer data = entity.getPersistentDataContainer();
        data.set(npcKey, PersistentDataType.BYTE, (byte) 1);
        data.set(gameKey, PersistentDataType.STRING, gameName);
        data.set(groupKey, PersistentDataType.STRING, groupId);
    }

    private boolean isCitizensCasinoNpc(Entity entity) {
        if (entity == null || !plugin.getPlugin().getServer().getPluginManager().isPluginEnabled("Citizens")) {
            return false;
        }
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        return npc != null && npc.hasTrait(CasinoGameTrait.class) && npc.getTrait(CasinoGameTrait.class).getGameName() != null;
    }

    private boolean isNativeCasinoNpc(Entity entity) {
        return entity != null
            && entity.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE)
            && entity.getPersistentDataContainer().has(gameKey, PersistentDataType.STRING);
    }

    private boolean hasNpcGroup(Entity entity) {
        return entity != null && entity.getPersistentDataContainer().has(groupKey, PersistentDataType.STRING);
    }

    private void removeNpcGroup(org.bukkit.World world, String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return;
        }
        for (Entity entity : world.getEntities()) {
            if (groupId.equals(entity.getPersistentDataContainer().get(groupKey, PersistentDataType.STRING))) {
                entity.remove();
            }
        }
    }

    private String resolveDisplayName(String gameName) {
        CasinoGame game = plugin.getGameManager().getCasinoGameDirect(gameName);
        return game == null ? gameName : game.getDisplayName();
    }

    private String normalizeGameName(String gameName) {
        return gameName.toLowerCase(Locale.ROOT).replace(" ", "");
    }

    private record NpcPreset(String displayName, String skinId, String texture, String signature) {
    }
}
