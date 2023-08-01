# LocalGPT-Android
- The Local GPT Android is a mobile application that runs the GPT (Generative Pre-trained Transformer) model directly on your Android device. This app does not require an active internet connection, as it executes the GPT model locally.
- (It still uses Internet to download the model, you can manually place the model in data directory and disable internet).

## Requirements
- Android 11+
- At least 3-4 GB of Free RAM (Again "Free" RAM)
- I tested it on iQoo 11 with 16GB RAM and Snapdragon 8 Gen 2

## Features
- It works :) 
- Only Text Completions as of now
    - If you want to to chat, send messages like  `### Human:\n prompt\n ### Assistant:`
- The model used does not care about ethics afaik

## How to?
- Download the latest release
- Wait for the download to complete 
    - The model is ggml-gpt4all-j-v1.3-groovy.bin (Downloaded from gpt4all.io)
- The model will get loaded 
- You can start chatting

## Benchmarks
| Device Name     | SoC       | RAM   | Model Load Time | Average Response Initiation Time |
|-----------------|-----------|-------|-----------------|----------------------------------|
| iQoo 11         | SD 8 Gen 2 | 16 GB | 4 seconds       | 2 seconds                        |
| Galaxy S21 Plus | SD 888    | 8 GB  | 7 seconds       | 6 seconds                        |
| LG G8X          | SD 855    | 6 GB  | Did not run     | NaN                              |
| Xiaomi Poco F5  | SD 7+Gen2 | 8 GB  | 7 seconds       | 5 seconds                        |
| Pixel 6 Pro     | Tensor    | 12 GB | 5 seconds       | 5 seconds                        |

## Demo
- [Demo Video](demo.mp4) or https://youtube.com/shorts/J7DJ-40Uy1k?feature=share

## TODO
- [x] Add very basic chat support
- [ ] Reduce the size of the model
- [ ] Fix UI issues

## Notes
- It is very much buggy. I haven't tested it much.
- You'll need a good device to run this.
- Response initiation time and RAM usage for Chat Completion increases with the number of messages.
  - This is because Chat Completion is using Text Completion, and with every message the prompt size increases.
  - This could be fixed by training the model with Chat model in mind. I'm looking into this.

## Credits
- nomic-AI
