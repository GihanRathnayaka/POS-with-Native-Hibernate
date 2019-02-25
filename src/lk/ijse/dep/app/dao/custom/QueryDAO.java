package lk.ijse.dep.app.dao.custom;

import lk.ijse.dep.app.dao.SuperDAO;
import lk.ijse.dep.app.entity.CustomEntity;
import lk.ijse.dep.app.entity.OrderDetail;

import java.util.List;
import java.util.Optional;

public interface QueryDAO extends SuperDAO {

    Optional<List<OrderDetail>> findOrderDetailsWithItemDescriptions(String orderId) throws Exception;

    Optional<List<CustomEntity>> findAllOrdersWithCustomerNameAndTotal() throws Exception;

    int count() throws Exception;


}
