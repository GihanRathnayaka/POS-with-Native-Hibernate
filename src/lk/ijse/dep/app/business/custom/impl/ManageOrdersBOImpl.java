package lk.ijse.dep.app.business.custom.impl;

import lk.ijse.dep.app.business.Converter;
import lk.ijse.dep.app.business.custom.ManageOrdersBO;
import lk.ijse.dep.app.dao.DAOFactory;
import lk.ijse.dep.app.dao.custom.*;
import lk.ijse.dep.app.dto.ItemDTO;
import lk.ijse.dep.app.dto.OrderDTO;
import lk.ijse.dep.app.dto.OrderDTO2;
import lk.ijse.dep.app.dto.OrderDetailDTO;
import lk.ijse.dep.app.entity.*;
import lk.ijse.dep.app.util.HibernateUtil;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageOrdersBOImpl implements ManageOrdersBO {

    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private ItemDAO itemDAO;
    private QueryDAO queryDAO;
    private CustomerDAO customerDAO;

    public ManageOrdersBOImpl() {
        orderDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER);
        orderDetailDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER_DETAIL);
        itemDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ITEM);
        queryDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
        customerDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.CUSTOMER);
    }

    @Override
    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {
        Session mySession = HibernateUtil.getSessionFactory().openSession();

        queryDAO.setSession(mySession);
        return queryDAO.findAllOrdersWithCustomerNameAndTotal().map(ce -> {
            return Converter.getDTOList(ce, OrderDTO2.class);
        }).get();

    }

    @Override
    public List<OrderDTO> getOrders() throws Exception {
        Session mySession = HibernateUtil.getSessionFactory().openSession();
        try (Session session = mySession) {
            orderDAO.setSession(session);
            session.beginTransaction();
            List<OrderDTO> orderDTOS = orderDAO.findAll().map(Converter::<OrderDTO>getDTOList).get();
            session.getTransaction().commit();
            return orderDTOS;
        } catch (Exception ex) {
            mySession.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public String generateOrderId() throws Exception {
        Session mySession = HibernateUtil.getSessionFactory().openSession();
        queryDAO.setSession(mySession);
        int count = queryDAO.count();

        return "O00" + (++count);
    }

    @Override
    public void createOrder(OrderDTO dto) throws Exception {

        Session mySession = HibernateUtil.getSessionFactory().openSession();
        try (Session session = mySession) {
            session.beginTransaction();
            Order order = Converter.getEntity(dto);
            customerDAO.setSession(session);

            Customer customer = customerDAO.find(dto.getCustomerId()).get();
            order.setCustomerId(customer);
            orderDAO.setSession(session);
            orderDAO.save(order);
//            System.out.println("xxxxxxxxxxxxx"+session);
            for (OrderDetailDTO dao : dto.getOrderDetailDTOS()) {
                System.out.println("xxxxxxxxxxxxx" + session);
                OrderDetail od = Converter.getEntity(dao);
                od.setOrderDetailPK(new OrderDetailPK(dto.getId(), dao.getCode()));
                orderDetailDAO.setSession(session);
                orderDetailDAO.save(od);
                itemDAO.setSession(session);
                Item item = itemDAO.find(dao.getCode()).get();
                item.setQtyOnHand(item.getQtyOnHand() - dao.getQty());
            }


            session.getTransaction().commit();

        } catch (Exception ex) {
            mySession.getTransaction().rollback();
            throw ex;
        }


    }

    @Override
    public OrderDTO findOrder(String orderId) throws Exception {
//        Session mySession = HibernateUtil.getSessionFactory().openSession();
//        try(Session session = mySession){
//            orderDAO.setSession(session);
//            session.beginTransaction();
//            OrderDTO orderDTO= orderDAO.find(orderId).map(Converter::<OrderDTO>getDTO).orElse(null);
//            session.getTransaction().commit();
//            return orderDTO;
//        }catch(Exception ex){
//            mySession.getTransaction().rollback();
//            throw ex;
//        }

         Session mySession = HibernateUtil.getSessionFactory().openSession();


        orderDAO.setSession(mySession);
        OrderDTO orderDTO = orderDAO.find(orderId).map(Converter::<OrderDTO>getDTO).orElse(null);
        orderDetailDAO.setSession(mySession);
        List<OrderDetail> entities = orderDetailDAO.findAll().get();
        List<OrderDetailDTO> list = new ArrayList<>();
        entities.forEach(c -> {
            if (orderId.equals(c.getOrderDetailPK().getOrderId())) {
                list.add(new OrderDetailDTO(c.getOrderDetailPK().getItemCode(), c.getItemId().getDescription(), c.getQty(), c.getUnitPrice()));
            }
        });

        orderDTO.setOrderDetailDTOS(list);
        return orderDTO;


    }

//    private OrderDAO orderDAO;
//    private OrderDetailDAO orderDetailDAO;
//    private ItemDAO itemDAO;
//    private QueryDAO queryDAO;
//
//    public ManageOrdersBOImpl() {
//        orderDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER);
//        orderDetailDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER_DETAIL);
//        itemDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ITEM);
//        queryDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
//    }
//
//    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {
//
//        return queryDAO.findAllOrdersWithCustomerNameAndTotal().map(ce -> {
//            return Converter.getDTOList(ce, OrderDTO2.class);
//        }).get();
//
//    }
//
//    public List<OrderDTO> getOrders() throws Exception {
//
//        List<Order> orders = orderDAO.findAll().get();
//        ArrayList<OrderDTO> tmpDTOs = new ArrayList<>();
//
//        for (Order order : orders) {
//            List<OrderDetailDTO> tmpOrderDetailsDtos = queryDAO.findOrderDetailsWithItemDescriptions(order.getId()).map(ce -> {
//                return Converter.getDTOList(ce, OrderDetailDTO.class);
//            }).get();
//
//            OrderDTO dto = new OrderDTO(order.getId(),
//                    order.getDate().toLocalDate(),
//                    order.getCustomerId(), tmpOrderDetailsDtos);
//            tmpDTOs.add(dto);
//        }
//
//        return tmpDTOs;
//    }
//
//    public String generateOrderId() throws Exception {
//        return orderDAO.count() + 1 + "";
//    }
//
//    public void createOrder(OrderDTO dto) throws Exception {
//
//        DBConnection.getConnection().setAutoCommit(false);
//
//        try {
//
//            boolean result = orderDAO.save(new Order(dto.getId(), Date.valueOf(dto.getDate()), dto.getCustomerId()));
//
//            if (!result) {
//                return;
//            }
//
//            for (OrderDetailDTO detailDTO : dto.getOrderDetailDTOS()) {
//                result = orderDetailDAO.save(new OrderDetail(dto.getId(),
//                        detailDTO.getCode(), detailDTO.getQty(), detailDTO.getUnitPrice()));
//
//                if (!result) {
//                    DBConnection.getConnection().rollback();
//                    return;
//                }
//
//                Item item = itemDAO.find(detailDTO.getCode()).get();
//                int qty = item.getQtyOnHand() - detailDTO.getQty();
//                item.setQtyOnHand(qty);
//                itemDAO.update(item);
//
//            }
//
//            DBConnection.getConnection().commit();
//
//        } catch (Exception ex) {
//            DBConnection.getConnection().rollback();
//            ex.printStackTrace();
//        } finally {
//            DBConnection.getConnection().setAutoCommit(true);
//        }
//
//    }
//
//    public OrderDTO findOrder(String orderId) throws Exception {
//        Order order = orderDAO.find(orderId).get();
//
//        List<OrderDetailDTO> tmpOrderDetailsDtos = queryDAO.findOrderDetailsWithItemDescriptions(order.getId()).map(ce -> {
//            return Converter.getDTOList(ce, OrderDetailDTO.class);
//        }).get();
//
//        return new OrderDTO(order.getId(), order.getDate().toLocalDate(), order.getCustomerId(), tmpOrderDetailsDtos);
//    }
}
