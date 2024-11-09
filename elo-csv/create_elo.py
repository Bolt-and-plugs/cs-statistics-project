import pandas as pd

# Load the CSV file to examine its structure
csgo_data = pd.read_csv('data/csgo_games.csv')

# Initialize parameters
initial_elo = 1500
k_factor = 32  # Typical K-factor in Elo calculations

# Initialize a dictionary to store the Elo rating for each team
elo_ratings = {}

# Function to calculate the expected score
def expected_score(elo_a, elo_b):
    return 1 / (1 + 10 ** ((elo_b - elo_a) / 400))

# Function to update Elo ratings after a match
def update_elo(elo_a, elo_b, score_a):
    expected_a = expected_score(elo_a, elo_b)
    new_elo_a = elo_a + k_factor * (score_a - expected_a)
    return new_elo_a

# Process each match in chronological order
elo_after_match = []  # To store Elo ratings after each match

for _, row in csgo_data.iterrows():
    team_1, team_2 = row['team_1'], row['team_2']
    t1_points, t2_points = row['t1_points'], row['t2_points']
    
    # Initialize Elo ratings for new teams
    if team_1 not in elo_ratings:
        elo_ratings[team_1] = initial_elo
    if team_2 not in elo_ratings:
        elo_ratings[team_2] = initial_elo
    
    # Get current Elo ratings
    elo_1, elo_2 = elo_ratings[team_1], elo_ratings[team_2]
    
    # Determine the match result for team_1
    if t1_points > t2_points:
        score_1, score_2 = 1, 0  # Team 1 won
    elif t1_points < t2_points:
        score_1, score_2 = 0, 1  # Team 2 won
    else:
        score_1, score_2 = 0.5, 0.5  # Draw
    
    # Update Elo ratings based on match outcome
    new_elo_1 = update_elo(elo_1, elo_2, score_1)
    new_elo_2 = update_elo(elo_2, elo_1, score_2)
    
    # Save updated Elo ratings back to the dictionary
    elo_ratings[team_1] = new_elo_1
    elo_ratings[team_2] = new_elo_2
    
    # Append the post-match Elo ratings for both teams
    elo_after_match.append((new_elo_1, new_elo_2))

# Convert the list of tuples to a DataFrame and merge with the original data
elo_df = pd.DataFrame(elo_after_match, columns=['team_1_elo', 'team_2_elo'])
csgo_data = pd.concat([csgo_data, elo_df], axis=1)
new_csgo_data = csgo_data[['match_date', 'team_1', 'team_2', 't1_points', 't2_points', 'team_1_elo', 'team_2_elo']]

new_csgo_data.to_csv('data/csgo_games_with_elo.csv', index=False)
