package lk.ijse.dep.app.dao.custom.impl;

import lk.ijse.dep.app.dao.custom.QueryDAO;
import lk.ijse.dep.app.entity.CustomEntity;
import lk.ijse.dep.app.entity.Order;
import lk.ijse.dep.app.entity.OrderDetail;
import org.hibernate.Session;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuaryDaoImpl implements QueryDAO {

    private Session  session;

    @Override
    public Optional<List<OrderDetail>> findOrderDetailsWithItemDescriptions(String orderId) throws Exception {
        String quary = "select o FROM  OrderDetail o Where o.orderId=' "+orderId+"'";
        List<OrderDetail> resultList = session.createQuery(quary, OrderDetail.class).getResultList();
        System.out.println(resultList);
        return Optional.ofNullable(resultList);

    }

    @Override
    public Optional<List<CustomEntity>> findAllOrdersWithCustomerNameAndTotal() throws Exception {

        String quary = "SELECT o.id,o.date,c.id,c.name,SUM(d.unitPrice * d.unitPrice) as total FROM Orders o LEFT JOIN Customer c on o.id = c.id LEFT JOIN OrderDetail  d on o.id = d.orderId group by o.id,o.date,c.id";

        List<Order> orders = session.createQuery("select o FROM Order o", Order.class).getResultList();
        List<CustomEntity> ce = new ArrayList<>();
        for (Order o : orders) {
            List resultList = session.createQuery(" select sum (o.unitPrice * o.qty) FROM OrderDetail o WHERE o.orderId='" + o.getId() + "'GROUP BY o.orderId ").getResultList();

            System.out.println(resultList.get(0).toString());
            double i = Double.parseDouble(resultList.get(0).toString());

            ce.add(new CustomEntity(o.getId(), o.getDate(), o.getCustomerId().getId(), o.getCustomerId().getName()
                    , i));
        }


        System.out.println(orders);
        System.out.println(orders.get(0));

        return Optional.ofNullable(ce);

    }

    @Override
    public int count() throws Exception {
        System.out.println(session);
        Query query = session.createQuery("SELECT count(o.id) FROM Order o");
        List resultList = query.getResultList();

        System.out.println(resultList.get(0));

        return Integer.parseInt(resultList.get(0).toString());
    }

    public void setSession(Session session){
        this.session=session;
    }
}
