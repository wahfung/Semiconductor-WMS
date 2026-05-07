import React, { useState, useEffect } from 'react';
import { Table, Select, Tag, Space, Input } from 'antd';
import { inventoryAPI, warehouseAPI } from '../services/api';

const { Option } = Select;

const materialTypeColors = {
  RAW_MATERIAL: 'blue',
  SEMI_PRODUCT: 'orange',
  FINISHED_PRODUCT: 'green',
};

const materialTypeLabels = {
  RAW_MATERIAL: '原材料',
  SEMI_PRODUCT: '半制品',
  FINISHED_PRODUCT: '产成品',
};

const Inventory = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    fetchWarehouses();
    fetchData();
  }, []);

  useEffect(() => {
    if (selectedWarehouseId) {
      fetchDataByWarehouse(selectedWarehouseId);
    } else {
      fetchData();
    }
  }, [selectedWarehouseId, pagination.current, pagination.pageSize]);

  const fetchWarehouses = async () => {
    try {
      const response = await warehouseAPI.getAllEnabled();
      if (response.success) {
        setWarehouses(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch warehouses:', error);
    }
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await inventoryAPI.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      if (response.success) {
        setData(response.data);
        setPagination((prev) => ({
          ...prev,
          total: response.pageInfo?.total || 0,
        }));
      }
    } catch (error) {
      console.error('Failed to fetch inventory:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchDataByWarehouse = async (warehouseId) => {
    setLoading(true);
    try {
      const response = await inventoryAPI.getByWarehouseId(warehouseId);
      if (response.success) {
        setData(response.data);
        setPagination((prev) => ({
          ...prev,
          total: response.data.length,
        }));
      }
    } catch (error) {
      console.error('Failed to fetch inventory:', error);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120 },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', width: 150 },
    {
      title: '类型',
      dataIndex: 'materialType',
      key: 'materialType',
      width: 100,
      render: (type) => (
        <Tag color={materialTypeColors[type]}>{materialTypeLabels[type] || type}</Tag>
      ),
    },
    { title: '规格', dataIndex: 'specification', key: 'specification', width: 150 },
    { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 120 },
    { title: '货架', dataIndex: 'shelfCode', key: 'shelfCode', width: 130 },
    { title: '货位', dataIndex: 'slotCode', key: 'slotCode', width: 150 },
    { title: '批次号', dataIndex: 'batchNo', key: 'batchNo', width: 150 },
    {
      title: '数量',
      key: 'quantity',
      width: 150,
      render: (_, record) => (
        <Space>
          <span style={{ color: '#1890ff', fontWeight: 'bold' }}>{record.quantity}</span>
          <span style={{ color: '#999' }}>
            (可用: {record.availableQuantity} / 锁定: {record.lockedQuantity})
          </span>
        </Space>
      ),
    },
    { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        <Tag color={status === 'NORMAL' ? 'green' : 'orange'}>
          {status === 'NORMAL' ? '正常' : status}
        </Tag>
      ),
    },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>库存查询</h2>
        <p>查询仓库库存信息和分布情况</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Select
            style={{ width: 200 }}
            placeholder="选择仓库"
            allowClear
            value={selectedWarehouseId}
            onChange={setSelectedWarehouseId}
          >
            {warehouses.map((w) => (
              <Option key={w.id} value={w.id}>{w.warehouseName}</Option>
            ))}
          </Select>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize }),
          }}
          scroll={{ x: 1500 }}
        />
      </div>
    </div>
  );
};

export default Inventory;
