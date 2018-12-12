package org.obsplatform.finance.clientbalance.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.api.JsonCommand;

@Entity
@Table(name = "b_client_balance")
public class ClientBalance {

	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;

	@Column(name = "client_id", nullable = false, length = 20)
	private Long clientId;

	@Column(name = "balance_amount", nullable = false, length = 20)
	private BigDecimal balanceAmount;
	
	@Column(name = "wallet_amount", nullable = false, length = 20)
	private BigDecimal walletAmount;



	public static ClientBalance create(Long clientId,BigDecimal balanceAmount, char isWalletPayment) {
		
		return new ClientBalance(clientId, balanceAmount,isWalletPayment);
	}

	public ClientBalance(Long clientId, BigDecimal balanceAmount, char isWalletPayment) {

		this.clientId = clientId;
		if(isWalletPayment == 'Y'){
			this.walletAmount= balanceAmount;
			this.balanceAmount=	BigDecimal.ZERO;
		}else{
		   this.balanceAmount = balanceAmount;
		   this.walletAmount= BigDecimal.ZERO;
		}
	}

	

public ClientBalance()
{

}
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public BigDecimal getBalanceAmount() {
		return balanceAmount;
	}

	public Long getId() {
		return id;
	}

	public void setBalanceAmount(BigDecimal balanceAmount, char iswalletEnable) {
		if(iswalletEnable == 'Y'){
			if(this.walletAmount != null)
			 this.walletAmount=this.walletAmount.add(balanceAmount);
			else
				this.walletAmount=BigDecimal.ZERO.add(balanceAmount);
		}else{
			 this.balanceAmount=this.balanceAmount.add(balanceAmount);
		}
	}

	public void updateClient(Long clientId){
		this.clientId = clientId;
	}

	


	public BigDecimal getWalletAmount() {
		return walletAmount;
	}

	public void updateDueAmount(BigDecimal dueAmount) {


	}

	public static ClientBalance fromJson(JsonCommand command) {
		
	    final Long clientId= command.longValueOfParameterNamed("clientId");
	    final BigDecimal balance = command.bigDecimalValueOfParameterNamed("balance");
	    return new ClientBalance(clientId,balance,'N');
	}

	public void updateBalance(String paymentType, BigDecimal amountPaid,char isWalletPayment) {
		 
		if("CREDIT".equalsIgnoreCase(paymentType)){
			  if(isWalletPayment == 'Y'){
				  if(this.walletAmount != null)
				    this.walletAmount=this.walletAmount.subtract(amountPaid);
				  else
					  this.walletAmount=BigDecimal.ZERO.subtract(amountPaid);
			  }else{
				  this.balanceAmount=this.balanceAmount.subtract(amountPaid);
			  }
		  }else{
			  if(isWalletPayment == 'Y'){
				  if(this.walletAmount != null)
					    this.walletAmount=this.walletAmount.add(amountPaid);
					  else
						  this.walletAmount=BigDecimal.ZERO.add(amountPaid);
			  }else{
				  this.balanceAmount=this.balanceAmount.add(amountPaid);
			  }
		  }
			  
		  
		
	}

	public void setBalanceAmount(BigDecimal balanceAmount) {
		this.balanceAmount=balanceAmount;
		
	}

	public void setWalletAmount(BigDecimal balance) {
		this.walletAmount=balance;
		
	}



}
