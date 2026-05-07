import React, { useState, useEffect, useRef, Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Text, Html } from '@react-three/drei';
import { Card, Select, Spin, Empty, Badge, Descriptions, Tag, Button } from 'antd';
import { CloseOutlined } from '@ant-design/icons';
import { warehouseAPI, shelfAPI } from '../services/api';

const { Option } = Select;

const materialTypeColors = {
  RAW_MATERIAL: '#1890ff',
  SEMI_PRODUCT: '#faad14',
  FINISHED_PRODUCT: '#52c41a',
  EMPTY: '#d9d9d9',
};

const materialTypeNames = {
  RAW_MATERIAL: '原材料',
  SEMI_PRODUCT: '半制品',
  FINISHED_PRODUCT: '产成品',
};

const Floor = () => (
  <mesh rotation={[-Math.PI / 2, 0, 0]} position={[0, -0.1, 0]} receiveShadow>
    <planeGeometry args={[2000, 2000]} />
    <meshStandardMaterial color="#1a1a2e" />
  </mesh>
);

const Grid = () => (
  <gridHelper args={[2000, 100, '#333', '#222']} position={[0, 0, 0]} />
);

const ShelfSlot = ({ position, slot, shelfCode, onSelect, isSelected }) => {
  const meshRef = useRef();
  const [hovered, setHovered] = useState(false);

  const color = slot.status === 'OCCUPIED'
    ? materialTypeColors[slot.materialType] || '#1890ff'
    : materialTypeColors.EMPTY;

  const fillLevel = slot.quantity / (slot.maxCapacity || 100);

  const handleClick = (e) => {
    e.stopPropagation();
    onSelect({ ...slot, shelfCode });
  };

  return (
    <group position={position}>
      <mesh
        ref={meshRef}
        onPointerOver={(e) => {
          e.stopPropagation();
          setHovered(true);
          document.body.style.cursor = 'pointer';
        }}
        onPointerOut={() => {
          setHovered(false);
          document.body.style.cursor = 'auto';
        }}
        onClick={handleClick}
        castShadow
      >
        <boxGeometry args={[28, 28 * fillLevel + 2, 28]} />
        <meshStandardMaterial
          color={isSelected ? '#ff4d4f' : color}
          transparent
          opacity={hovered || isSelected ? 1 : 0.85}
          emissive={hovered || isSelected ? (isSelected ? '#ff4d4f' : color) : '#000'}
          emissiveIntensity={hovered || isSelected ? 0.4 : 0}
        />
      </mesh>
      <mesh position={[0, 0, 0]}>
        <boxGeometry args={[30, 30, 30]} />
        <meshStandardMaterial color={isSelected ? '#ff4d4f' : '#333'} wireframe />
      </mesh>
    </group>
  );
};

const Shelf3D = ({ shelf, onSlotSelect, selectedSlotId }) => {
  const groupRef = useRef();

  const baseX = (shelf.positionX || 0) / 2;
  const baseY = 0;
  const baseZ = (shelf.positionZ || 0) / 2;

  const slotSize = 35;
  const slots = shelf.slots || [];

  return (
    <group ref={groupRef} position={[baseX, baseY, baseZ]}>
      <mesh position={[0, -5, 0]} castShadow receiveShadow>
        <boxGeometry args={[shelf.columnNum * slotSize + 20, 10, shelf.layerNum * slotSize + 20]} />
        <meshStandardMaterial color="#2d2d44" />
      </mesh>

      {[...Array(shelf.columnNum + 1)].map((_, i) => (
        <mesh
          key={`frame-col-${i}`}
          position={[
            -((shelf.columnNum * slotSize) / 2) + i * slotSize,
            (shelf.rowNum * slotSize) / 2,
            0,
          ]}
        >
          <boxGeometry args={[3, shelf.rowNum * slotSize + 10, shelf.layerNum * slotSize + 20]} />
          <meshStandardMaterial color="#4a4a6a" />
        </mesh>
      ))}

      {slots.map((slot, index) => {
        const col = slot.columnIndex - 1;
        const row = slot.rowIndex - 1;
        const layer = slot.layerIndex - 1;

        const x = -((shelf.columnNum - 1) * slotSize) / 2 + col * slotSize;
        const y = row * slotSize + slotSize / 2;
        const z = -((shelf.layerNum - 1) * slotSize) / 2 + layer * slotSize;

        return (
          <ShelfSlot
            key={slot.id || index}
            position={[x, y, z]}
            slot={slot}
            shelfCode={shelf.shelfCode}
            onSelect={onSlotSelect}
            isSelected={selectedSlotId === slot.id}
          />
        );
      })}

      <Text
        position={[0, shelf.rowNum * slotSize + 20, 0]}
        fontSize={20}
        color="#fff"
        anchorX="center"
        anchorY="middle"
      >
        {shelf.shelfCode}
      </Text>
    </group>
  );
};

const Scene = ({ shelves, onSlotSelect, selectedSlotId }) => {
  return (
    <>
      <ambientLight intensity={0.4} />
      <directionalLight position={[100, 200, 100]} intensity={0.8} castShadow />
      <pointLight position={[-100, 100, -100]} intensity={0.5} />

      <Floor />
      <Grid />

      {shelves.map((shelf) => (
        <Shelf3D
          key={shelf.id}
          shelf={shelf}
          onSlotSelect={onSlotSelect}
          selectedSlotId={selectedSlotId}
        />
      ))}

      <OrbitControls
        enablePan
        enableZoom
        enableRotate
        minDistance={100}
        maxDistance={1000}
        maxPolarAngle={Math.PI / 2.1}
      />
    </>
  );
};

const SlotInfoPanel = ({ slot, onClose }) => {
  if (!slot) return null;

  const isOccupied = slot.status === 'OCCUPIED';

  return (
    <div
      style={{
        position: 'absolute',
        top: 16,
        right: 16,
        width: 320,
        background: 'rgba(255, 255, 255, 0.98)',
        borderRadius: 8,
        boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
        zIndex: 100,
        overflow: 'hidden',
      }}
    >
      <div
        style={{
          background: isOccupied
            ? materialTypeColors[slot.materialType] || '#1890ff'
            : '#8c8c8c',
          padding: '12px 16px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ color: '#fff', fontWeight: 600, fontSize: 16 }}>
          货位详情
        </span>
        <Button
          type="text"
          icon={<CloseOutlined />}
          onClick={onClose}
          style={{ color: '#fff' }}
          size="small"
        />
      </div>
      <div style={{ padding: 16 }}>
        <Descriptions column={1} size="small" labelStyle={{ width: 80 }}>
          <Descriptions.Item label="货架编码">
            <Tag color="blue">{slot.shelfCode}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="货位编码">
            <Tag>{slot.slotCode}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="位置">
            第{slot.rowIndex}行 第{slot.columnIndex}列 第{slot.layerIndex}层
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={isOccupied ? 'success' : 'default'}>
              {isOccupied ? '已占用' : '空闲'}
            </Tag>
          </Descriptions.Item>
        </Descriptions>

        {isOccupied && (
          <>
            <div
              style={{
                borderTop: '1px solid #f0f0f0',
                margin: '12px 0',
                paddingTop: 12,
              }}
            >
              <div style={{ fontWeight: 600, marginBottom: 8, color: '#1890ff' }}>
                物料信息
              </div>
              <Descriptions column={1} size="small" labelStyle={{ width: 80 }}>
                <Descriptions.Item label="物料编码">
                  {slot.materialCode || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="物料名称">
                  {slot.materialName || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="物料类型">
                  <Tag color={materialTypeColors[slot.materialType]}>
                    {materialTypeNames[slot.materialType] || slot.materialType}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>
            </div>
            <div
              style={{
                borderTop: '1px solid #f0f0f0',
                margin: '12px 0',
                paddingTop: 12,
              }}
            >
              <div style={{ fontWeight: 600, marginBottom: 8, color: '#52c41a' }}>
                库存信息
              </div>
              <Descriptions column={1} size="small" labelStyle={{ width: 80 }}>
                <Descriptions.Item label="当前数量">
                  <span style={{ fontSize: 18, fontWeight: 600, color: '#1890ff' }}>
                    {slot.quantity || 0}
                  </span>
                </Descriptions.Item>
                <Descriptions.Item label="最大容量">
                  {slot.maxCapacity || 100}
                </Descriptions.Item>
                <Descriptions.Item label="使用率">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div
                      style={{
                        flex: 1,
                        height: 8,
                        background: '#f0f0f0',
                        borderRadius: 4,
                        overflow: 'hidden',
                      }}
                    >
                      <div
                        style={{
                          width: `${((slot.quantity || 0) / (slot.maxCapacity || 100)) * 100}%`,
                          height: '100%',
                          background: materialTypeColors[slot.materialType] || '#1890ff',
                          borderRadius: 4,
                        }}
                      />
                    </div>
                    <span style={{ minWidth: 45, textAlign: 'right' }}>
                      {Math.round(((slot.quantity || 0) / (slot.maxCapacity || 100)) * 100)}%
                    </span>
                  </div>
                </Descriptions.Item>
              </Descriptions>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

const Warehouse3D = () => {
  const [warehouses, setWarehouses] = useState([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(null);
  const [shelves, setShelves] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchWarehouses();
  }, []);

  useEffect(() => {
    if (selectedWarehouseId) {
      fetchShelves(selectedWarehouseId);
      setSelectedSlot(null);
    }
  }, [selectedWarehouseId]);

  const fetchWarehouses = async () => {
    try {
      const response = await warehouseAPI.getAllEnabled();
      if (response.success && response.data.length > 0) {
        setWarehouses(response.data);
        setSelectedWarehouseId(response.data[0].id);
      }
    } catch (error) {
      // Handle error silently
    }
  };

  const fetchShelves = async (warehouseId) => {
    setLoading(true);
    try {
      const response = await shelfAPI.getByWarehouseId(warehouseId);
      if (response.success) {
        setShelves(response.data);
      }
    } catch (error) {
      // Handle error silently
    } finally {
      setLoading(false);
    }
  };

  const handleSlotSelect = (slot) => {
    setSelectedSlot(slot);
  };

  const handleClosePanel = () => {
    setSelectedSlot(null);
  };

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>3D仓库视图</h2>
        <p>实时查看仓库货架和库存分布情况，点击货位查看详细信息</p>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Select
          style={{ width: 300 }}
          placeholder="选择仓库"
          value={selectedWarehouseId}
          onChange={setSelectedWarehouseId}
        >
          {warehouses.map((w) => (
            <Option key={w.id} value={w.id}>{w.warehouseName}</Option>
          ))}
        </Select>

        <div style={{ marginTop: 16, display: 'flex', gap: 24 }}>
          <span><Badge color={materialTypeColors.RAW_MATERIAL} /> 原材料</span>
          <span><Badge color={materialTypeColors.SEMI_PRODUCT} /> 半制品</span>
          <span><Badge color={materialTypeColors.FINISHED_PRODUCT} /> 产成品</span>
          <span><Badge color={materialTypeColors.EMPTY} /> 空闲</span>
        </div>
      </Card>

      <div style={{ position: 'relative' }}>
        <div className="warehouse-3d-container">
          {loading ? (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
              <Spin size="large" />
            </div>
          ) : shelves.length === 0 ? (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
              <Empty description="暂无货架数据" />
            </div>
          ) : (
            <Canvas
              shadows
              camera={{ position: [300, 200, 300], fov: 60 }}
              style={{ background: 'linear-gradient(180deg, #1a1a2e 0%, #16213e 100%)' }}
            >
              <Suspense fallback={null}>
                <Scene
                  shelves={shelves}
                  onSlotSelect={handleSlotSelect}
                  selectedSlotId={selectedSlot?.id}
                />
              </Suspense>
            </Canvas>
          )}
        </div>
        <SlotInfoPanel slot={selectedSlot} onClose={handleClosePanel} />
      </div>

      <Card style={{ marginTop: 16 }} title="操作说明">
        <ul style={{ margin: 0, paddingLeft: 20, color: '#666' }}>
          <li>点击货位：查看库存和物料详细信息</li>
          <li>鼠标左键拖动：旋转视角</li>
          <li>鼠标右键拖动：平移视角</li>
          <li>鼠标滚轮：缩放视图</li>
          <li>不同颜色代表不同物料类型</li>
        </ul>
      </Card>
    </div>
  );
};

export default Warehouse3D;
