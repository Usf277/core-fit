package com.corefit.service.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.MonthlyIncomeComparison;
import com.corefit.dto.response.market.TopProductStats;
import com.corefit.entity.auth.User;
import com.corefit.entity.market.Market;
import com.corefit.entity.market.Order;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
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

    public GeneralResponse<?> getProviderStats(HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType().equals(UserType.GENERAL)) {
            throw new GeneralException("User is not provider");
        }

        List<Long> marketIds = user.getMarket().stream()
                .map(Market::getId)
                .toList();

        List<TopProductStats> topProducts = orderItemRepository.findTopProductsByMarketIds(marketIds).stream().limit(3).toList();


        Map<String, Object> data = new HashMap<>();
        data.put("topProducts", topProducts);
        data.put("incomeComparison", getMonthlyIncome(user));

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
                double orderIncome = order.getTotalPrice();

                MonthlyIncomeComparison monthly = incomeByMonth.get(month);
                monthly.setStoreIncome(monthly.getStoreIncome() + orderIncome);
            }
        }

        Set<Playground> playgrounds = user.getPlaygrounds();

        for (Playground playground : playgrounds) {
            for (Reservation reservation : playground.getReservations()) {
                if (reservation.getCreatedAt() == null) continue;

                int month = reservation.getCreatedAt().getMonthValue();
                double resIncome = reservation.getPrice();

                MonthlyIncomeComparison monthly = incomeByMonth.get(month);
                monthly.setReservationIncome(monthly.getReservationIncome() + resIncome);
            }
        }

        return new ArrayList<>(incomeByMonth.values());
    }
}
