package com.progresssoft.fxdeals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "deals")
public class Deal {

    @Id
    @Column(name = "deal_unique_id", nullable = false, updatable = false, length = 100)
    private String dealUniqueId;

    @Column(name = "from_currency_iso_code", nullable = false, length = 3)
    private String fromCurrencyIsoCode;

    @Column(name = "to_currency_iso_code", nullable = false, length = 3)
    private String toCurrencyIsoCode;

    @Column(name = "deal_timestamp", nullable = false)
    private OffsetDateTime dealTimestamp;

    @Column(name = "deal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal dealAmount;

    public String getDealUniqueId() { return dealUniqueId; }
    public void setDealUniqueId(String dealUniqueId) { this.dealUniqueId = dealUniqueId; }

    public String getFromCurrencyIsoCode() { return fromCurrencyIsoCode; }
    public void setFromCurrencyIsoCode(String fromCurrencyIsoCode) { this.fromCurrencyIsoCode = fromCurrencyIsoCode; }

    public String getToCurrencyIsoCode() { return toCurrencyIsoCode; }
    public void setToCurrencyIsoCode(String toCurrencyIsoCode) { this.toCurrencyIsoCode = toCurrencyIsoCode; }

    public OffsetDateTime getDealTimestamp() { return dealTimestamp; }
    public void setDealTimestamp(OffsetDateTime dealTimestamp) { this.dealTimestamp = dealTimestamp; }

    public BigDecimal getDealAmount() { return dealAmount; }
    public void setDealAmount(BigDecimal dealAmount) { this.dealAmount = dealAmount; }
}
