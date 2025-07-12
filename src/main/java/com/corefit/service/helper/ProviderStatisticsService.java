package com.corefit.service.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.MonthlyIncomeComparison;
import com.corefit.dto.response.ProviderMonthOverView;
import com.corefit.entity.auth.User;
import com.corefit.entity.market.Market;
import com.corefit.entity.market.Order;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import com.corefit.enums.OrderStatus;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.market.OrderItemRepo;
import com.corefit.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProviderStatisticsService {
    @Autowired
    private AuthService authService;

    @Autowired
    private OrderItemRepo orderItemRepository;

    public GeneralResponse<?> getProviderStats(Integer month, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType().equals(UserType.GENERAL)) {
            throw new GeneralException("User is not a provider");
        }

        int previousMonth = month == 1 ? 12 : month - 1;

        Map<Integer, Integer> totalOrdersMap = new HashMap<>();
        Map<Integer, Integer> cancelledOrdersMap = new HashMap<>();
        Map<Integer, Integer> deliveredOrdersMap = new HashMap<>();
        Map<Integer, Integer> totalReservationsMap = new HashMap<>();
        Map<Integer, Integer> cancelledReservationsMap = new HashMap<>();
        Map<Integer, Double> storeIncomeMap = new HashMap<>();
        Map<Integer, Double> playgroundIncomeMap = new HashMap<>();

        for (int i = 1; i <= 12; i++) {
            totalOrdersMap.put(i, 0);
            cancelledOrdersMap.put(i, 0);
            deliveredOrdersMap.put(i, 0);
            totalReservationsMap.put(i, 0);
            cancelledReservationsMap.put(i, 0);
            storeIncomeMap.put(i, 0.0);
            playgroundIncomeMap.put(i, 0.0);
        }

        for (Market market : user.getMarket()) {
            for (Order order : market.getOrders()) {
                if (order.getCreatedAt() == null) continue;

                int orderMonth = order.getCreatedAt().getMonthValue();

                totalOrdersMap.put(orderMonth, totalOrdersMap.get(orderMonth) + 1);

                if (order.getStatus().equals(OrderStatus.ORDER_CANCELED)) {
                    cancelledOrdersMap.put(orderMonth, cancelledOrdersMap.get(orderMonth) + 1);
                } else if (order.getStatus().equals(OrderStatus.ORDER_DELIVERED)) {
                    deliveredOrdersMap.put(orderMonth, deliveredOrdersMap.get(orderMonth) + 1);
                    storeIncomeMap.put(orderMonth, storeIncomeMap.get(orderMonth) + order.getTotalPrice());
                }
            }
        }

        for (Playground playground : user.getPlaygrounds()) {
            for (Reservation reservation : playground.getReservations()) {
                if (reservation.getCreatedAt() == null) continue;

                int resMonth = reservation.getCreatedAt().getMonthValue();

                totalReservationsMap.put(resMonth, totalReservationsMap.get(resMonth) + 1);

                if (reservation.isCancelled()) {
                    cancelledReservationsMap.put(resMonth, cancelledReservationsMap.get(resMonth) + 1);
                } else if (reservation.isEnded()) {
                    playgroundIncomeMap.put(resMonth, playgroundIncomeMap.get(resMonth) + reservation.getPrice());
                }
            }
        }

        Map<String, Object> data = new HashMap<>();

        List<ProviderMonthOverView> overviewCards = List.of(
                createCard("Total Orders", totalOrdersMap.get(month), calculateChangeRate(totalOrdersMap.get(month), totalOrdersMap.get(previousMonth))),
                createCard("Cancelled Orders", cancelledOrdersMap.get(month), calculateChangeRate(cancelledOrdersMap.get(month), cancelledOrdersMap.get(previousMonth))),
                createCard("Delivered Orders", deliveredOrdersMap.get(month), calculateChangeRate(deliveredOrdersMap.get(month), deliveredOrdersMap.get(previousMonth))),
                createCard("Total Reservations", totalReservationsMap.get(month), calculateChangeRate(totalReservationsMap.get(month), totalReservationsMap.get(previousMonth))),
                createCard("Cancelled Reservations", cancelledReservationsMap.get(month), calculateChangeRate(cancelledReservationsMap.get(month), cancelledReservationsMap.get(previousMonth))),
                createCard("Store Income", storeIncomeMap.get(month), calculateChangeRate(storeIncomeMap.get(month), storeIncomeMap.get(previousMonth))),
                createCard("Playground Income", playgroundIncomeMap.get(month), calculateChangeRate(playgroundIncomeMap.get(month), playgroundIncomeMap.get(previousMonth)))
        );

        data.put("overview", overviewCards);
        data.put("topProducts", orderItemRepository.findTopProductsByMarketIds(user.getMarket().stream().map(Market::getId).toList()).stream().limit(3).toList());
        data.put("monthlyIncome", getMonthlyIncome(user));

        return new GeneralResponse<>("Success", data);
    }

    private List<MonthlyIncomeComparison> getMonthlyIncome(User user) {
        Map<Integer, MonthlyIncomeComparison> incomeByMonth = new HashMap<>();

        String[] monthNames = {
                "Jan", "Feb", "Mar", "Apr", "May", "June",
                "July", "Aug", "Sept", "Oct", "Nov", "Dec"
        };

        for (int i = 1; i <= 12; i++) {
            incomeByMonth.put(i, new MonthlyIncomeComparison(monthNames[i - 1], 0.0, 0.0));
        }

        for (Market market : user.getMarket()) {
            for (Order order : market.getOrders()) {
                if (order.getCreatedAt() == null) continue;

                int month = order.getCreatedAt().getMonthValue();
                incomeByMonth.get(month).setStoreIncome(
                        incomeByMonth.get(month).getStoreIncome() + order.getTotalPrice()
                );
            }
        }

        for (Playground playground : user.getPlaygrounds()) {
            for (Reservation reservation : playground.getReservations()) {
                if (reservation.getCreatedAt() == null) continue;

                int month = reservation.getCreatedAt().getMonthValue();
                incomeByMonth.get(month).setReservationIncome(
                        incomeByMonth.get(month).getReservationIncome() + reservation.getPrice()
                );
            }
        }

        return new ArrayList<>(incomeByMonth.values());
    }

    private ProviderMonthOverView createCard(String title, Number count, int changeRate) {
        ProviderMonthOverView card = new ProviderMonthOverView();
        card.setTitle(title);
        card.setCount(count.longValue());
        card.setChangeRate(changeRate);
        return card;
    }

    private int calculateChangeRate(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current.doubleValue() > 0 ? 100 : 0;
        }
        double rate = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        return (int) Math.round(rate);
    }
}
