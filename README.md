# Breezy weather fixed vietnamese address

[![GitHub repo size](https://img.shields.io/github/repo-size/wikiepeidia/breezy-weather-VN)](https://github.com/wikiepeidia/breezy-weather-VN)
[![GitHub last commit](https://img.shields.io/github/last-commit/wikiepeidia/breezy-weather-VN)](https://github.com/wikiepeidia/breezy-weather-VN/commits)
![Commit activity](https://img.shields.io/github/commit-activity/w/wikiepeidia/breezy-weather-VN)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

## Use case

- the app is finetuned for new administrative vietnamese address starting on july 1 2025, and since nominatim return wrong addresses and it take a very long time to fix, we can ultilize this quick patch.

## Method

- we use LocationIQ, you provide the API key into the Setting page, start with `pk.abcdefghijk`
- The requests from LocationIQ even though it is paid, it is INSANELY generous enough for a  weather application. (5k requests/DAY , even MONTH is still generous!)

## what bug

- Nominatim return weirdass datas, some wrong address, while the correct address is buried in some other keys.

## Implement fix

- LocationIQ's JSON contains guranteed commune/ward names on the `display_name` section. By REGEX, we can grab what we need and properly display.
