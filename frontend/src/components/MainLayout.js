import React, { useState } from "react";
import { Layout, Menu, Dropdown, Avatar, Space, Button } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import {
  DashboardOutlined,
  AppstoreOutlined,
  HomeOutlined,
  InboxOutlined,
  BoxPlotOutlined,
  ShoppingCartOutlined,
  ImportOutlined,
  ExportOutlined,
  AuditOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from "@ant-design/icons";
import { useAuthStore } from "../utils/store";

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: "/dashboard", icon: <DashboardOutlined />, label: "工作台" },
  { key: "/materials", icon: <AppstoreOutlined />, label: "物料管理" },
  { key: "/warehouses", icon: <HomeOutlined />, label: "仓库管理" },
  { key: "/shelves", icon: <InboxOutlined />, label: "货架管理" },
  { key: "/warehouse-3d", icon: <BoxPlotOutlined />, label: "3D仓库视图" },
  { key: "/inventory", icon: <AppstoreOutlined />, label: "库存查询" },
  { type: "divider" },
  {
    key: "/purchase-orders",
    icon: <ShoppingCartOutlined />,
    label: "采购管理",
  },
  { key: "/inbound-orders", icon: <ImportOutlined />, label: "入库管理" },
  { key: "/outbound-orders", icon: <ExportOutlined />, label: "出库管理" },
  { key: "/stocktaking-orders", icon: <AuditOutlined />, label: "盘点管理" },
];

const MainLayout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();

  const handleMenuClick = ({ key }) => {
    navigate(key);
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const userMenuItems = [
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "退出登录",
      onClick: handleLogout,
    },
  ];

  return (
    <Layout className="layout-container">
      <Header className="layout-header">
        <div className="header-logo">
          <BoxPlotOutlined style={{ fontSize: 28, color: "#fff" }} />
          <h1>半导体仓库管理系统</h1>
        </div>
        <div className="header-user">
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: "pointer" }}>
              <Avatar
                icon={<UserOutlined />}
                style={{ backgroundColor: "#fff", color: "#1890ff" }}
              />
              <span>{user?.realName || "管理员"}</span>
            </Space>
          </Dropdown>
        </div>
      </Header>
      <Layout>
        <Sider
          className="layout-sider"
          width={220}
          collapsible
          collapsed={collapsed}
          onCollapse={setCollapsed}
          trigger={null}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ width: "100%", height: 48, borderRadius: 0 }}
          />
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={handleMenuClick}
            style={{ borderRight: 0, paddingTop: 8 }}
          />
        </Sider>
        <Content className="layout-content">{children}</Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
