const test = require("node:test");
const assert = require("node:assert/strict");
const { createInventory, getStockStatus, getSummary, recordSale } = require("../src/stock-manager");

test("recordSale decreases stock for the sold product", () => {
  const inventory = createInventory([
    { id: "marteau", name: "Marteau", stock: 10, reorderLevel: 2 }
  ]);

  const updatedInventory = recordSale(inventory, "marteau", 3);

  assert.equal(updatedInventory[0].stock, 7);
  assert.equal(inventory[0].stock, 10);
});

test("recordSale rejects quantities greater than available stock", () => {
  const inventory = createInventory([
    { id: "peinture", name: "Peinture", stock: 2, reorderLevel: 1 }
  ]);

  assert.throws(() => recordSale(inventory, "peinture", 4), {
    message: "Stock insuffisant pour enregistrer cette vente."
  });
});

test("stock status and summary highlight products to replenish", () => {
  const inventory = createInventory([
    { id: "vis", name: "Vis", stock: 30, reorderLevel: 20 },
    { id: "ciment", name: "Ciment", stock: 5, reorderLevel: 10 },
    { id: "peinture", name: "Peinture", stock: 0, reorderLevel: 4 }
  ]);

  const summary = getSummary(inventory);

  assert.equal(getStockStatus(inventory[0]), "En stock");
  assert.equal(getStockStatus(inventory[1]), "À réapprovisionner");
  assert.equal(getStockStatus(inventory[2]), "Rupture");
  assert.deepEqual(summary, {
    totalProducts: 3,
    totalUnits: 35,
    lowStockCount: 1,
    outOfStockCount: 1
  });
});
