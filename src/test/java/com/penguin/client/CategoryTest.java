package com.penguin.client;

import com.penguin.client.module.Category;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Category enum.
 */
public class CategoryTest {

    @Test
    public void testCategoryValues() {
        // Ensure all expected categories exist
        Category[] categories = Category.values();
        assertTrue(categories.length > 0, "Should have at least one category");
    }

    @Test
    public void testCombatCategory() {
        Category combat = Category.COMBAT;
        assertNotNull(combat);
        assertEquals("Combat", combat.getName());
    }

    @Test
    public void testMovementCategory() {
        Category movement = Category.MOVEMENT;
        assertNotNull(movement);
        assertEquals("Movement", movement.getName());
    }

    @Test
    public void testRenderCategory() {
        Category render = Category.RENDER;
        assertNotNull(render);
        assertEquals("Render", render.getName());
    }

    @Test
    public void testPlayerCategory() {
        Category player = Category.PLAYER;
        assertNotNull(player);
        assertEquals("Player", player.getName());
    }

    @Test
    public void testWorldCategory() {
        Category world = Category.WORLD;
        assertNotNull(world);
        assertEquals("World", world.getName());
    }

    @Test
    public void testMiscCategory() {
        Category misc = Category.MISC;
        assertNotNull(misc);
        assertEquals("Misc", misc.getName());
    }

    @Test
    public void testTestingCategory() {
        Category testing = Category.TESTING;
        assertNotNull(testing);
        assertEquals("Testing", testing.getName());
    }

    @Test
    public void testSearchCategory() {
        Category search = Category.SEARCH;
        assertNotNull(search);
        assertEquals("Search", search.getName());
    }

    @Test
    public void testCategoryFromName() {
        // Test that we can find categories by iterating
        boolean foundCombat = false;
        for (Category cat : Category.values()) {
            if (cat.getName().equals("Combat")) {
                foundCombat = true;
                break;
            }
        }
        assertTrue(foundCombat, "Should find Combat category");
    }

    @Test
    public void testAllCategoriesHaveNames() {
        for (Category cat : Category.values()) {
            assertNotNull(cat.getName(), "Category " + cat + " should have a name");
            assertFalse(cat.getName().isEmpty(), "Category " + cat + " name should not be empty");
        }
    }

    @Test
    public void testCategoryVisibility() {
        // Most categories should be visible by default
        assertTrue(Category.COMBAT.isVisible(), "Combat should be visible");
        assertTrue(Category.MOVEMENT.isVisible(), "Movement should be visible");
        assertTrue(Category.PLAYER.isVisible(), "Player should be visible");
        assertTrue(Category.RENDER.isVisible(), "Render should be visible");
        assertTrue(Category.WORLD.isVisible(), "World should be visible");
        assertTrue(Category.MISC.isVisible(), "Misc should be visible");
        assertTrue(Category.SETTINGS.isVisible(), "Settings should be visible");
        assertTrue(Category.SEARCH.isVisible(), "Search should be visible");
        
        // TESTING should be hidden by default (requires beta tester mode)
        assertFalse(Category.TESTING.isVisible(), "Testing should be hidden by default");
    }
    
    @Test
    public void testSettingsCategory() {
        Category settings = Category.SETTINGS;
        assertNotNull(settings);
        assertEquals("Settings", settings.getName());
    }
}
