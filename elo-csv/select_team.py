import pandas as pd

# Function to write the filtered data to a CSV file based on team name
def filter_team_matches(team_name, input_file='data/csgo_games_with_elo.csv'):
    # Read the data from the input file
    csgo_data = pd.read_csv(input_file)
    
    # Initialize a list to store rows for the new CSV
    csv_data = []
    
    # Iterate through the rows in the data
    for _, row in csgo_data.iterrows():
        if row['team_1'] == team_name or row['team_2'] == team_name:
            # Determine if the selected team is team_1 or team_2
            if row['team_1'] == team_name:
                elo = row['team_1_elo']
                against = row['team_2']
                against_elo = row['team_2_elo']
            else:
                elo = row['team_2_elo']
                against = row['team_1']
                against_elo = row['team_1_elo']
            
            # Append the match data to the csv_data list
            csv_data.append({
                'match_date': row['match_date'],
                'elo': elo,
                'against': against,
                'against_elo': against_elo
            })
    
    # Convert the list of dictionaries to a pandas DataFrame
    df = pd.DataFrame(csv_data)
    
    # Write the DataFrame to a CSV file
    df.to_csv('data/teams/{}.csv'.format(team_name), index=False)

# Main function to take the team name as input
def main():
    team_name = input("Enter the team name: ")
    filter_team_matches(team_name)

# Run the main function
if __name__ == "__main__":
    main()
