package me.timothy.bots.database;

/**
 * Describes a database for mappings.
 * 
 * @author Timothy
 */
public interface MappingDatabase {
	public AdminUpdateMapping getAdminUpdateMapping();
	public CreationInfoMapping getCreationInfoMapping();
	public FullnameMapping getFullnameMapping();
	public LCCMapping getLccMapping();
	public RecheckMapping getRecheckMapping();
	public RepaymentMapping getRepaymentMapping();
	public ResetPasswordRequestMapping getResetPasswordRequestMapping();
	public ResponseHistoryMapping getResponseHistoryMapping();
	public ShareCodeMapping getShareCodeMapping();
	public UserMapping getUserMapping();
	public UsernameMapping getUsernameMapping();
	public WarningMapping getWarningMapping();
}
