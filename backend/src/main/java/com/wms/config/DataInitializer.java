package com.wms.config;

import com.wms.entity.*;
import com.wms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private ShelfSlotRepository shelfSlotRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            initUsers();
            initMaterials();
            initWarehouses();
            initShelvesAndSlots();
            initInventory();
            logger.info("数据初始化完成");
        }
    }

    private void initUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("系统管理员");
        admin.setEmail("admin@semiconductor-wms.com");
        admin.setPhone("13800000001");
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        userRepository.save(admin);

        User operator = new User();
        operator.setUsername("operator");
        operator.setPassword(passwordEncoder.encode("123456"));
        operator.setRealName("仓库操作员");
        operator.setEmail("operator@semiconductor-wms.com");
        operator.setPhone("13800000002");
        operator.setRole("OPERATOR");
        operator.setEnabled(true);
        userRepository.save(operator);

        logger.info("初始化用户数据完成");
    }

    private void initMaterials() {
        String[][] rawMaterials = {
                {"RM001", "单晶硅片", "RAW_MATERIAL", "8英寸/P型", "片", "150.00", "深圳硅威电子"},
                {"RM002", "光刻胶", "RAW_MATERIAL", "正性/I线", "升", "2800.00", "日本东京应化"},
                {"RM003", "蚀刻液", "RAW_MATERIAL", "HF基", "升", "580.00", "上海新阳半导体"},
                {"RM004", "溅射靶材", "RAW_MATERIAL", "铜靶/99.999%", "块", "12000.00", "江丰电子"},
                {"RM005", "封装基板", "RAW_MATERIAL", "BT基板", "片", "45.00", "深南电路"},
                {"RM006", "金线", "RAW_MATERIAL", "25μm", "米", "8.50", "贺利氏"},
                {"RM007", "环氧树脂", "RAW_MATERIAL", "EMC封装料", "千克", "320.00", "长春一塑"},
                {"RM008", "清洗溶剂", "RAW_MATERIAL", "IPA/电子级", "升", "85.00", "江化微"},
        };

        String[][] semiProducts = {
                {"SP001", "氧化硅片", "SEMI_PRODUCT", "8英寸/1000A", "片", "280.00", ""},
                {"SP002", "光刻图形晶圆", "SEMI_PRODUCT", "8英寸/65nm", "片", "450.00", ""},
                {"SP003", "刻蚀晶圆", "SEMI_PRODUCT", "8英寸/完成刻蚀", "片", "520.00", ""},
                {"SP004", "金属化晶圆", "SEMI_PRODUCT", "8英寸/Cu互连", "片", "680.00", ""},
                {"SP005", "减薄晶圆", "SEMI_PRODUCT", "8英寸/200μm", "片", "720.00", ""},
        };

        String[][] finishedProducts = {
                {"FP001", "MCU芯片", "FINISHED_PRODUCT", "STM32F103", "颗", "12.50", ""},
                {"FP002", "存储芯片", "FINISHED_PRODUCT", "DDR4/8Gb", "颗", "35.00", ""},
                {"FP003", "电源管理IC", "FINISHED_PRODUCT", "DC-DC/5A", "颗", "8.80", ""},
                {"FP004", "射频芯片", "FINISHED_PRODUCT", "2.4GHz/PA", "颗", "15.60", ""},
                {"FP005", "传感器芯片", "FINISHED_PRODUCT", "MEMS加速度计", "颗", "22.00", ""},
                {"FP006", "驱动IC", "FINISHED_PRODUCT", "LED驱动/30W", "颗", "6.50", ""},
        };

        saveMaterials(rawMaterials);
        saveMaterials(semiProducts);
        saveMaterials(finishedProducts);

        logger.info("初始化物料数据完成");
    }

    private void saveMaterials(String[][] data) {
        for (String[] row : data) {
            Material material = new Material();
            material.setMaterialCode(row[0]);
            material.setMaterialName(row[1]);
            material.setMaterialType(row[2]);
            material.setSpecification(row[3]);
            material.setUnit(row[4]);
            material.setPrice(new BigDecimal(row[5]));
            material.setSupplier(row[6]);
            material.setMinStock(100);
            material.setMaxStock(10000);
            material.setEnabled(true);
            materialRepository.save(material);
        }
    }

    private void initWarehouses() {
        String[][] warehouses = {
                {"WH001", "原材料仓库", "A栋1层", "张伟", "13900000001", "5000"},
                {"WH002", "半制品仓库", "B栋2层", "李明", "13900000002", "3000"},
                {"WH003", "成品仓库", "C栋1层", "王芳", "13900000003", "4000"},
        };

        for (String[] row : warehouses) {
            Warehouse warehouse = new Warehouse();
            warehouse.setWarehouseCode(row[0]);
            warehouse.setWarehouseName(row[1]);
            warehouse.setAddress(row[2]);
            warehouse.setManager(row[3]);
            warehouse.setPhone(row[4]);
            warehouse.setTotalCapacity(Integer.parseInt(row[5]));
            warehouse.setUsedCapacity(0);
            warehouse.setEnabled(true);
            warehouseRepository.save(warehouse);
        }

        logger.info("初始化仓库数据完成");
    }

    private void initShelvesAndSlots() {
        List<Warehouse> warehouses = warehouseRepository.findAllEnabled();

        for (Warehouse warehouse : warehouses) {
            for (int i = 1; i <= 4; i++) {
                Shelf shelf = new Shelf();
                shelf.setShelfCode(warehouse.getWarehouseCode() + "-S" + String.format("%02d", i));
                shelf.setShelfName(warehouse.getWarehouseName() + " " + i + "号货架");
                shelf.setWarehouse(warehouse);
                shelf.setRowNum(4);
                shelf.setColumnNum(5);
                shelf.setLayerNum(3);
                shelf.setPositionX((i - 1) * 300);
                shelf.setPositionY(0);
                shelf.setPositionZ(0);
                shelf.setWidth(200);
                shelf.setDepth(80);
                shelf.setHeight(250);
                shelf.setShelfType("STANDARD");
                shelf.setTotalSlots(60);
                shelf.setUsedSlots(0);
                shelf.setEnabled(true);

                shelf = shelfRepository.save(shelf);

                createSlotsForShelf(shelf);
            }
        }

        logger.info("初始化货架数据完成");
    }

    private void createSlotsForShelf(Shelf shelf) {
        List<ShelfSlot> slots = new ArrayList<>();
        for (int row = 1; row <= shelf.getRowNum(); row++) {
            for (int col = 1; col <= shelf.getColumnNum(); col++) {
                for (int layer = 1; layer <= shelf.getLayerNum(); layer++) {
                    ShelfSlot slot = new ShelfSlot();
                    slot.setSlotCode(String.format("%s-%d-%d-%d", shelf.getShelfCode(), row, col, layer));
                    slot.setShelf(shelf);
                    slot.setRowIndex(row);
                    slot.setColumnIndex(col);
                    slot.setLayerIndex(layer);
                    slot.setQuantity(0);
                    slot.setMaxCapacity(100);
                    slot.setStatus("EMPTY");
                    slots.add(slot);
                }
            }
        }
        shelfSlotRepository.saveAll(slots);
    }

    private void initInventory() {
        List<Material> materials = materialRepository.findAllEnabled();
        List<Warehouse> warehouses = warehouseRepository.findAllEnabled();

        int materialIndex = 0;
        for (Warehouse warehouse : warehouses) {
            List<Shelf> shelves = shelfRepository.findByWarehouseId(warehouse.getId());
            if (shelves.isEmpty()) continue;

            Shelf shelf = shelves.get(0);
            List<ShelfSlot> slots = shelfSlotRepository.findByShelfIdWithMaterial(shelf.getId());

            for (int i = 0; i < Math.min(5, slots.size()) && materialIndex < materials.size(); i++) {
                ShelfSlot slot = slots.get(i);
                Material material = materials.get(materialIndex);

                int quantity = 100 + (int) (Math.random() * 400);

                slot.setMaterial(material);
                slot.setQuantity(quantity);
                slot.setStatus("OCCUPIED");
                shelfSlotRepository.save(slot);

                Inventory inventory = new Inventory();
                inventory.setMaterial(material);
                inventory.setWarehouse(warehouse);
                inventory.setShelf(shelf);
                inventory.setSlot(slot);
                inventory.setBatchNo("BATCH" + System.currentTimeMillis() + i);
                inventory.setQuantity(quantity);
                inventory.setAvailableQuantity(quantity);
                inventory.setLockedQuantity(0);
                inventory.setStatus("NORMAL");
                inventoryRepository.save(inventory);

                shelf.setUsedSlots(shelf.getUsedSlots() + 1);
                materialIndex++;
            }

            shelfRepository.save(shelf);
            warehouse.setUsedCapacity(warehouse.getUsedCapacity() + shelf.getUsedSlots() * 10);
            warehouseRepository.save(warehouse);
        }

        logger.info("初始化库存数据完成");
    }
}
