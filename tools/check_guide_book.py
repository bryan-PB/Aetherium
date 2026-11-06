#!/usr/bin/env python3
"""Static checks for the starter/craftable Kryzorae Codex (Forge 1.16.5)."""
from pathlib import Path
import json

root = Path("src/main/java/com/aetherium/aetherium")
assets = Path("src/main/resources/assets/aetherium")
data = Path("src/main/resources/data/aetherium")
item = (root / "content/item/GuideBookItem.java").read_text(encoding="utf-8")
client = (root / "client/GuideBookClient.java").read_text(encoding="utf-8")
screen = (root / "client/GuideBookScreen.java").read_text(encoding="utf-8")
events = (root / "events/GuideBookEvents.java").read_text(encoding="utf-8")
registry = (root / "registry/RegistryHandler.java").read_text(encoding="utf-8")

assert 'GUIDE_BOOK = ITEMS.register("guide_book"' in registry
assert "new GuideBookItem(" in registry
assert "DistExecutor.safeRunWhenOn(Dist.CLIENT" in item
assert "new GuideBookScreen()" in client
assert "extends Screen" in screen
assert "CHAPTERS = 8" in screen
assert "renderCover(" in screen
assert "renderSidebar(" in screen
assert "renderPage(" in screen
assert "mouseClicked(" in screen
assert "mouseScrolled(" in screen
assert "keyPressed(" in screen
assert "panelScale = Math.min(" in screen
assert "RegistryHandler.AETHERIUM_SWORD.get()" in screen
assert "RegistryHandler.ASTRAL_SWORD.get()" in screen
assert "GuideBookEvents" not in screen
assert "if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;" in events
assert 'STARTER_FLAG = "KryzoraeGuideReceived"' in events
assert "persistent.getBoolean(STARTER_FLAG)" in events
assert "onPlayerClone(PlayerEvent.Clone event)" in events
assert "if (!player.inventory.add(guide)) player.drop(guide, false);" in events

recipe = json.loads((data / "recipes/guide_book.json").read_text(encoding="utf-8"))
assert recipe["type"] == "minecraft:crafting_shapeless"
assert recipe["result"]["item"] == "aetherium:guide_book"
assert {x["item"] for x in recipe["ingredients"]} == {"minecraft:book", "minecraft:lapis_lazuli"}

model = json.loads((assets / "models/item/guide_book.json").read_text(encoding="utf-8"))
assert model["textures"]["layer0"] == "minecraft:item/written_book"

for locale in ("en_us", "es_es", "es_cl", "es_mx", "es_ar"):
    d = json.loads((assets / f"lang/{locale}.json").read_text(encoding="utf-8"))
    assert d["item.aetherium.guide_book"]
    assert d["tooltip.aetherium.guide_book"]
    for key in ("guide.aetherium.ui.codex", "guide.aetherium.ui.chapter", "guide.aetherium.ui.hint"):
        assert d[key]
    for i in range(1, 9):
        assert d[f"guide.aetherium.chapter.{i}"]
        text = d[f"guide.aetherium.page.{i}"]
        assert 50 <= len(text) <= 400, (locale, i)
        assert len(text.splitlines()) >= 2, (locale, i)

print("Guide: item, first login delivery, respawn persistence, recipe, custom GUI and five languages validated.")
