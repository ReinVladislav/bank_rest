package com.example.bankcards.util.mappers;

import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.CardWithOwnerResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.components.CardNumberUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CardMapper {

    @Mapping(source = "encryptNumber", target = "maskedNumber", qualifiedByName = "maskNumber")
    CardWithOwnerResponse toResponseWithOwner(Card card, @Context CardNumberUtil util);

    @Named("maskNumber")
    default String maskNumber(String encryptNumber, @Context CardNumberUtil util) {
        return util.getCardNumberWithMask(encryptNumber);
    }

    @Mapping(source = "encryptNumber", target = "maskedNumber", qualifiedByName = "maskNumber")
    CardResponse toResponse(Card card, @Context CardNumberUtil util);
}
