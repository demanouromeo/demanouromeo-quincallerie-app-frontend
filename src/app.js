const initialProducts = [
  { id: "marteau", name: "Marteau", category: "Outillage", stock: 18, reorderLevel: 5 },
  { id: "vis-6mm", name: "Vis 6 mm", category: "Fixation", stock: 120, reorderLevel: 40 },
  { id: "ciment-colle", name: "Ciment colle", category: "Maçonnerie", stock: 8, reorderLevel: 10 },
  { id: "peinture-blanche", name: "Peinture blanche", category: "Finition", stock: 0, reorderLevel: 4 }
];

const storageKey = "quincallerie-etoud-stock";
const saleForm = document.querySelector("#sale-form");
const productSelect = document.querySelector("#product");
const quantityInput = document.querySelector("#quantity");
const messageBox = document.querySelector("#message");
const summaryList = document.querySelector("#summary");
const inventoryTableBody = document.querySelector("#inventory-body");

function loadInventory() {
  try {
    const savedInventory = window.localStorage.getItem(storageKey);

    if (!savedInventory) {
      return window.StockManager.createInventory(initialProducts);
    }

    return window.StockManager.createInventory(JSON.parse(savedInventory));
  } catch (error) {
    return window.StockManager.createInventory(initialProducts);
  }
}

let inventory = loadInventory();

function saveInventory() {
  window.localStorage.setItem(storageKey, JSON.stringify(inventory));
}

function renderSummary() {
  const summary = window.StockManager.getSummary(inventory);

  summaryList.innerHTML = `
    <li><strong>${summary.totalProducts}</strong><span>produits suivis</span></li>
    <li><strong>${summary.totalUnits}</strong><span>articles en stock</span></li>
    <li><strong>${summary.lowStockCount}</strong><span>à réapprovisionner</span></li>
    <li><strong>${summary.outOfStockCount}</strong><span>en rupture</span></li>
  `;
}

function renderInventory() {
  inventoryTableBody.innerHTML = inventory
    .map((product) => {
      const status = window.StockManager.getStockStatus(product);

      return `
        <tr>
          <td>${product.name}</td>
          <td>${product.category}</td>
          <td>${product.stock}</td>
          <td>${product.reorderLevel}</td>
          <td><span class="status status-${status.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, "-")}">${status}</span></td>
        </tr>
      `;
    })
    .join("");
}

function renderProductOptions() {
  productSelect.innerHTML = inventory
    .map(
      (product) => `
        <option value="${product.id}">
          ${product.name} (${product.stock} en stock)
        </option>
      `
    )
    .join("");
}

function render() {
  renderSummary();
  renderInventory();
  renderProductOptions();
}

saleForm.addEventListener("submit", (event) => {
  event.preventDefault();

  try {
    inventory = window.StockManager.recordSale(inventory, productSelect.value, quantityInput.value);
    saveInventory();
    render();
    messageBox.textContent = "Vente enregistrée et stock mis à jour.";
    messageBox.dataset.state = "success";
    quantityInput.value = "1";
  } catch (error) {
    messageBox.textContent = error.message;
    messageBox.dataset.state = "error";
  }
});

render();
