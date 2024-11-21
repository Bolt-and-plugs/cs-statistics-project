const express = require('express');
const csv = require('csv-parser');
const fs = require('fs');
const moment = require('moment');
const stats = require('simple-statistics');
const { exec } = require('child_process');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const data = [];
const games = [];

const calculateWinProbability = (elo1, elo2) => 1 / (1 + Math.pow(10, (elo2 - elo1) / 400));

const calculateMetrics = eloValues => ({
    n: eloValues.length,
    media: stats.mean(eloValues),
    mediana: stats.median(eloValues),
    moda: stats.mode(eloValues),
    dp: stats.standardDeviation(eloValues),
    cv: stats.standardDeviation(eloValues),
    minimo: Math.min(...eloValues),
    q1: stats.quantile(eloValues, 0.25),
    q3: stats.quantile(eloValues, 0.75),
    maximo: Math.max(...eloValues),
    iq: stats.interquartileRange(eloValues),
    amplitude: Math.max(...eloValues) - Math.min(...eloValues)
});

// Load and parse the CSV file once on startup
fs.createReadStream('csgo_games.csv')
    .pipe(csv())
    .on('data', (row) => {
        row.match_date = moment(row.match_date, 'YYYY-MM-DD'); // Parse date format
        games.push(row);
    })
    .on('end', () => {
        console.log('CSV file successfully processed');
    });

fs.createReadStream('csgo_data.csv')
    .pipe(csv())
    .on('data', (row) => {
        row.match_date = moment(row.match_date, 'YYYY-MM-DD'); // Parse date format
        data.push(row);
    })
    .on('end', () => {
        console.log('CSV file successfully processed');
    });

// Define an API endpoint to get rankings for a given date
app.get('/rankings', (req, res) => {
    const dateInput = req.query.date;
    const date = moment(dateInput, 'YYYY-MM-DD', true);

    if (!date.isValid()) {
        return res.status(400).json({ error: 'Invalid date format. Use YYYY-MM-DD.' });
    }

    // Find the closest date data entry
    const closestMatch = data.reduce((closest, entry) => {
        const entryDate = moment(entry.match_date, 'YYYY-MM-DD');
        return Math.abs(entryDate.diff(date)) < Math.abs(moment(closest.match_date, 'YYYY-MM-DD').diff(date)) ? entry : closest;
    });

    if (!closestMatch) {
        return res.status(404).json({ error: 'No data found for the given date.' });
    }

    // Prepare the rankings as an array of objects
    const rankings = [];
    for (let i = 1; i <= 50; i++) {
        const team = closestMatch[`top_${i}`];
        const elo = closestMatch[`top_${i}_elo`];
        if (team && elo) {
            rankings.push({
                team,
                elo,
                top: i
            });
        }
    }

    // Send the response
    res.json({
        date: moment(closestMatch.match_date).format('YYYY-MM-DD'),
        teams: rankings,
    });
});

app.get('/match', (req, res) => {
    let [dateInput, team1Input, team2Input] = [req.query.date, req.query.team1, req.query.team2];
    const date = moment(dateInput, 'YYYY-MM-DD', true);
    let carlosI = -1;
    const change = !team1Input || !team2Input;

    if (!date.isValid()) {
        return res.status(400).json({ error: 'Invalid date format. Use YYYY-MM-DD.' });
    }
  
    // Find the closest match date
    const closestMatch = data.reduce((closest, entry, i) => {
        const { team_1, team_2 } = entry;
        const entryDate = entry.match_date;
        const closestDate = closest.match_date;

        if (Math.abs(entryDate - date) < Math.abs(closestDate - date)) {
            if (change) {
                team1Input = team_1;
                team2Input = team_2;
            }
            carlosI = i;
            return entry;
        } else {
            return closest;
        }
    });
  
    if (!closestMatch) {
        return res.status(404).json({ error: 'No data found for the given date.' });
    }
  
    // Find team ELO ratings
    const team1Rank = Math.ceil((Object.values(closestMatch).findIndex((val, i) => i > 2 ? val === team1Input : false) - 1) / 2);
    const team2Rank = Math.ceil((Object.values(closestMatch).findIndex((val, i) => i > 2 ? val === team2Input : false) - 1) / 2);
    
    const team1Elo = closestMatch[`top_${team1Rank}_elo`];
    const team2Elo = closestMatch[`top_${team2Rank}_elo`];
  
    if (!team1Elo || !team2Elo) {
        return res.status(404).json({ error: 'Team ELO not found for the given teams.' });
    }
  
    // Calculate win probabilities
    const team1WinProbability = calculateWinProbability(team1Elo, team2Elo);
    const team2WinProbability = 1 - team1WinProbability;
  
    // Respond with the calculated odds
    const { t1_points, t2_points, team_1, team_2, match_date } = games[carlosI];
    const isFicticional = !((match_date.format('YYYY-MM-DD') === dateInput && team_1 === team1Input && team_2 === team2Input) || change);
    
<<<<<<< HEAD
    exec(`python3 sim_matches.py ${team1Input} ${team2Input} ${dateInput}`, (err, stdout, stderr) => {
=======
    exec(`python sim_matches.py ${team1Input} ${team2Input} ${dateInput}`, (err, stdout, stderr) => {
>>>>>>> refs/remotes/origin/main
        if (!(stdout === 't1' || stdout === 't2')) {
            console.log({err, stdout, stderr})
            return console.log("bigodou legal");
        }
        res.json({
            date: closestMatch.match_date.format('YYYY-MM-DD'),
            winner: !isFicticional ? (t1_points > t2_points ? team_1 : team_2) : null,
            team1: {
                name: team1Input,
                elo: Math.round(team1Elo),
                win_probability: team1WinProbability,
                ai_winner: stdout === 't1'
            },
            team2: {
                name: team2Input,
                elo: Math.round(team2Elo),
                win_probability: team2WinProbability,
                ai_winner: stdout === 't2'
            }
        });
    })
});

app.get('/metrics', (req, res) => {
    const { team, date_start, date_end } = req.query;
    
    if (!team || !date_start || !date_end) {
      return res.status(400).json({ error: 'Please provide team, date_start, and date_end query parameters.' });
    }
  
    const startDate = moment(date_start, 'YYYY-MM-DD', true);
    const endDate = moment(date_end, 'YYYY-MM-DD', true);
    
    if (!startDate.isValid() || !endDate.isValid()) {
        return res.status(400).json({ error: 'Invalid date format. Use YYYY-MM-DD for date_start and date_end.' });
    }
  
    const filteredData = games.filter(match => {
        const { match_date, team_1, team_2 } = match;
        return match_date.isBetween(startDate, endDate, 'day', '[]') && (team_1 === team || team === team_2)
    });
    const eloValues = filteredData.map(entry => {
        const { team_1, team_1_elo, team_2_elo } = entry;
        return Math.round(Number(team_1 === team ? team_1_elo : team_2_elo))
    });
    
    if (eloValues.length === 0) {
        return res.status(404).json({ error: `No ELO data found for team ${team} in the specified date range.` });
    }
  
    // Calculate metrics
    const metrics = calculateMetrics(eloValues);
    
    // Respond with the metrics
    res.json({ team, date_start, date_end, metrics });
  });

// Start the server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server is running on port ${PORT}`);
});
