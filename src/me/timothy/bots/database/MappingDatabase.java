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
	public LoanMapping getLoanMapping();
	public RecheckMapping getRecheckMapping();
	public RepaymentMapping getRepaymentMapping();
	public ResetPasswordRequestMapping getResetPasswordRequestMapping();
	public ResponseHistoryMapping getResponseHistoryMapping();
	public ResponseMapping getResponseMapping();
	public ShareCodeMapping getShareCodeMapping();
	public UserMapping getUserMapping();
	public UsernameMapping getUsernameMapping();
	public WarningMapping getWarningMapping();
	public RecentPostMapping getRecentPostMapping();
	public BannedUserMapping getBannedUserMapping();
	public SiteSessionMapping getSiteSessionMapping();
	public SavedQueryMapping getSavedQueryMapping();
	public SavedQueryParamMapping getSavedQueryParamMapping();
	public SavedQueryUserMapping getSavedQueryUserMapping();
	public RedFlagReportMapping getRedFlagReportMapping();
	public RedFlagMapping getRedFlagMapping();
	public RedFlagQueueSpotMapping getRedFlagQueueSpotMapping();
	public RedFlagForSubredditMapping getRedFlagForSubredditMapping();
	public RedFlagUserHistoryCommentMapping getRedFlagUserHistoryCommentMapping();
	public RedFlagUserHistoryLinkMapping getRedFlagUserHistoryLinkMapping();
	public RedFlagUserHistorySortMapping getRedFlagUserHistorySortMapping();
	public PromotionBlacklistMapping getPromotionBlacklistMapping();
}
