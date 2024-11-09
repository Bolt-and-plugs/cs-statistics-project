import pandas as pd

# Load the CSV data
csgo_data = pd.read_csv('data/csgo_games_with_elo.csv')

# Initialize an empty list to store the results for CSV
greatest_elo = []
csv_data = []
qtd = 5  # Number of top teams to include

# Iterate over each match in the data
for _, row in csgo_data.iterrows():
    team_1 = {'name': row['team_1'], 'elo': row['team_1_elo']}
    team_2 = {'name': row['team_2'], 'elo': row['team_2_elo']}
    
    # Add the two teams for the current match
    greatest_elo.append(team_1)
    greatest_elo.append(team_2)
    
    # Sort the list by ELO in descending order
    greatest_elo.sort(reverse=True, key=lambda x: x['elo'])
    
    # Remove duplicate teams with the same name, keeping the one with the highest ELO
    unique_teams = {}
    for team in greatest_elo:
        if team['name'] not in unique_teams:
            unique_teams[team['name']] = team
        else:
            # If team with the same name already exists, keep the one with the higher ELO
            if team['elo'] > unique_teams[team['name']]['elo']:
                unique_teams[team['name']] = team
    
    # Get the unique teams sorted by ELO again
    greatest_elo = sorted(unique_teams.values(), reverse=True, key=lambda x: x['elo'])

    # Keep only the top `qtd` teams
    if len(greatest_elo) > qtd:
        greatest_elo = greatest_elo[:qtd]
    
    # Prepare the data for the CSV row (starting with match date)
    row_data = {'match_date': row['match_date']}
    
    # Add each top team's name and elo to the row (e.g., top_1, top_1_elo, top_2, top_2_elo, ..., top_qtd, top_qtd_elo)
    for i in range(qtd):
        if i < len(greatest_elo):  # In case there are less than `qtd` teams
            row_data[f'top_{i + 1}'] = greatest_elo[i]['name']
            row_data[f'top_{i + 1}_elo'] = greatest_elo[i]['elo']
        else:
            row_data[f'top_{i + 1}'] = ''  # Fill empty if there are fewer teams than `qtd`
            row_data[f'top_{i + 1}_elo'] = ''  # Fill empty if there are fewer teams than `qtd`
    
    # Append the row data to the csv_data list
    csv_data.append(row_data)

# Convert the list of dictionaries to a pandas DataFrame
df = pd.DataFrame(csv_data)

# Write the DataFrame to a CSV file
df.to_csv('data/csgo_top_teams_with_elo.csv', index=False)