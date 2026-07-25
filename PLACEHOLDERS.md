# CasinoCore Placeholders

This file documents the PlaceholderAPI placeholders registered by CasinoCore.

## Player economy

- `%casino_balance%` - formatted player balance
- `%casino_balance_raw%` - raw player balance with 2 decimals
- `%casino_economy%` - hooked economy provider name

## Lifetime player stats

- `%casino_wins%` - total wins
- `%casino_losses%` - total losses
- `%casino_games_played%` - total games played
- `%casino_streak%` - current win streak
- `%casino_best_streak%` - best win streak

## Weekly stats

- `%casino_wins_w%` - current week wins
- `%casino_wins_week%` - alias of `%casino_wins_w%`
- `%casino_losses_w%` - current week losses
- `%casino_losses_week%` - alias of `%casino_losses_w%`
- `%casino_games_played_w%` - current week games played
- `%casino_games_played_week%` - alias of `%casino_games_played_w%`

## Monthly stats

- `%casino_wins_m%` - current month wins
- `%casino_wins_month%` - alias of `%casino_wins_m%`
- `%casino_losses_m%` - current month losses
- `%casino_losses_month%` - alias of `%casino_losses_m%`
- `%casino_games_played_m%` - current month games played
- `%casino_games_played_month%` - alias of `%casino_games_played_m%`

## Last month stats

- `%casino_wins_lm%` - last month wins
- `%casino_wins_last_month%` - alias of `%casino_wins_lm%`
- `%casino_losses_lm%` - last month losses
- `%casino_losses_last_month%` - alias of `%casino_losses_lm%`
- `%casino_games_played_lm%` - last month games played
- `%casino_games_played_last_month%` - alias of `%casino_games_played_lm%`

## Daily reward

- `%casino_daily_ready%` - `true` if the daily reward can be claimed
- `%casino_next_daily%` - next daily claim text

## General plugin info

- `%casino_available_games%` - enabled casino game count
- `%casino_enabled_games%` - enabled casino game count
- `%casino_total_games%` - total registered casino game count
- `%casino_enabled_games_list%` - comma-separated list of enabled game ids
- `%casino_plugin_version%` - loaded CasinoCore plugin version
- `%casino_locale%` - active CasinoCore locale id
- `%casino_min_bet%` - formatted global minimum bet
- `%casino_max_bet%` - formatted global maximum bet
- `%casino_daily_reward%` - formatted configured daily reward value

## Time placeholders

- `%casino_time_year%` - current year
- `%casino_time_month%` - current month number
- `%casino_time_month_name%` - current month name in English
- `%casino_time_week%` - current ISO week number
- `%casino_time_day%` - current day of month
- `%casino_time_day_name%` - current day name in English
- `%casino_time_date%` - current date in `yyyy-MM-dd`
- `%casino_time_clock%` - current time in `HH:mm:ss`

## Last bet snapshot

- `%casino_last_bet_game%` - last logged game name
- `%casino_last_bet_section%` - last logged section name
- `%casino_last_bet_amount%` - last logged amount detail
- `%casino_last_bet_status%` - last logged status/result detail
- `%casino_last_bet_payout%` - last logged payout detail
- `%casino_last_bet_profit%` - last logged profit detail
