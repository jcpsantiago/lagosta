-- :name get-cases :? :*
-- :doc Get all cases
SELECT 
	slack_team_id,
	team_name
FROM connected_teams
WHERE slack_team_id IS NOT NULL;
