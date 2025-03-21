import yt_dlp
import sys
import json

def get_audio_url(video_url):
    """Extract the best audio URL using yt-dlp."""
    ydl_opts = {
        'format': 'bestaudio/best',  # Get the best audio format
        'quiet': True,
        'noplaylist': True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            info = ydl.extract_info(video_url, download=False)
            return {"audio_url": info.get('url', '')}
        except Exception as e:
            return {"error": f"Failed to extract audio URL: {str(e)}"}

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No video URL provided"}))
        sys.exit(1)

    video_url = sys.argv[1]
    result = get_audio_url(video_url)
    print(json.dumps(result))
