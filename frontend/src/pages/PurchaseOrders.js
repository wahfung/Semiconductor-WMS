import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space, Tag, message, Popconfirm, DatePicker } from 'antd';
import { PlusOutlined, CheckOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import { purchaseOrderAPI, materialAPI } from '../services/api';
import dayjs from 'dayjs';

const { Option } = Select;

const statusColors = {
  PENDING: 'orange',
  APPROVED: 'blue',
  COMPLETED: 'green',
  CANCELLED: 'default',
};

const statusLabels = {
  PENDING: '待审批',
  APPROVED: '已审批',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

const PurchaseOrders = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
    fetchMaterials();
  }, [pagination.current, pagination.pageSize]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await purchaseOrderAPI.getAll({
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
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  const fetchMaterials = async () => {
    try {
      const response = await materialAPI.getAllEnabled();
      if (response.success) {
        setMaterials(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch materials:', error);
    }
  };

  const handleAdd = () => {
    form.resetFields();
    form.setFieldsValue({ items: [{}] });
    setModalVisible(true);
  };

  const handleView = async (record) => {
    try {
      const response = await purchaseOrderAPI.getById(record.id);
      if (response.success) {
        setCurrentRecord(response.data);
        setDetailVisible(true);
      }
    } catch (error) {
      message.error('获取详情失败');
    }
  };

  const handleApprove = async (id) => {
    try {
      const response = await purchaseOrderAPI.approve(id);
      if (response.success) {
        message.success('审批成功');
        fetchData();
      }
    } catch (error) {
      message.error('审批失败');
    }
  };

  const handleCancel = async (id) => {
    try {
      const response = await purchaseOrderAPI.cancel(id);
      if (response.success) {
        message.success('取消成功');
        fetchData();
      }
    } catch (error) {
      message.error('取消失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const submitData = {
        ...values,
        expectedArrivalTime: values.expectedArrivalTime?.format('YYYY-MM-DDTHH:mm:ss'),
      };
      const response = await purchaseOrderAPI.create(submitData);
      if (response.success) {
        message.success('创建成功');
        setModalVisible(false);
        fetchData();
      }
    } catch (error) {
      message.error('创建失败');
    }
  };

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '供应商', dataIndex: 'supplier', key: 'supplier', width: 150 },
    { title: '联系人', dataIndex: 'contact', key: 'contact', width: 100 },
    { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, render: (v) => v ? `¥${v.toFixed(2)}` : '-' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => <Tag color={statusColors[status]}>{statusLabels[status]}</Tag>,
    },
    { title: '创建人', dataIndex: 'creatorName', key: 'creatorName', width: 100 },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      render: (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>查看</Button>
          {record.status === 'PENDING' && (
            <>
              <Popconfirm title="确定审批通过吗？" onConfirm={() => handleApprove(record.id)}>
                <Button type="link" icon={<CheckOutlined />} style={{ color: '#52c41a' }}>审批</Button>
              </Popconfirm>
              <Popconfirm title="确定取消吗？" onConfirm={() => handleCancel(record.id)}>
                <Button type="link" danger icon={<CloseOutlined />}>取消</Button>
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>采购管理</h2>
        <p>管理物料采购订单</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建采购单
          </Button>
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
          scroll={{ x: 1200 }}
        />
      </div>

      <Modal
        title="新建采购单"
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={800}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="supplier" label="供应商" rules={[{ required: true }]}>
            <Input placeholder="请输入供应商名称" />
          </Form.Item>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="contact" label="联系人" style={{ width: 200 }}>
              <Input placeholder="联系人" />
            </Form.Item>
            <Form.Item name="phone" label="联系电话" style={{ width: 200 }}>
              <Input placeholder="联系电话" />
            </Form.Item>
            <Form.Item name="expectedArrivalTime" label="预计到货时间" style={{ width: 200 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
          </Space>
          <Form.List name="items" initialValue={[{}]}>
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                    <Form.Item {...restField} name={[name, 'materialId']} rules={[{ required: true, message: '请选择物料' }]}>
                      <Select style={{ width: 200 }} placeholder="选择物料">
                        {materials.map((m) => (
                          <Option key={m.id} value={m.id}>{m.materialCode} - {m.materialName}</Option>
                        ))}
                      </Select>
                    </Form.Item>
                    <Form.Item {...restField} name={[name, 'quantity']} rules={[{ required: true, message: '请输入数量' }]}>
                      <InputNumber min={1} placeholder="数量" />
                    </Form.Item>
                    <Form.Item {...restField} name={[name, 'unitPrice']} rules={[{ required: true, message: '请输入单价' }]}>
                      <InputNumber min={0} precision={2} placeholder="单价" />
                    </Form.Item>
                    {fields.length > 1 && (
                      <Button type="link" danger onClick={() => remove(name)}>删除</Button>
                    )}
                  </Space>
                ))}
                <Button type="dashed" onClick={() => add()} block>添加物料</Button>
              </>
            )}
          </Form.List>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="采购单详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentRecord && (
          <div>
            <p><strong>订单号：</strong>{currentRecord.orderNo}</p>
            <p><strong>供应商：</strong>{currentRecord.supplier}</p>
            <p><strong>状态：</strong><Tag color={statusColors[currentRecord.status]}>{statusLabels[currentRecord.status]}</Tag></p>
            <p><strong>总金额：</strong>¥{currentRecord.totalAmount?.toFixed(2)}</p>
            <Table
              dataSource={currentRecord.items}
              rowKey="id"
              pagination={false}
              size="small"
              columns={[
                { title: '物料编码', dataIndex: 'materialCode' },
                { title: '物料名称', dataIndex: 'materialName' },
                { title: '数量', dataIndex: 'quantity' },
                { title: '单价', dataIndex: 'unitPrice', render: (v) => `¥${v}` },
                { title: '金额', dataIndex: 'amount', render: (v) => `¥${v}` },
              ]}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PurchaseOrders;
