package com.github.eostermueller.tjp2.dataaccess_2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;

import com.github.eostermueller.tjp2.AppContext;
import com.github.eostermueller.snail4j.workload.annotations.Load;
import com.github.eostermueller.snail4j.workload.annotations.UserInterfaceDescription;

import com.github.eostermueller.tjp2.PerfSandboxException;
import com.github.eostermueller.tjp2.dataaccess.BaseManager;
import com.github.eostermueller.tjp2.dataaccess.PerfSandboxUtil;
import com.github.eostermueller.tjp2.model.Account;
import com.github.eostermueller.tjp2.model.Accounts;
import com.github.eostermueller.tjp2.model.Transaction;

public class AccountMgr2 implements BaseManager {
	boolean dbConnectionFromPool = true;
	public boolean isDbConnectionFromPool() {
		return dbConnectionFromPool;
	}
	public void setDbConnectionFromPool(boolean dbConnectionFromPool) {
		this.dbConnectionFromPool = dbConnectionFromPool;
	}
	@Autowired
	public void setAppContext(AppContext val) {
		this.init(val);
	}
	public AccountMgr2() {
		this.setAppContext(AppContext.SINGLETON_HIKARI_JDBC_CON_POOL);
	}
	protected Connection getConnection() throws SQLException, PerfSandboxException {
		Connection c = null;
		
		if ( isDbConnectionFromPool() )
			c = this.getAppContext().getConnection();
		else
			c = this.getAppContext().createDriverManagerConnection();
		
		return c;
		
	}
	
	public void init(AppContext val) {
		this.appContext = val;
		m_sqlTextMgr2.setLogger(val);
	}
	protected AppContext appContext = null;

	public static SqlTextMgr2 m_sqlTextMgr2 = new SqlTextMgr2();
	public Accounts getAccounts(List<Long> randomAccountIds) throws PerfSandboxException {
		Accounts accounts = getAccountsInternal(randomAccountIds);
		
		getTransactions(accounts);
		
		return accounts;
	}
	protected AppContext getPerfSandboxSingleton() {
		return this.appContext;
	}

	@Load(
			useCase = "01_Persistence_chunkyVersusChattySql", 
			value = {@UserInterfaceDescription("AccountMgr2")}
			)
	public Accounts getAccounts_bulkAccountBulkTransInq() throws PerfSandboxException {
		List<Long> accountIdsCriteria = 
				this.getPerfSandboxSingleton().getRandomAccountIds(
						this.getPerfSandboxSingleton().NUM_ACCOUNTS);
		return this.getAccounts(accountIdsCriteria);
	}
	

	private Accounts getAccountsInternal(List<Long> accountIds) throws PerfSandboxException  {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Accounts accounts = new Accounts();
		try {
			con = getConnection();
			ps = con.prepareStatement( m_sqlTextMgr2.getMultipleAccountsSql(accountIds.size()) );
			for (int i = 1; i <= accountIds.size(); i++) {
				ps.setLong(i, accountIds.get(i-1).longValue());
			}
			rs = ps.executeQuery();
			short count = 0;
			
			while(rs.next()) {
				Account a = createAccount(rs);
				accounts.addAccount(a);
			}
		} catch (SQLException e) {
			throw new PerfSandboxException(e);
		} finally {
			PerfSandboxUtil.closeQuietly(rs);
			PerfSandboxUtil.closeQuietly(ps);
			PerfSandboxUtil.closeQuietly(con);
		}
		return accounts;
	}

	private Account createAccount(ResultSet rs) throws SQLException {
		Account a = new Account();
		a.accountId = rs.getLong(1);
		a.branchId = rs.getInt(2);
		a.balance = rs.getLong(3);
		a.filler = rs.getString(4);
		a.filler01 = rs.getString(5);
		a.filler02 = rs.getString(6);
		a.filler03 = rs.getString(7);
		a.filler04 = rs.getString(8);
		a.filler05 = rs.getString(9);
		a.filler06 = rs.getString(10);
		a.filler07 = rs.getString(11);
		a.filler08 = rs.getString(12);
		a.filler09 = rs.getString(13);
		a.filler10 = rs.getString(14);
		a.filler11 = rs.getString(15);
		a.filler12 = rs.getString(16);
		a.filler13 = rs.getString(17);
		a.filler14 = rs.getString(18);
		a.filler15 = rs.getString(19);
		a.filler16 = rs.getString(20);
		a.filler17 = rs.getString(21);
		a.filler18 = rs.getString(22);
		a.filler19 = rs.getString(23);
		a.filler20 = rs.getString(24);
		return a;
	}
	private void getTransactions(Accounts accounts) throws PerfSandboxException  {
		
		Connection con = null; 
		PreparedStatement ps = null; 
		ResultSet rs = null;
		try {
			con = getConnection();
			ps = con.prepareStatement( m_sqlTextMgr2.getHistorySql(accounts.getAccounts().size()) );
			
			for (int i = 1; i <= accounts.getAccounts().size();i++) {
				ps.setLong(i, accounts.getAccounts().get(i-1).accountId );
			}
			rs = ps.executeQuery();
			while(rs.next()) {
				Transaction t = new Transaction();
				t.tellerId = rs.getInt(1);
				t.historyId = rs.getLong(2);
				t.branchId = rs.getInt(3);
				t.accountId = rs.getInt(4);
				t.delta = rs.getLong(5);
				t.mtime = PerfSandboxUtil.getDate(rs, 6); 
				t.filler = rs.getString(7);
				Account a = accounts.findAccount(t.accountId);
				if (a!=null)
					a.transactions.add(t);
				else
					throw new PerfSandboxException("Found transaction with account id = [" + t.accountId + "], but it didn't have a matching account.  Full transactions [" + t.toString() + "]");
			}
		} catch (SQLException e) {
			throw new PerfSandboxException(e);
		} finally {
			PerfSandboxUtil.closeQuietly(rs);
			PerfSandboxUtil.closeQuietly(ps);
			PerfSandboxUtil.closeQuietly(con);
		}
	}

	@Override
	public AppContext getAppContext() {
		return this.appContext;
	}

}
