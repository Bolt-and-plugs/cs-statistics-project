import pandas as pd
from pickle import load
import sys

games = pd.read_csv('csgo_games_sorted_with_elo_ai.csv', index_col='Unnamed: 0')

def create_match(team_1 : str, team_2, date) -> pd.DataFrame:
    match_dict = {
        'match_date' : date,
        'team_1' : team_1,
        'team_2' : team_2,
    }

    features = list(games.columns)
    team_1_features = [f for f in features if f.startswith("t1_") or f.startswith("team_1")]
    team_2_features = [f for f in features if f.startswith("t2_") or f.startswith("team_2")]

    team_1_games = games[(games['team_1'] == team_1) | (games['team_2'] == team_1)]
    team_2_games = games[(games['team_2'] == team_2) | (games['team_1'] == team_2)]

    team_1_match = team_1_games.iloc[-1]
    team_2_match = team_2_games.iloc[-1]

    if team_1 == team_1_match['team_1']:
        for t1_f in team_1_features:
            match_dict[t1_f] = team_1_match[t1_f]

    else:
        #team_1 == team_1_match['team_2']
        for idx, t1_f in enumerate(team_1_features):
            match_dict[t1_f] = team_1_match[team_2_features[idx]]
  
    if team_2 == team_2_match['team_2']:
        for t2_f in team_2_features:
            match_dict[t2_f] = team_2_match[t2_f]
    else:
        for idx, t2_f in enumerate(team_2_features):
            match_dict[t2_f] = team_2_match[team_1_features[idx]]
  
    team_1_vs_team_2_matches = games[((games['team_1'] == team_1) & (games['team_1'] == team_1)) | ((games['team_2'] == team_1) & (games['team_1'] == team_2))]
    team_1_vs_team_2_match = team_1_vs_team_2_matches.iloc[-1]

    if team_1_vs_team_2_match['team_1'] == team_1:
        match_dict['t1_h2h_win_perc'] = team_1_vs_team_2_match['t1_h2h_win_perc']
        match_dict['t2_h2h_win_perc'] = team_1_vs_team_2_match['t2_h2h_win_perc']
        match_dict['h2h_win_perc_diff'] = team_1_vs_team_2_match['h2h_win_perc_diff']

    if team_1_vs_team_2_match['team_1'] == team_2:
        match_dict['t1_h2h_win_perc'] = team_1_vs_team_2_match['t1_h2h_win_perc']
        match_dict['t2_h2h_win_perc'] = team_1_vs_team_2_match['t2_h2h_win_perc']
        match_dict['h2h_win_perc_diff'] = team_1_vs_team_2_match['h2h_win_perc_diff']

    # Calcular diffs
    match_dict['world_rank_diff'] = match_dict['t1_world_rank'] - match_dict['t2_world_rank']
    match_dict['elo_diff'] = match_dict['team_1_elo'] - match_dict['team_2_elo']
    match_dict['avg_rating_diff'] = match_dict['t1_avg_rating'] - match_dict['t2_avg_rating']
    match_dict['opk_ratio_diff'] = match_dict['t1_avg_opk_ratio'] - match_dict['t2_avg_opk_ratio']
    match_dict['clutch_win_diff'] = match_dict['t1_avg_clutch_win_perc'] - match_dict['t2_avg_clutch_win_perc']
    match_dict['sniper_ratio_diff'] = match_dict['t1_sniper_ratio'] - match_dict['t2_sniper_ratio'] 
    match_dict['avg_impact_diff'] = match_dict['t1_avg_impact'] - match_dict['t2_avg_impact']
    match_dict['avg_kdr_diff'] = match_dict['t1_avg_kdr'] - match_dict['t2_avg_kdr']
    match_dict['avg_dmr_diff'] = match_dict['t1_avg_dmr'] - match_dict['t2_avg_dmr']
    match_dict['avg_kpr_diff'] = match_dict['t1_avg_kpr'] - match_dict['t2_avg_kpr']
    match_dict['avg_apr_diff'] = match_dict['t1_avg_kpr'] - match_dict['t2_avg_kpr']
    match_dict['avg_dpr_diff'] = match_dict['t1_avg_dpr'] - match_dict['t2_avg_dpr']
    match_dict['avg_spr_diff'] = match_dict['t1_avg_spr'] - match_dict['t2_avg_spr']
    match_dict['avg_opk_rating_diff'] = match_dict['t1_avg_opk_rating'] - match_dict['t2_avg_opk_rating']
    match_dict['avg_wins_perc_after_fk_diff'] = match_dict['t1_avg_wins_perc_after_fk'] - match_dict['t2_avg_wins_perc_after_fk']
    match_dict['avg_fk_perc_in_wins_diff'] = match_dict['t1_avg_fk_perc_in_wins'] - match_dict['t2_avg_fk_perc_in_wins']  
    match_dict['avg_multikill_perc_diff'] = match_dict['t1_avg_multikill_perc'] - match_dict['t2_avg_multikill_perc']
    match_dict['avg_rating_at_least_one_perc_diff'] = match_dict['t1_avg_rating_at_least_one_perc'] - match_dict['t1_avg_rating_at_least_one_perc']
      
    match_dataframe = pd.DataFrame(pd.Series(match_dict)).T
    match_dataframe = match_dataframe.drop(columns = ['t1_points', 't2_points'])
    match_dataframe = match_dataframe[[
        "match_date", "team_1", "team_2", "t1_world_rank", "t2_world_rank",
        "t1_h2h_win_perc", "t2_h2h_win_perc", "t1_player1_rating", "t1_player1_impact",
        "t1_player1_kdr", "t1_player1_dmr", "t1_player1_kpr", "t1_player1_apr", "t1_player1_dpr",
        "t1_player1_spr", "t1_player1_opk_ratio", "t1_player1_opk_rating", "t1_player1_wins_perc_after_fk",
        "t1_player1_fk_perc_in_wins", "t1_player1_multikill_perc", "t1_player1_rating_at_least_one_perc",
        "t1_player1_is_sniper", "t1_player1_clutch_win_perc", "t1_player2_rating", "t1_player2_impact",
        "t1_player2_kdr", "t1_player2_dmr", "t1_player2_kpr", "t1_player2_apr", "t1_player2_dpr",
        "t1_player2_spr", "t1_player2_opk_ratio", "t1_player2_opk_rating", "t1_player2_wins_perc_after_fk",
        "t1_player2_fk_perc_in_wins", "t1_player2_multikill_perc", "t1_player2_rating_at_least_one_perc",
        "t1_player2_is_sniper", "t1_player2_clutch_win_perc", "t1_player3_rating", "t1_player3_impact",
        "t1_player3_kdr", "t1_player3_dmr", "t1_player3_kpr", "t1_player3_apr", "t1_player3_dpr",
        "t1_player3_spr", "t1_player3_opk_ratio", "t1_player3_opk_rating", "t1_player3_wins_perc_after_fk",
        "t1_player3_fk_perc_in_wins", "t1_player3_multikill_perc", "t1_player3_rating_at_least_one_perc",
        "t1_player3_is_sniper", "t1_player3_clutch_win_perc", "t1_player4_rating", "t1_player4_impact",
        "t1_player4_kdr", "t1_player4_dmr", "t1_player4_kpr", "t1_player4_apr", "t1_player4_dpr",
        "t1_player4_spr", "t1_player4_opk_ratio", "t1_player4_opk_rating", "t1_player4_wins_perc_after_fk",
        "t1_player4_fk_perc_in_wins", "t1_player4_multikill_perc", "t1_player4_rating_at_least_one_perc",
        "t1_player4_is_sniper", "t1_player4_clutch_win_perc", "t1_player5_rating", "t1_player5_impact",
        "t1_player5_kdr", "t1_player5_dmr", "t1_player5_kpr", "t1_player5_apr", "t1_player5_dpr",
        "t1_player5_spr", "t1_player5_opk_ratio", "t1_player5_opk_rating", "t1_player5_wins_perc_after_fk",
        "t1_player5_fk_perc_in_wins", "t1_player5_multikill_perc", "t1_player5_rating_at_least_one_perc",
        "t1_player5_is_sniper", "t1_player5_clutch_win_perc", "t2_player1_rating", "t2_player1_impact",
        "t2_player1_kdr", "t2_player1_dmr", "t2_player1_kpr", "t2_player1_apr", "t2_player1_dpr",
        "t2_player1_spr", "t2_player1_opk_ratio", "t2_player1_opk_rating", "t2_player1_wins_perc_after_fk",
        "t2_player1_fk_perc_in_wins", "t2_player1_multikill_perc", "t2_player1_rating_at_least_one_perc",
        "t2_player1_is_sniper", "t2_player1_clutch_win_perc", "t2_player2_rating", "t2_player2_impact",
        "t2_player2_kdr", "t2_player2_dmr", "t2_player2_kpr", "t2_player2_apr", "t2_player2_dpr",
        "t2_player2_spr", "t2_player2_opk_ratio", "t2_player2_opk_rating", "t2_player2_wins_perc_after_fk",
        "t2_player2_fk_perc_in_wins", "t2_player2_multikill_perc", "t2_player2_rating_at_least_one_perc",
        "t2_player2_is_sniper", "t2_player2_clutch_win_perc", "t2_player3_rating", "t2_player3_impact",
        "t2_player3_kdr", "t2_player3_dmr", "t2_player3_kpr", "t2_player3_apr", "t2_player3_dpr",
        "t2_player3_spr", "t2_player3_opk_ratio", "t2_player3_opk_rating", "t2_player3_wins_perc_after_fk",
        "t2_player3_fk_perc_in_wins", "t2_player3_multikill_perc", "t2_player3_rating_at_least_one_perc",
        "t2_player3_is_sniper", "t2_player3_clutch_win_perc", "t2_player4_rating", "t2_player4_impact",
        "t2_player4_kdr", "t2_player4_dmr", "t2_player4_kpr", "t2_player4_apr", "t2_player4_dpr",
        "t2_player4_spr", "t2_player4_opk_ratio", "t2_player4_opk_rating", "t2_player4_wins_perc_after_fk",
        "t2_player4_fk_perc_in_wins", "t2_player4_multikill_perc", "t2_player4_rating_at_least_one_perc",
        "t2_player4_is_sniper", "t2_player4_clutch_win_perc", "t2_player5_rating", "t2_player5_impact",
        "t2_player5_kdr", "t2_player5_dmr", "t2_player5_kpr", "t2_player5_apr", "t2_player5_dpr",
        "t2_player5_spr", "t2_player5_opk_ratio", "t2_player5_opk_rating", "t2_player5_wins_perc_after_fk",
        "t2_player5_fk_perc_in_wins", "t2_player5_multikill_perc", "t2_player5_rating_at_least_one_perc",
        "t2_player5_is_sniper", "t2_player5_clutch_win_perc", "team_1_elo", "team_2_elo", "world_rank_diff",
        "elo_diff", "h2h_win_perc_diff", "t1_avg_rating", "t2_avg_rating", "avg_rating_diff",
        "t1_avg_opk_ratio", "t2_avg_opk_ratio", "opk_ratio_diff", "t1_avg_clutch_win_perc", "t2_avg_clutch_win_perc",
        "clutch_win_diff", "t1_sniper_ratio", "t2_sniper_ratio", "sniper_ratio_diff", "t1_avg_impact",
        "t2_avg_impact", "avg_impact_diff", "t1_avg_kdr", "t2_avg_kdr", "avg_kdr_diff", "t1_avg_dmr",
        "t2_avg_dmr", "avg_dmr_diff", "t1_avg_kpr", "t2_avg_kpr", "avg_kpr_diff", "t1_avg_apr", "t2_avg_apr",
        "avg_apr_diff", "t1_avg_dpr", "t2_avg_dpr", "avg_dpr_diff", "t1_avg_spr", "t2_avg_spr", "avg_spr_diff",
        "t1_avg_opk_rating", "t2_avg_opk_rating", "avg_opk_rating_diff", "t1_avg_wins_perc_after_fk",
        "t2_avg_wins_perc_after_fk", "avg_wins_perc_after_fk_diff", "t1_avg_fk_perc_in_wins", "t2_avg_fk_perc_in_wins",
        "avg_fk_perc_in_wins_diff", "t1_avg_multikill_perc", "t2_avg_multikill_perc", "avg_multikill_perc_diff",
        "t1_avg_rating_at_least_one_perc", "t2_avg_rating_at_least_one_perc", "avg_rating_at_least_one_perc_diff"
    ]]
    
    return match_dataframe

team_1 = sys.argv[1]
team_2 = sys.argv[2]
date = pd.to_datetime(sys.argv[3])

match = create_match(team_1, team_2, date)

with open("model.pkl", "rb") as f:
    rf = load(f)
    a = rf.predict(match.drop(columns=['match_date', 'team_1', 'team_2']))
    print(a[0], end='')


