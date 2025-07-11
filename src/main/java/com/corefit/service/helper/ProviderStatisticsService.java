package com.corefit.service.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.market.TopProductStats;
import com.corefit.entity.auth.User;
import com.corefit.entity.market.Market;
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

        List<TopProductStats> topProducts = orderItemRepository.findTopProductsByMarketIds(marketIds)  .stream().limit(3).toList();;

//        List<Map<String, Object>> formatted = topProducts.stream().map(stat -> {
//            Map<String, Object> map = new HashMap<>();
//            map.put("id", stat.getId());
//            map.put("label", stat.getLabel());
//            map.put("value", stat.getValue());
//            return map;
//        }).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("topProducts", topProducts);

        return new GeneralResponse<>("Success", data);
    }

}
