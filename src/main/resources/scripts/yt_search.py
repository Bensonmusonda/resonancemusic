import yt_dlp
import sys
import json

def search_youtube(query, max_results=5):
    """Search YouTube and return a list of video titles and URLs."""
    ydl_opts = {
        'quiet': True,
        'extract_flat': True,  # Prevents downloading
        'skip_download': True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            search_results = ydl.extract_info(f"ytsearch{max_results}:{query}", download=False)
        except Exception as e:
            return {"error": f"Failed to search: {str(e)}"}

    videos = search_results.get('entries', [])
    return [{"title": video.get('title', 'Unknown Title'), "url": video.get('url', '')} for video in videos if video]

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No search query provided"}))
        sys.exit(1)

    query = sys.argv[1]
    results = search_youtube(query)
    print(json.dumps(results))
