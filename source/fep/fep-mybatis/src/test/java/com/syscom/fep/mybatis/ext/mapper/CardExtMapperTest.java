package com.syscom.fep.mybatis.ext.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CardExtMapperTest extends MybatisBaseTest {
	@Autowired
	private CardExtMapper mapper;
	private Card card;

	@BeforeEach
	public void setup() {
		card = new Card();
		card.setCardActno("1");
		card.setCardCardSeq((short) 2);
		card.setCardStatus((short) 2);
		card.setCardCombine((short) 2);
		card.setCardCreditno("1");
	}

	@Test
	public void testGetNcCardByStatus() {
		List<Card> list = mapper.getNcCardByStatus("0081201000244448", (short) 0);
		assertTrue(CollectionUtils.isNotEmpty(list));
	}

	@Test
	public void testQueryNcCardByStatus() {

		Card card1 = mapper.queryNcCardByStatus("0081201000244448", (short) 0);
		assertNotNull(card1);
	}

	@Test
	public void testQueryByCreditNoWithMaxCardSeq() {
		Card card1 = mapper.queryByCreditNoWithMaxCardSeq("5184880000008302");
		assertNotNull(card1);

	}

	@Test
	public void testGetSingleCard() {
		Card card1 = mapper.getSingleCard("8070260001798301");
		assertNotNull(card1);
	}

	@Test
	public void testQueryStatusNot5678ByActno() {
		int actNo = mapper.queryStatusNot5678ByActno("0081201000244448", (short) 0);
		assertEquals(0, actNo);
	}

	@Test
	public void testGetMaxCardSeq() {
		Short cardSeq= mapper.getMaxCardSeq("0003900400010862", "4");
		assertEquals((short)14, cardSeq);
	}

	@Test
	public void testGetOldCardByMaxCardSeq() {
		card.setCardActno("0000100400081549");
		card.setCardCardSeq((short)3);
		card.setCardStatus((short)4);
		card.setCardCombine((short)6);
		card.setCardCreditno("5148729602662104");
		Card card1 = mapper.getOldCardByMaxCardSeq(card);
		assertNotNull(card1);
	}

	@Test
	public void testGetCardByCreditNoStatus() {
		card.setCardCardSeq((short)3);
		card.setCardStatus((short)4);
		card.setCardCreditno("5148729602662104");
		Card card1 = mapper.getCardByCreditNoStatus(card);
		assertNotNull(card1);
	}
}
