function cloneInventory(products) {
  return products.map((product) => ({ ...product }));
}

function createInventory(products) {
  return cloneInventory(products);
}

function getStockStatus(product) {
  if (product.stock <= 0) {
    return "Rupture";
  }

  if (product.stock <= product.reorderLevel) {
    return "À réapprovisionner";
  }

  return "En stock";
}

function getSummary(inventory) {
  return inventory.reduce(
    (summary, product) => {
      summary.totalProducts += 1;
      summary.totalUnits += product.stock;

      if (product.stock <= 0) {
        summary.outOfStockCount += 1;
      } else if (product.stock <= product.reorderLevel) {
        summary.lowStockCount += 1;
      }

      return summary;
    },
    {
      totalProducts: 0,
      totalUnits: 0,
      lowStockCount: 0,
      outOfStockCount: 0
    }
  );
}

function recordSale(inventory, productId, quantity) {
  const saleQuantity = Number(quantity);

  if (!Number.isInteger(saleQuantity) || saleQuantity <= 0) {
    throw new Error("La quantité vendue doit être un entier positif.");
  }

  const nextInventory = cloneInventory(inventory);
  const product = nextInventory.find((item) => item.id === productId);

  if (!product) {
    throw new Error("Produit introuvable.");
  }

  if (saleQuantity > product.stock) {
    throw new Error("Stock insuffisant pour enregistrer cette vente.");
  }

  product.stock -= saleQuantity;
  return nextInventory;
}

const StockManager = {
  createInventory,
  getStockStatus,
  getSummary,
  recordSale
};

if (typeof module !== "undefined") {
  module.exports = StockManager;
}

if (typeof window !== "undefined") {
  window.StockManager = StockManager;
}
